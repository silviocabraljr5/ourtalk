"""Refresh the public, city-wide forecast. Run as the web site's owner."""
import datetime as dt
import json
import math
import os
import sys
import tempfile
from pathlib import Path
from urllib.parse import urlencode
from urllib.request import urlopen
from zoneinfo import ZoneInfo

ZONE = "America/Sao_Paulo"
FIELDS = ("temperature_2m_min", "temperature_2m_max",
          "precipitation_probability_max", "weather_code")
URL = "https://api.open-meteo.com/v1/forecast?" + urlencode({
    "latitude": -21.1776,
    "longitude": -47.8101,
    "timezone": ZONE,
    "forecast_days": 3,
    "daily": ",".join(FIELDS),
})


def refresh(target):
    with urlopen(URL, timeout=20) as response:
        payload = response.read(65_537)
    if len(payload) > 65_536:
        raise ValueError("Forecast response too large")
    received = dt.datetime.now(dt.timezone.utc)
    data = json.loads(payload)["daily"]
    today = received.astimezone(ZoneInfo(ZONE)).date()
    forecast = []
    for i, date in enumerate(data["time"]):
        low, high, rain, code = (data[field][i] for field in FIELDS)
        if (dt.date.fromisoformat(date) != today + dt.timedelta(days=i)
                or not all(type(x) in (int, float) and math.isfinite(x) for x in (low, high, rain, code))
                or not -60 <= low <= high <= 65 or not 0 <= rain <= 100
                or type(code) is not int or not 0 <= code <= 99):
            raise ValueError("Invalid forecast row")
        forecast.append(dict(date=date, low=low, high=high, rain=rain, code=code))
    if len(forecast) != 3:
        raise ValueError("Incomplete forecast")
    output = dict(version=1, timezone=ZONE,
                  fetchedAt=received.isoformat(timespec="seconds"), days=forecast)
    target = Path(target)
    with tempfile.NamedTemporaryFile(mode="w", encoding="utf-8", dir=target.parent,
                                     prefix=".weather-", delete=False) as temporary:
        name = temporary.name
        try:
            json.dump(output, temporary, separators=(",", ":"))
            temporary.flush()
            os.fsync(temporary.fileno())
            os.chmod(name, 0o644)
            os.replace(name, target)
        finally:
            Path(name).unlink(missing_ok=True)


if __name__ == "__main__":
    refresh(sys.argv[1])
