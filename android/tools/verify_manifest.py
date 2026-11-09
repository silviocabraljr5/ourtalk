"""Check release security properties on the *merged* Android manifest."""

import sys
from pathlib import Path
from xml.etree import ElementTree


ANDROID = "{http://schemas.android.com/apk/res/android}"
ALLOWED_PERMISSIONS = {
    "ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION", "ACCESS_WIFI_STATE",
    "CHANGE_WIFI_STATE", "BLUETOOTH", "BLUETOOTH_ADMIN", "BLUETOOTH_SCAN",
    "BLUETOOTH_CONNECT", "BLUETOOTH_ADVERTISE", "NEARBY_WIFI_DEVICES",
    "ACCESS_NETWORK_STATE", "INTERNET",
}
ALLOWED_EXPORTS = {
    ("activity", "br.com.ourtalk.app.MainActivity", None),
    ("receiver", "androidx.profileinstaller.ProfileInstallReceiver", "android.permission.DUMP"),
}
PLAY_REFERRER_PERMISSION = "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE"


def check_manifest(path: Path) -> None:
    root = ElementTree.parse(path).getroot()
    package = root.attrib.get("package")
    app = root.find("application")
    failures = []
    if package != "br.com.ourtalk.app" or app is None:
        raise SystemExit("Expected the merged production manifest for br.com.ourtalk.app")
    for name, value in {"allowBackup": "false", "usesCleartextTraffic": "false"}.items():
        if app.get(ANDROID + name) != value:
            failures.append(f"Expected {name}={value}")
    if app.get(ANDROID + "debuggable", "false") != "false":
        failures.append("Production is debuggable")
    for permission in root.findall("uses-permission") + root.findall("uses-permission-sdk-23"):
        name = permission.get(ANDROID + "name", "")
        if name not in {f"android.permission.{value}" for value in ALLOWED_PERMISSIONS} | {f"{package}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION", PLAY_REFERRER_PERMISSION}:
            failures.append(f"Unexpected permission: {name}")
    for component in app:
        name = component.get(ANDROID + "name", "")
        if ".exposurenotification." in name:
            failures.append(f"Unneeded Exposure Notifications component: {name}")
        if component.get(ANDROID + "exported") == "true":
            entry = (component.tag, name, component.get(ANDROID + "permission"))
            if entry not in ALLOWED_EXPORTS:
                failures.append(f"Unexpected exported component: {entry}")
    if failures:
        raise SystemExit("\n".join(failures))
    print("Merged release manifest verified: permissions, exported components, backup, cleartext and debug flags")


if __name__ == "__main__":
    check_manifest(Path(sys.argv[1]))
