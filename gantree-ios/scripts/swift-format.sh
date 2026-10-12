#!/usr/bin/env bash
# Apple swift-format, using the Swift 6.3.3 defaults in .swift-format.
# format — rewrite sources in place
# lint — strict diagnostics, then fail if format would still change a file
# Skips GantreeThemes.swift: generated symlink into ../shared/theme. Formatting
# it would rewrite that shared file.
set -euo pipefail

mode="${1:-lint}"
root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"

cfg="$root/.swift-format"

files=()
while IFS= read -r f; do
  files+=("$f")
done < <(find Sources Tests app -type f -name '*.swift' ! -name 'GantreeThemes.swift' | sort)
files+=(Package.swift)

case "$mode" in
  format)
    swift format format --in-place --parallel --configuration "$cfg" "${files[@]}"
    ;;
  lint)
    swift format lint --strict --parallel --configuration "$cfg" "${files[@]}"
    tmp="$(mktemp -d)"
    trap 'rm -rf "$tmp"' EXIT
    rel=()
    for f in "${files[@]}"; do
      mkdir -p "$tmp/$(dirname "$f")"
      cp -a "$f" "$tmp/$f"
      rel+=("$tmp/$f")
    done
    swift format format --in-place --parallel --configuration "$cfg" "${rel[@]}"
    for f in "${files[@]}"; do
      diff -u "$f" "$tmp/$f"
    done
    ;;
  *)
    echo "usage: swift-format.sh format|lint" >&2
    exit 2
    ;;
esac
