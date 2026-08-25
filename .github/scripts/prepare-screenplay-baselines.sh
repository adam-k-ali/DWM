#!/usr/bin/env bash
# Download the latest green main Screenplay Tests artifact and flatten PNGs for
# captureScreenshot compare baselines.
#
# Usage:
#   prepare-screenplay-baselines.sh <artifact-name-prefix> <output-dir>
#
# Example:
#   prepare-screenplay-baselines.sh screenplay-tests-dwm screenplay-baselines
#
# Prints the absolute baselines directory to stdout when prepared; prints nothing
# when no green main run / screenshots are available (cold start).
set -euo pipefail

ARTIFACT_PREFIX="${1:?artifact name prefix required}"
OUTPUT_DIR="${2:?output directory required}"
DOWNLOAD_DIR="${OUTPUT_DIR}.download"

rm -rf "${OUTPUT_DIR}" "${DOWNLOAD_DIR}"
mkdir -p "${DOWNLOAD_DIR}"

RUN_JSON="$(gh run list \
  --workflow=scenario-tests.yml \
  --branch=main \
  --status=success \
  --limit=1 \
  --json databaseId,url)"
RUN_ID="$(echo "${RUN_JSON}" | jq -r '.[0].databaseId // empty')"
RUN_URL="$(echo "${RUN_JSON}" | jq -r '.[0].url // empty')"

if [[ -z "${RUN_ID}" ]]; then
  echo "No successful main Screenplay Tests run found yet; screenshot compare baselines unset." >&2
  exit 0
fi

echo "Using main Screenplay baseline run #${RUN_ID} (${RUN_URL})" >&2
if ! gh run download "${RUN_ID}" -D "${DOWNLOAD_DIR}" -n "${ARTIFACT_PREFIX}" 2>/dev/null; then
  # Fall back to downloading the whole run and filtering by artifact folder name.
  if ! gh run download "${RUN_ID}" -D "${DOWNLOAD_DIR}"; then
    echo "Could not download main Screenplay artifacts; screenshot compare baselines unset." >&2
    exit 0
  fi
fi

mkdir -p "${OUTPUT_DIR}"
copied=0
while IFS= read -r -d '' png; do
  # Prefer run/screenshots and results/*/screenshots paths; still accept other PNGs
  # under the matching artifact tree so older layouts keep working.
  case "${png}" in
    *"/run/screenshots/"*|*"/results/"*"/screenshots/"*)
      ;;
    *)
      # Skip non-screenshot PNGs (icons, etc.) if any appear outside screenshots dirs.
      if [[ "${png}" != *"/screenshots/"* ]]; then
        continue
      fi
      ;;
  esac
  # When the whole run was downloaded, keep only the requested artifact prefix tree.
  if [[ "${png}" != *"${ARTIFACT_PREFIX}"* && -d "${DOWNLOAD_DIR}/${ARTIFACT_PREFIX}" ]]; then
    continue
  fi
  if [[ -d "${DOWNLOAD_DIR}/${ARTIFACT_PREFIX}" && "${png}" != "${DOWNLOAD_DIR}/${ARTIFACT_PREFIX}"* ]]; then
    continue
  fi
  name="$(basename "${png}")"
  if [[ -f "${OUTPUT_DIR}/${name}" ]]; then
    echo "Warning: duplicate baseline filename '${name}' (last wins)" >&2
  fi
  cp -f "${png}" "${OUTPUT_DIR}/${name}"
  copied=$((copied + 1))
done < <(find "${DOWNLOAD_DIR}" -type f -name '*.png' -print0 | sort -z)

if [[ "${copied}" -eq 0 ]]; then
  echo "No screenshot PNGs found in main artifact '${ARTIFACT_PREFIX}'; baselines unset." >&2
  rm -rf "${OUTPUT_DIR}"
  exit 0
fi

echo "Prepared ${copied} screenshot baseline(s) under ${OUTPUT_DIR}" >&2
# Absolute path for Gradle -PscreenplayBaselinesDir
python3 - <<'PY' "${OUTPUT_DIR}"
import os, sys
print(os.path.abspath(sys.argv[1]))
PY
