#!/usr/bin/env bash
# Uploads PR screenshots to the public assets bucket (served via CloudFront) and prints markdown.
#
# Usage:
#   dwm/scripts/upload-pr-screenshots.sh <pr-number> [dir|file ...]
#       Local mode: uploads with your own AWS credentials (`aws s3 cp`).
#   dwm/scripts/upload-pr-screenshots.sh --url-map <file> <pr-number> [dir|file ...]
#       Cloud-session mode: <file> has one `<basename>=<presigned PUT URL>` per line
#       (minted with the AWS MCP `get_presigned_url`); uploads with curl, no AWS creds needed.
#   dwm/scripts/upload-pr-screenshots.sh --plan <pr-number> [dir|file ...]
#       Prints the S3 object keys that would be used (to mint presigned URLs) and exits.
#
# Defaults to the client GameTest screenshots directory. Object keys are
#   dwm-repo/pr-screenshots/pr-<N>/<random>/<name>.png
# The random segment is stable per invocation of --plan/--url-map via DWM_UPLOAD_ID.
#
# Environment:
#   DWM_ASSETS_BUCKET    (default adam-public-dev-assets)
#   DWM_ASSETS_REGION    (default eu-west-2)
#   DWM_ASSETS_BASE_URL  CloudFront base URL, e.g. https://dxxxx.cloudfront.net (required to print markdown)
#   DWM_UPLOAD_ID        random path segment; generated if unset
set -euo pipefail

DWM_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUCKET="${DWM_ASSETS_BUCKET:-adam-public-dev-assets}"
REGION="${DWM_ASSETS_REGION:-eu-west-2}"
BASE_URL="${DWM_ASSETS_BASE_URL:-}"
UPLOAD_ID="${DWM_UPLOAD_ID:-$(od -An -N4 -tx1 /dev/urandom | tr -d ' \n')}"
PREFIX="dwm-repo/pr-screenshots"

url_map=""
plan_only=0
while [[ $# -gt 0 ]]; do
  case "$1" in
    --url-map) url_map="${2:?--url-map needs a file}"; shift 2 ;;
    --plan) plan_only=1; shift ;;
    -h|--help) sed -n '2,22p' "${BASH_SOURCE[0]}"; exit 0 ;;
    *) break ;;
  esac
done

pr="${1:?usage: $0 [--url-map FILE|--plan] <pr-number> [dir|file ...]}"
shift
[[ "$pr" =~ ^[0-9]+$ ]] || { echo "pr-number must be numeric: $pr" >&2; exit 2; }

inputs=("$@")
if [[ ${#inputs[@]} -eq 0 ]]; then
  inputs=("$DWM_DIR/build/run/clientGameTest/screenshots")
fi

files=()
for item in "${inputs[@]}"; do
  if [[ -d "$item" ]]; then
    while IFS= read -r f; do files+=("$f"); done < <(find "$item" -type f -name '*.png' | sort)
  elif [[ -f "$item" ]]; then
    files+=("$item")
  else
    echo "not found: $item" >&2; exit 2
  fi
done
[[ ${#files[@]} -gt 0 ]] || { echo "no PNG files found" >&2; exit 2; }

key_for() { echo "$PREFIX/pr-$pr/$UPLOAD_ID/$(basename "$1")"; }

if [[ $plan_only -eq 1 ]]; then
  echo "DWM_UPLOAD_ID=$UPLOAD_ID"
  for f in "${files[@]}"; do echo "$(basename "$f")=s3://$BUCKET/$(key_for "$f")"; done
  exit 0
fi

for f in "${files[@]}"; do
  key="$(key_for "$f")"
  if [[ -n "$url_map" ]]; then
    url="$(grep -m1 "^$(basename "$f")=" "$url_map" | cut -d= -f2- || true)"
    [[ -n "$url" ]] || { echo "no presigned URL for $(basename "$f") in $url_map" >&2; exit 2; }
    curl -fsS -X PUT -H 'Content-Type: image/png' --upload-file "$f" "$url"
  else
    aws s3 cp "$f" "s3://$BUCKET/$key" --region "$REGION" --content-type image/png --only-show-errors
  fi
  echo "uploaded $key" >&2
done

if [[ -z "$BASE_URL" ]]; then
  echo "DWM_ASSETS_BASE_URL not set; objects uploaded but no markdown printed." >&2
  exit 0
fi
for f in "${files[@]}"; do
  name="$(basename "$f" .png)"
  echo "![$name](${BASE_URL%/}/$(key_for "$f"))"
done
