#!/usr/bin/env bash
# Repo-root Gradle shim — the DWM Gradle wrapper lives under dwm/.
# There is no shared task namespace at the repository root.
set -euo pipefail

cat >&2 <<'EOF'
Use the DWM Gradle wrapper:

  ./dwm/gradlew <task>

Examples:
  ./dwm/gradlew runClient
  ./dwm/gradlew test
  ./dwm/gradlew build

See README.md and AGENTS.md.
EOF
exit 1
