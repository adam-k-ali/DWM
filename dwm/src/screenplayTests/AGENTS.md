# Screenplay tests (agents)

This file applies to `src/screenplayTests/` — mod-owned **Screenplay** YAML scenarios only.
The harness lives in the sibling [`screenplay/`](../../../screenplay/) Gradle build (`modRuntimeOnly` via composite includeBuild). It is not bundled into the production mod jar.

## What this is
YAML scenarios drive the **real Minecraft client** via Screenplay. Primitives and the compiler ship in `screenplay/common`. Mod scenarios stay under `resources/tests/` (e.g. `placeAndOpenTardis.yaml`). Vanilla demos ship inside `screenplay/common` resources and are run from the Screenplay build.

## Read first
Also: [`screenplay/README.md`](../../../screenplay/README.md), [`screenplay/metadata/modrinth-body.md`](../../../screenplay/metadata/modrinth-body.md), and this directory’s `README.md`.

## Commands
- Run a scenario or suite: `./dwm/gradlew runScreenplay -Pscreenplay=<yaml-filename-stem>`
- Run all discovered suites + standalone `type: test` scenarios: `./dwm/gradlew runScreenplayTests`
- Properties: `-PscreenplayDisplay=display|xvfb`, `-PscreenplayTimeout=<seconds>`, optional `-PscreenplayBaselinesDir=<dir>` for screenshot compare, optional `-PscreenplayRecord=true|false` (or YAML `record: true`) for ffmpeg screen recording
- Outputs: `dwm/build/screenplay/report.xml`, `metrics.json`, `diagnostics.txt`, screenshots under `dwm/build/screenplay/run/screenshots/`, recordings under `dwm/build/screenplay/run/recordings/`
- Per-run archives from `runScreenplayTests`: `dwm/build/screenplay/results/<id>/`

## Harness unit tests
- Unit tests for the harness: `./screenplay/gradlew :common:test`

## Authoring rules
- Stable ID is the **filename stem**. Duplicate IDs across mod + Screenplay demo roots are rejected by `runScreenplayTests`.
- Prefer adding shared primitives upstream in `screenplay/common`; mod-only steps can use `ServiceLoader` registration.
- Keep scenarios deterministic (`createWorld` defaults include seed `"42"`).
- Optional `captureScreenshot.compare: true` diffs against CI baselines from green `main` (via `-PscreenplayBaselinesDir`); pixels within a fixed per-channel color epsilon (`24`) count as equal — do not commit PNG goldens. In-world shots may still need a modest `maxDiffPixels` for edge AA. Prefer `pressKey: f1` before compares so HUD / first-person hand chrome cannot dominate the budget.
- Suites (`type: suite`) share one client session via `before-all` / `before-each` / `after-each` / `after-all` hooks. Suite members that are listed under `tests:` are not also run standalone by `runScreenplayTests`.

## CI
- **CI** — `.github/workflows/scenario-tests.yml` runs `./dwm/gradlew runScreenplayTests` (and Screenplay library demos) with xvfb; PRs download main screenshot artifacts for compare.
