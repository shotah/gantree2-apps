#!/usr/bin/env bash
# Pull gradle/wrapper/gradle-wrapper.jar from the official Gradle 9.8.1
# distribution. The jar is not committed (OpenSSF Scorecard binary-artifacts).
# The byte range is the local header plus deflated payload of
# gradle-9.8.1/lib/plugins/gradle-wrapper-main-9.8.1.jar inside
# gradle-9.8.1-bin.zip. The inner gradle-wrapper.jar sha256 is the pin.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
props="$root/gradle/wrapper/gradle-wrapper.properties"
jar="$root/gradle/wrapper/gradle-wrapper.jar"

inner_sha=3b8a25775a69158b5ad2b1d17a80a88dc7b40a352a73eb27d06c612a7ce68e98
range_sha=0cfd7d43cd280db4e148ea83f103d108294c9ae8008b2c3ef13a5f24e42d3b8a
outer_sha=3a514d263648e0b94ad3405c1018fb0f3e6133615b70569f9787ead69a95416d

if [[ -f "$jar" ]]; then
  got="$(sha256sum "$jar" | awk 'NR==1 { print $1 }')"
  if [[ "$got" == "$inner_sha" ]]; then
    exit 0
  fi
  echo "gradle-wrapper.jar sha256 is ${got}; replacing with the pinned jar" >&2
fi

if ! grep -qxF 'distributionUrl=https\://services.gradle.org/distributions/gradle-9.8.1-bin.zip' "$props"; then
  echo "scripts/wrapper-jar.sh pins gradle-9.8.1-bin.zip. Update the pin when distributionUrl changes." >&2
  exit 1
fi

command -v curl >/dev/null 2>&1 || {
  echo "curl is required to pull the Gradle wrapper jar" >&2
  exit 1
}
command -v python3 >/dev/null 2>&1 || {
  echo "python3 is required to unpack the Gradle wrapper jar" >&2
  exit 1
}

tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

curl -fsSL --retry 3 -r 107813077-107861939 \
  -o "$tmp/slice.bin" \
  "https://services.gradle.org/distributions/gradle-9.8.1-bin.zip"

got="$(sha256sum "$tmp/slice.bin" | awk 'NR==1 { print $1 }')"
if [[ "$got" != "$range_sha" ]]; then
  echo "Gradle distribution slice sha256 is ${got}; expected the pinned range" >&2
  exit 1
fi

INNER_SHA="$inner_sha" OUTER_SHA="$outer_sha" python3 - "$tmp/slice.bin" "$tmp/gradle-wrapper.jar" <<'PY'
import hashlib
import io
import os
import struct
import sys
import zipfile
import zlib

blob = open(sys.argv[1], "rb").read()
sig, _ver, _flag, method, _time, _date, _crc, csize, _usize, namelen, extralen = struct.unpack_from(
    "<IHHHHHIIIHH", blob
)
if sig != 0x04034B50 or method != 8:
    sys.exit("Gradle distribution slice is not a deflated zip entry")
name = blob[30 : 30 + namelen]
if name != b"gradle-9.8.1/lib/plugins/gradle-wrapper-main-9.8.1.jar":
    sys.exit("Gradle distribution slice is not gradle-wrapper-main")
start = 30 + namelen + extralen
raw = zlib.decompress(blob[start : start + csize], -15)
outer = hashlib.sha256(raw).hexdigest()
if outer != os.environ["OUTER_SHA"]:
    sys.exit(f"gradle-wrapper-main sha256 is {outer}")
inner = zipfile.ZipFile(io.BytesIO(raw)).read("gradle-wrapper.jar")
got = hashlib.sha256(inner).hexdigest()
if got != os.environ["INNER_SHA"]:
    sys.exit(f"gradle-wrapper.jar sha256 is {got}")
open(sys.argv[2], "wb").write(inner)
PY

mv "$tmp/gradle-wrapper.jar" "$jar"
trap - EXIT
rm -rf "$tmp"
