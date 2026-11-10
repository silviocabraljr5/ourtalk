"""Convert an OSM XML extract to the compact, ODbL walking graph."""
import struct
import sys
from pathlib import Path
from xml.etree import ElementTree

WALKABLE = {"residential", "living_street", "unclassified", "service",
            "primary", "secondary", "tertiary", "footway", "pedestrian", "path", "steps"}
ALLOWED = {"yes", "designated", "permissive"}


def tags(element):
    return {tag.attrib["k"]: tag.attrib["v"] for tag in element.findall("tag")}


def walkable(way):
    if way.get("highway") not in WALKABLE or "conditional" in " ".join(way):
        return False
    if way.get("motorroad") == "yes" or way.get("foot") in {"no", "private", "use_sidepath"}:
        return False
    if way.get("sidewalk") in {"no", "none", "separate"} and way.get("foot") not in ALLOWED:
        return False
    if way.get("highway") in {"primary", "secondary", "tertiary"}:
        if way.get("foot") not in ALLOWED and way.get("sidewalk") not in {"yes", "both", "left", "right"}:
            return False
    return way.get("access", "yes") in ALLOWED or way.get("foot") in ALLOWED


def build(source, target):
    osm = ElementTree.parse(source).getroot()
    bounds = osm.find("bounds").attrib
    south, west, north, east = (float(bounds[k]) for k in ("minlat", "minlon", "maxlat", "maxlon"))
    nodes = {}
    for node in osm.findall("node"):
        lat, lon = float(node.attrib["lat"]), float(node.attrib["lon"])
        node_tags = tags(node)
        if not (south <= lat <= north and west <= lon <= east):
            continue
        if node_tags.get("foot", "yes") not in ALLOWED or node_tags.get("access", "yes") not in ALLOWED:
            continue
        if "barrier" in node_tags and node_tags.get("foot") not in ALLOWED:
            continue
        nodes[node.attrib["id"]] = (round(lat * 1e6), round(lon * 1e6))
    edges = set()
    for way in osm.findall("way"):
        way_tags = tags(way)
        if not walkable(way_tags):
            continue
        refs = [node.attrib["ref"] for node in way.findall("nd")]
        direction = way_tags.get("oneway:foot")
        for a, b in zip(refs, refs[1:]):
            if a == b or a not in nodes or b not in nodes:
                continue
            if direction != "-1":
                edges.add((a, b))
            if direction not in {"yes", "1", "true"}:
                edges.add((b, a))
    used = sorted({node for edge in edges for node in edge}, key=int)
    ids = {node: i for i, node in enumerate(used)}
    with Path(target).open("wb") as file:
        file.write(struct.pack(">I4dII", 0x4F545731, south, west, north, east, len(used), len(edges)))
        for node in used:
            file.write(struct.pack(">ii", *nodes[node]))
        for a, b in sorted((ids[a], ids[b]) for a, b in edges):
            file.write(struct.pack(">II", a, b))
    print(f"{len(used)} nodes, {len(edges)} directed edges")


if __name__ == "__main__":
    build(*sys.argv[1:])
