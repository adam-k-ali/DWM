# AGENTS.md

## Scope
This file applies to `src/scenarioTest/` — DWM-owned **Sightline** YAML scenarios only.
The harness lives in the `sightline-*` Gradle modules (`sightline-fabric` on the client run classpath). It is not bundled into the production DWM jar.

## Local Context
YAML scenarios drive the **real Minecraft client** via Sightline. Primitives and the compiler ship in `sightline-common`. DWM scenarios stay under `resources/tests/` (e.g. `placeAndOpenTardis.yaml`). Vanilla demos ship inside `sightline-common` resources.

Full product docs: `sightline-fabric/README.md` and `metadata/sightline/modrinth-body.md`.
Step/selector reference for authors: this directory’s `README.md`.

## Commands
- Run a scenario: `./gradlew runSightline -Psightline=<yaml-filename-stem>`
- Run all discovered `type: test` scenarios: `./gradlew runSightlineTests`
- Override step timeout (seconds): `-PsightlineTimeout=60`
- Display mode: `-PsightlineDisplay=display|xvfb` (default `display`)
- Reports: `build/sightline/report.xml`, `build/sightline/metrics.json`, `build/sightline/diagnostics.txt`
- Per-scenario archives from `runSightlineTests`: `build/sightline/results/<id>/`
- Screenshots: `build/sightline/run/screenshots/`
- Vanilla server harness dir: `build/sightline/vanilla-server/`
- Unit tests for the harness: `./gradlew :sightline-common:test`

## Conventions
- YAML files live recursively under `resources/tests/`. Role is set by frontmatter `type`: `test` (runnable) or `command` (composite).
- Stable ID is the **filename stem**. Duplicate IDs across DWM + Sightline demo roots are rejected by `runSightlineTests`.
- Prefer adding shared primitives upstream in `sightline-common`; DWM-only steps can use `ServiceLoader` registration.
- Selectors match exact widget `name` + `type` (`button`, `cycle`, `tab`, `editbox`, `label`, `screen`).

## Common Pitfalls
- **Display required** — use `-PsightlineDisplay=xvfb` for headless/CI Linux runs.
- **CI** — `.github/workflows/scenario-tests.yml` runs `./gradlew runSightlineTests` with xvfb.
- Each run deletes saved worlds under `build/sightline/run/saves` and clears `build/sightline/vanilla-server/world`.
- Quote relative coords in YAML: `"~"` not bare `~` (YAML null).
