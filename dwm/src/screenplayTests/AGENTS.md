# Screenplay tests (agents)

This file applies to `src/screenplayTests/` — mod-owned **Screenplay** YAML scenarios only.
The harness lives in the sibling [`screenplay/`](../../../screenplay/) Gradle build (`modRuntimeOnly` via composite includeBuild). It is not bundled into the production mod jar.

## What this is
YAML scenarios drive the **real Minecraft client** via Screenplay. Primitives and the compiler ship in `screenplay/common`. Mod scenarios stay under `resources/tests/` (e.g. `placeAndOpenTardis.yaml`). Vanilla demos ship inside `screenplay/common` resources and are run from the Screenplay build.

## Read first
Also: [`screenplay/README.md`](../../../screenplay/README.md), [`screenplay/metadata/modrinth-body.md`](../../../screenplay/metadata/modrinth-body.md), and this directory’s `README.md`.

## Commands
- Run a scenario: `./dwm/gradlew runScreenplay -Pscreenplay=<yaml-filename-stem>`
- Run all discovered `type: test` scenarios: `./dwm/gradlew runScreenplayTests`
- Properties: `-PscreenplayDisplay=display|xvfb`, `-PscreenplayTimeout=<seconds>`
- Outputs: `dwm/build/screenplay/report.xml`, `metrics.json`, `diagnostics.txt`, screenshots under `dwm/build/screenplay/run/screenshots/`
- Per-scenario archives from `runScreenplayTests`: `dwm/build/screenplay/results/<id>/`

## Harness unit tests
- Unit tests for the harness: `./screenplay/gradlew :common:test`

## Authoring rules
- Stable ID is the **filename stem**. Duplicate IDs across mod + Screenplay demo roots are rejected by `runScreenplayTests`.
- Prefer adding shared primitives upstream in `screenplay/common`; mod-only steps can use `ServiceLoader` registration.
- Keep scenarios deterministic (fixed seeds / options where the harness supports them).

## CI
- **CI** — `.github/workflows/scenario-tests.yml` runs `./dwm/gradlew runScreenplayTests` (and Screenplay library demos) with xvfb.
