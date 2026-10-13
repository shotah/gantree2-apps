#!/usr/bin/env bash
# gradle-wrapper.jar is fetched from the official distribution, not committed.
set -euo pipefail

root="$(cd "$(dirname "$0")/../.." && pwd)"
repo="$(cd "$root/.." && pwd)"
jar="$root/gradle/wrapper/gradle-wrapper.jar"
script="$root/scripts/wrapper-jar.sh"
inner_sha=3b8a25775a69158b5ad2b1d17a80a88dc7b40a352a73eb27d06c612a7ce68e98

grep -q 'scripts/wrapper-jar.sh' "$root/gradlew" || {
  echo "FAIL: gradlew should pull the wrapper jar" >&2
  exit 1
}
grep -q 'scripts/wrapper-jar.sh' "$root/gradlew.bat" || {
  echo "FAIL: gradlew.bat should pull the wrapper jar" >&2
  exit 1
}
grep -qx 'gantree-android/gradle/wrapper/gradle-wrapper.jar' "$repo/.gitignore" || {
  echo "FAIL: wrapper jar should be gitignored" >&2
  exit 1
}

backup="$(mktemp)"
restore() {
  if [[ -s "$backup" && ! -f "$jar" ]]; then
    cp "$backup" "$jar"
  fi
  rm -f "$backup"
}
trap restore EXIT

if [[ -f "$jar" ]]; then
  cp "$jar" "$backup"
fi
rm -f "$jar"

"$script"
got="$(sha256sum "$jar" | awk 'NR==1 { print $1 }')"
if [[ "$got" != "$inner_sha" ]]; then
  echo "FAIL: pulled wrapper jar sha256 is ${got}" >&2
  exit 1
fi

"$script"

# Leave the jar out of the tree so the tracked binary stays deleted.
rm -f "$jar"
: >"$backup"
echo "ok wrapper-jar"
