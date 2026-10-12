#!/usr/bin/env bash
# Home-screen glyph is the shared gantree icon set, not a single 1024 universal image.
set -euo pipefail

root="$(cd "$(dirname "$0")/../.." && pwd)"
logo="$root/assets/logo.svg"
banner="$root/assets/banner.svg"
iconset="$root/app/Helm/Assets.xcassets/AppIcon.appiconset"
proj="$root/app/Helm.xcodeproj/project.pbxproj"
script="$root/scripts/app-icon.sh"
src="$root/../shared/brand/ios"

[[ -x "$script" ]] || {
  echo "FAIL: scripts/app-icon.sh should be executable" >&2
  exit 1
}

grep -Fq 'aria-label="gantree"' "$logo" || {
  echo "FAIL: assets/logo.svg should be the gantree mark" >&2
  exit 1
}
grep -Fq 'aria-label=' "$banner" || {
  echo "FAIL: assets/banner.svg missing" >&2
  exit 1
}

"$script"
"$script"

[[ ! -f "$iconset/AppIcon.png" ]] || {
  echo "FAIL: unsized AppIcon.png should be replaced by the sized set" >&2
  exit 1
}

python3 - "$iconset" "$src" <<'PY'
import json
import struct
import sys
from pathlib import Path

iconset = Path(sys.argv[1])
src = Path(sys.argv[2])
sizes = [1024, 180, 167, 152, 120, 87, 80, 76, 60, 58, 40, 29, 20]
for size in sizes:
    path = iconset / f"AppIcon-{size}.png"
    data = path.read_bytes()
    if data[:8] != b"\x89PNG\r\n\x1a\n" or data[12:16] != b"IHDR":
        sys.exit(f"FAIL: {path.name} is not a PNG")
    width, height = struct.unpack(">II", data[16:24])
    if (width, height) != (size, size):
        sys.exit(f"FAIL: {path.name} is {width}x{height}, want {size}x{size}")
    if data != (src / path.name).read_bytes():
        sys.exit(f"FAIL: {path.name} does not match {src / path.name}")

doc = json.loads((iconset / "Contents.json").read_text())
want = {
    ("iphone", "20x20", "2x", "AppIcon-40.png"),
    ("iphone", "20x20", "3x", "AppIcon-60.png"),
    ("iphone", "29x29", "2x", "AppIcon-58.png"),
    ("iphone", "29x29", "3x", "AppIcon-87.png"),
    ("iphone", "40x40", "2x", "AppIcon-80.png"),
    ("iphone", "40x40", "3x", "AppIcon-120.png"),
    ("iphone", "60x60", "2x", "AppIcon-120.png"),
    ("iphone", "60x60", "3x", "AppIcon-180.png"),
    ("ipad", "20x20", "1x", "AppIcon-20.png"),
    ("ipad", "20x20", "2x", "AppIcon-40.png"),
    ("ipad", "29x29", "1x", "AppIcon-29.png"),
    ("ipad", "29x29", "2x", "AppIcon-58.png"),
    ("ipad", "40x40", "1x", "AppIcon-40.png"),
    ("ipad", "40x40", "2x", "AppIcon-80.png"),
    ("ipad", "76x76", "1x", "AppIcon-76.png"),
    ("ipad", "76x76", "2x", "AppIcon-152.png"),
    ("ipad", "83.5x83.5", "2x", "AppIcon-167.png"),
    ("ios-marketing", "1024x1024", "1x", "AppIcon-1024.png"),
}
got = set()
for image in doc["images"]:
    got.add((image["idiom"], image["size"], image["scale"], image["filename"]))
if got != want:
    missing = sorted(want - got)
    extra = sorted(got - want)
    sys.exit(f"FAIL: Contents.json slots mismatch\n missing {missing}\n extra {extra}")
PY

count=$(grep -c 'ASSETCATALOG_COMPILER_APPICON_NAME = AppIcon;' "$proj" || true)
[[ "$count" -ge 2 ]] || {
  echo "FAIL: Debug and Release must set ASSETCATALOG_COMPILER_APPICON_NAME = AppIcon" >&2
  exit 1
}

grep -q 'Assets.xcassets in Resources' "$proj" || {
  echo "FAIL: Assets.xcassets must be in the Resources build phase" >&2
  exit 1
}

echo "ok app-icon"
