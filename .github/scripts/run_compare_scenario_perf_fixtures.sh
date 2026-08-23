#!/usr/bin/env bash
# Fixture checks for compare-scenario-perf.py (advisory; always expects exit 0).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
SCRIPT="$ROOT/compare-scenario-perf.py"
DATA="$ROOT/testdata/scenario-perf"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT

run_case() {
  local name="$1"
  local expect="$2"
  python3 "$SCRIPT" \
    --current-dir "$DATA/$name/current" \
    --baseline-dir "$DATA/$name/baseline" \
    --markdown-out "$OUT/$name.md" \
    --baseline-label "fixture:$name" >/dev/null
  local status
  status="$(grep -E '^\| placeBlock' "$OUT/$name.md" | awk -F'|' '{gsub(/^ +| +$/,"",$6); print $6}')"
  if [[ "$status" != "$expect" ]]; then
    echo "FAIL $name: expected status '$expect', got '$status'" >&2
    cat "$OUT/$name.md" >&2
    exit 1
  fi
  if ! grep -q '<!-- dwm-scenario-perf -->' "$OUT/$name.md"; then
    echo "FAIL $name: missing comment marker" >&2
    exit 1
  fi
  echo "OK $name ($expect)"
}

run_case ok OK
run_case regressed REGRESSED
run_case no-baseline "NO BASELINE"
run_case missing MISSING

# REGRESSED case must still exit 0 (re-run capturing code).
set +e
python3 "$SCRIPT" \
  --current-dir "$DATA/regressed/current" \
  --baseline-dir "$DATA/regressed/baseline" \
  --markdown-out "$OUT/regressed-exit.md" >/dev/null
code=$?
set -e
if [[ "$code" -ne 0 ]]; then
  echo "FAIL regressed exit code: expected 0, got $code" >&2
  exit 1
fi
echo "OK regressed exit code 0"

if ! grep -q 'waitUntil block' "$OUT/regressed.md"; then
  echo "FAIL regressed: expected step regression details" >&2
  exit 1
fi
echo "OK regressed step details"

echo "All compare-scenario-perf fixtures passed."
