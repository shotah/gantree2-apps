#!/usr/bin/env bash
# .env (or process env) → app/Helm.local.xcconfig. Never commit the output.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
envfile="${HELM_BAKE_ENV:-$root/.env}"
out="${HELM_BAKE_OUT:-$root/app/Helm.local.xcconfig}"

if [[ -f "$envfile" ]]; then
  set -a
  # shellcheck disable=SC1090
  . "$envfile"
  set +a
fi

origin="${HELM_MAILBOX_ORIGIN:-}"
web="${HELM_GOOGLE_WEB_CLIENT_ID:-}"
ios="${HELM_GOOGLE_IOS_CLIENT_ID:-}"
reversed="${HELM_GOOGLE_REVERSED_CLIENT_ID:-}"

suffix=".apps.googleusercontent.com"
if [[ -z "$reversed" && -n "$ios" && "$ios" == *"$suffix" ]]; then
  reversed="com.googleusercontent.apps.${ios%"$suffix"}"
fi

# xcconfig starts a comment at `//`, so `https://host` would bake as `https:`.
# An empty `$()` between the slashes keeps the whole origin.
split_slashes='/$()/'
xcconfig_origin="${origin//\/\//$split_slashes}"

mkdir -p "$(dirname "$out")"
cat > "$out" <<EOF
HELM_MAILBOX_ORIGIN = ${xcconfig_origin}
HELM_GOOGLE_WEB_CLIENT_ID = ${web}
HELM_GOOGLE_IOS_CLIENT_ID = ${ios}
HELM_GOOGLE_REVERSED_CLIENT_ID = ${reversed}
EOF
echo "wrote $out"
