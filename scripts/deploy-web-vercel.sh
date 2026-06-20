#!/usr/bin/env bash
# Deploy the verified wasm dist to Vercel.
# Requires VERCEL_TOKEN. Optionally VERCEL_ORG_ID and VERCEL_PROJECT_ID.
#
# Usage:
#   ./scripts/verify-web-deploy.sh --ci
#   VERCEL_TOKEN=... VERCEL_ORG_ID=... VERCEL_PROJECT_ID=... ./scripts/deploy-web-vercel.sh
#   ./scripts/deploy-web-vercel.sh --preview   # non-production deploy
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST="$ROOT/webApp/build/dist/wasmJs/productionExecutable"
PREVIEW=false

for arg in "$@"; do
  case "$arg" in
    --preview) PREVIEW=true ;;
    -h|--help)
      echo "Usage: $0 [--preview]"
      exit 0
      ;;
    *)
      echo "Unknown argument: $arg" >&2
      exit 1
      ;;
  esac
done

if [[ ! -f "$DIST/index.html" || ! -f "$DIST/markor.js" ]]; then
  echo "Dist not ready. Run ./scripts/verify-web-deploy.sh first." >&2
  exit 1
fi

: "${VERCEL_TOKEN:?Set VERCEL_TOKEN (create at https://vercel.com/account/tokens)}"

cd "$DIST"

args=(deploy --yes --token "$VERCEL_TOKEN")
if [[ -n "${VERCEL_ORG_ID:-}" ]]; then
  export VERCEL_ORG_ID
fi
if [[ -n "${VERCEL_PROJECT_ID:-}" ]]; then
  export VERCEL_PROJECT_ID
fi
if [[ "$PREVIEW" == false ]]; then
  args+=(--prod)
fi

echo "==> Deploying $(basename "$DIST") to Vercel"
vercel "${args[@]}"