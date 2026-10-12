#!/usr/bin/env bash
# Copy shared launcher PNGs into mipmap-* (ic_launcher + ic_launcher_round).
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
src="$root/../shared/brand/android"
dst="$root/app/src/main/res"

for dpi in mdpi hdpi xhdpi xxhdpi xxxhdpi; do
  from="$src/mipmap-$dpi/ic_launcher.png"
  test -f "$from"
  mkdir -p "$dst/mipmap-$dpi"
  cp "$from" "$dst/mipmap-$dpi/ic_launcher.png"
  cp "$from" "$dst/mipmap-$dpi/ic_launcher_round.png"
done
