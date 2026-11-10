"""Publish a validated summary of INMET notices for IBGE municipality 3543402."""
import datetime as dt
import json
import os
import sys
import tempfile
from pathlib import Path
from urllib.request import Request, urlopen
from zoneinfo import ZoneInfo

SOURCE = "https://apiprevmet3.inmet.gov.br/avisos/ativos"
ZONE = ZoneInfo("America/Sao_Paulo")
LIMIT = 4_000_000


def summarize(data, now):
    rows = data["hoje"] + data["futuro"]
    latest = {}
    for row in rows:
        codes = row["geocodes"]
        if not isinstance(codes, str):
            raise ValueError("Invalid municipality codes")
        alert_id, sequence = int(row["id_aviso"]), int(row["id_sequencia"])
        if alert_id not in latest or sequence > latest[alert_id][0]:
            latest[alert_id] = (sequence, row)
    result = []
    for alert_id, (_, row) in latest.items():
        if "3543402" not in row["geocodes"].replace(" ", "").split(",") or row["encerrado"] is True:
            continue
        start = dt.datetime.fromisoformat(row["inicio"]).replace(tzinfo=ZONE)
        end = dt.datetime.fromisoformat(row["fim"]).replace(tzinfo=ZONE)
        if end <= start:
            raise ValueError("Invalid alert period")
        if end <= now:
            continue
        title, severity = row["descricao"].strip(), row["severidade"].strip()
        if not (0 < len(title) <= 140 and 0 < len(severity) <= 80):
            raise ValueError("Invalid alert title or severity")
        detail_id = int(row["id"])
        if detail_id <= 0 or alert_id <= 0:
            raise ValueError("Invalid alert ID")
        result.append(dict(id=detail_id, title=title, severity=severity,
                           startsAt=start.astimezone(dt.timezone.utc).isoformat(timespec="seconds"),
                           endsAt=end.astimezone(dt.timezone.utc).isoformat(timespec="seconds")))
    if len(result) > 40:
        raise ValueError("Too many municipal alerts")
    return dict(version=1, municipality="3543402", source="INMET",
                fetchedAt=now.isoformat(timespec="seconds"),
                alerts=sorted(result, key=lambda x: (x["startsAt"], x["id"])))


def refresh(target):
    request = Request(SOURCE, headers={
        "User-Agent": "OurTalk/1.0 (+https://ourtalk.com.br/)",
        "Accept": "application/json",
    })
    with urlopen(request, timeout=20) as response:
        data = response.read(LIMIT + 1)
    if len(data) > LIMIT:
        raise ValueError("INMET response too large")
    now = dt.datetime.now(dt.timezone.utc)
    output = summarize(json.loads(data), now)
    target = Path(target)
    with tempfile.NamedTemporaryFile(mode="w", encoding="utf-8", dir=target.parent,
                                     prefix=".inmet-", delete=False) as temporary:
        name = temporary.name
        try:
            json.dump(output, temporary, separators=(",", ":"), ensure_ascii=False)
            temporary.flush()
            os.fsync(temporary.fileno())
            os.chmod(name, 0o644)
            os.replace(name, target)
        finally:
            Path(name).unlink(missing_ok=True)


if __name__ == "__main__":
    refresh(sys.argv[1])
