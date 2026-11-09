"""Bundle public facilities from CNES and the municipal boundary from IBGE."""
import io
import json
import math
import sys
import zipfile
from pathlib import Path

MUNICIPALITY = "354340"
HOSPITALS = {"5": "Hospital geral", "7": "Hospital especializado", "62": "Hospital-dia"}
EMERGENCY_TYPES = {"20": "Pronto-socorro geral", "21": "Pronto-socorro especializado",
                   "73": "Pronto atendimento"}
MUNICIPAL_URGENT_CARE = {
    "7038747": "UPA Leste",
    "0253588": "UPA Norte",
    "0414301": "UPA Oeste",
    "8212201": "UPA Ribeirão Verde",
    "0881503": "Pronto Atendimento Vila Virgínia",
}


def records(stream):
    """Read the national JSON array in chunks without loading it into memory."""
    decoder = json.JSONDecoder()
    if stream.read(1) != "[":
        raise ValueError("Expected CNES JSON array")
    remaining = ""
    while True:
        if len(remaining) < 65536:
            remaining += stream.read(131072)
        remaining = remaining.lstrip()
        if remaining.startswith("]"):
            return
        if remaining.startswith(","):
            remaining = remaining[1:].lstrip()
        item, end = decoder.raw_decode(remaining)
        yield item
        remaining = remaining[end:]


def build(boundary_file, cnes_zip, snapshot):
    features = json.loads(Path(boundary_file).read_text(encoding="utf-8"))["features"]
    feature = next(f for f in features if f["properties"]["codarea"] == "3543402")
    geometry = feature["geometry"]
    if geometry["type"] not in {"Polygon", "MultiPolygon"}:
        raise ValueError("Unexpected boundary geometry")
    polygons = [geometry["coordinates"]] if geometry["type"] == "Polygon" else geometry["coordinates"]
    outlines = [polygon[0] for polygon in polygons]
    longitudes, latitudes = zip(*(point for ring in outlines for point in ring))
    bounds = [min(latitudes) - .005, min(longitudes) - .005,
              max(latitudes) + .005, max(longitudes) + .005]
    places = []
    with zipfile.ZipFile(cnes_zip) as archive:
        with io.TextIOWrapper(archive.open("cnes_estabelecimentos.json"), encoding="utf-8-sig") as source:
            for row in records(source):
                if row["CO_IBGE"] != MUNICIPALITY or row.get("CO_MOTIVO_DESAB") not in (None, "", "0", "00"):
                    continue
                code = row["CO_CNES"].zfill(7)
                kind = row["TP_UNIDADE"].lstrip("0")
                municipal = code in MUNICIPAL_URGENT_CARE
                category = "Pronto atendimento municipal" if municipal else HOSPITALS.get(kind, EMERGENCY_TYPES.get(kind))
                if category is None:
                    continue
                name = MUNICIPAL_URGENT_CARE.get(code) or row.get("NO_FANTASIA") or row.get("NO_RAZAO_SOCIAL") or f"CNES {code}"
                place = {"id": code, "name": name.strip(), "category": category, "municipal": municipal,
                         "address": " · ".join(filter(None, [
                             " ".join(filter(None, [row.get("NO_LOGRADOURO"), row.get("NU_ENDERECO")])),
                             row.get("NO_BAIRRO"),
                         ]))}
                try:
                    lat, lon = float(row["NU_LATITUDE"]), float(row["NU_LONGITUDE"])
                except (TypeError, ValueError, KeyError):
                    pass
                else:
                    if math.isfinite(lat) and math.isfinite(lon) and bounds[0] <= lat <= bounds[2] and bounds[1] <= lon <= bounds[3]:
                        place.update(lat=lat, lon=lon)
                places.append(place)
    ids = {place["id"] for place in places}
    if len(ids) != len(places) or not set(MUNICIPAL_URGENT_CARE).issubset(ids):
        raise ValueError("Duplicate CNES IDs or missing municipal urgent care facilities")
    # The municipal directory identifies this separate site of the HC.
    # Point: OpenStreetMap node 492635628. The directory supplies the address.
    places.append({"id": "hc-ue", "name": "HC · Unidade de Emergência",
                   "category": "Unidade de emergência", "municipal": False,
                   "address": "Rua Bernardino de Campos, 1000 · Centro",
                   "lat": -21.185691, "lon": -47.808456})
    places.sort(key=lambda place: (not place["municipal"], place["name"].casefold()))
    return {"version": 1, "snapshot": snapshot, "bounds": bounds, "outlines": outlines, "places": places}


if __name__ == "__main__":
    boundary, cnes, target, snapshot = sys.argv[1:]
    data = build(boundary, cnes, snapshot)
    Path(target).write_text(json.dumps(data, ensure_ascii=False, separators=(",", ":")) + "\n",
                            encoding="utf-8")
    print(f'{len(data["places"])} facilities, {sum("lat" in p for p in data["places"])} mapped')
