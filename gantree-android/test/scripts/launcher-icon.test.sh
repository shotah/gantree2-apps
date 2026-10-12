#!/usr/bin/env bash
# Adaptive foreground is the traced gantree mark. Raster fallbacks come from shared brand.
set -euo pipefail

root="$(cd "$(dirname "$0")/../.." && pwd)"
logo="$root/assets/logo.svg"
fg="$root/app/src/main/res/drawable/ic_launcher_foreground.xml"
icon="$root/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml"

grep -q 'aria-label="gantree"' "$logo" || {
  echo "FAIL: assets/logo.svg should be the gantree mark" >&2
  exit 1
}

# The traced path opens the same way in the SVG and in the vector drawable.
grep -q 'd="M7057 12760 c-24 -63 -80 -139' "$logo" || {
  echo "FAIL: assets/logo.svg lost the traced tree path" >&2
  exit 1
}
grep -q 'android:pathData="M7057 12760 c-24 -63 -80 -139' "$fg" || {
  echo "FAIL: ic_launcher_foreground.xml should carry the traced path from assets/logo.svg" >&2
  exit 1
}

grep -q 'fillColor="#F07848"' "$fg" || {
  echo "FAIL: launcher foreground should keep the accent dot" >&2
  exit 1
}

grep -q '<monochrome android:drawable="@drawable/ic_launcher_foreground"' "$icon" || {
  echo "FAIL: adaptive icon should declare a monochrome layer" >&2
  exit 1
}

"$root/scripts/launcher-icon.sh"
"$root/scripts/launcher-icon.sh"

src="$root/../shared/brand/android"
for dpi in mdpi hdpi xhdpi xxhdpi xxxhdpi; do
  for name in ic_launcher.png ic_launcher_round.png; do
    cmp -s "$src/mipmap-$dpi/ic_launcher.png" "$root/app/src/main/res/mipmap-$dpi/$name" || {
      echo "FAIL: mipmap-$dpi/$name does not match shared brand" >&2
      exit 1
    }
  done
done

echo "ok launcher-icon"
