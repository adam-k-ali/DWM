#!/usr/bin/env bash
# Repo-root Gradle shim — this monorepo has two sibling Gradle builds.
# There is no shared task namespace at the repository root.
set -euo pipefail

cat >&2 <<'EOF'
This repository is a monorepo with separate Gradle wrappers:

  ./dwm/gradlew <task>          # The Doctor Who Mod (Fabric)
  ./screenplay/gradlew <task>   # Screenplay harness (Fabric; loaders via -p loaders)

Examples:
  ./dwm/gradlew runClient
  ./dwm/gradlew runScreenplay -Pscreenplay=<id>
  ./screenplay/gradlew runClient
  ./screenplay/gradlew runScreenplay -Pscreenplay=createWorld
  ./screenplay/gradlew -p loaders :forge:build :neoforge:build

See README.md and AGENTS.md.
EOF
exit 1
