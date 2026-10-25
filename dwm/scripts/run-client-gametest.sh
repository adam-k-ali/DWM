#!/usr/bin/env bash
# Runs the DWM client GameTests headlessly (Xvfb + software GL) and lists the screenshots.
# Not part of `build`/CI. Usage: dwm/scripts/run-client-gametest.sh [extra gradle args]
set -euo pipefail

DWM_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$DWM_DIR"

export LIBGL_ALWAYS_SOFTWARE=1
export ALSOFT_DRIVERS=null

status=0
xvfb-run -a -s "-screen 0 1280x720x24" ./gradlew runClientGameTest "$@" || status=$?

echo
echo "Screenshots:"
find "$DWM_DIR/build" -path '*screenshots*' -name '*.png' -printf '%T@ %p\n' 2>/dev/null | sort -rn | cut -d' ' -f2- || true
exit "$status"
