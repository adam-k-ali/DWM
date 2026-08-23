# AGENTS.md

## Scope
This file applies to `src/screenplayTests/` — mod-owned **Screenplay** YAML scenarios only.
The harness lives in the `screenplay-*` Gradle modules (`screenplay-fabric` on the client run classpath). It is not bundled into the production mod jar.

## Local Context
YAML scenarios drive the **real Minecraft client** via Screenplay. Primitives and the compiler ship in `screenplay-common`. Mod scenarios stay under `resources/tests/` (e.g. `placeAndOpenTardis.yaml`). Vanilla demos ship inside `screenplay-common` resources.

Full product docs: `screenplay-fabric/README.md` and `metadata/screenplay/modrinth-body.md`.
Step/selector reference for authors: this directory’s `README.md`.

## Commands
- Run a scenario: `./gradlew runScreenplay -Pscreenplay=<yaml-filename-stem>`
- Run all discovered `type: test` scenarios: `./gradlew runScreenplayTests`
- Override step timeout (seconds): `-PscreenplayTimeout=60`
- Display mode: `-PscreenplayDisplay=display|xvfb` (default `display`)
- Reports: `build/screenplay/report.xml`, `build/screenplay/metrics.json`, `build/screenplay/diagnostics.txt`
- Per-scenario archives from `runScreenplayTests`: `build/screenplay/results/<id>/`
- Screenshots: `build/screenplay/run/screenshots/`
- Vanilla server harness dir: `build/screenplay/vanilla-server/`
- Unit tests for the harness: `./gradlew :screenplay-common:test`

## Conventions
- YAML files live recursively under `resources/tests/`. Role is set by frontmatter `type`: `test` (runnable) or `command` (composite).
- Stable ID is the **filename stem**. Duplicate IDs across mod + Screenplay demo roots are rejected by `runScreenplayTests`.
- Prefer adding shared primitives upstream in `screenplay-common`; mod-only steps can use `ServiceLoader` registration.
- Selectors match exact widget `name` + `type` (`button`, `cycle`, `tab`, `editbox`, `label`, `screen`).

## Common Pitfalls
- **Display required** — use `-PscreenplayDisplay=xvfb` for headless/CI Linux runs.
- **CI** — `.github/workflows/scenario-tests.yml` runs `./gradlew runScreenplayTests` with xvfb.
- Each run deletes saved worlds under `build/screenplay/run/saves` and clears `build/screenplay/vanilla-server/world`.
- Quote relative coords in YAML: `"~"` not bare `~` (YAML null).
