# AGENTS.md

## Scope
This file applies to `src/screenplayTests/` — mod-owned **Screenplay** YAML scenarios only.
The harness lives in the `screenplay-*` Gradle modules (`screenplay-fabric` on the client run classpath). It is not bundled into the production mod jar.

## Local Context
YAML scenarios drive the **real Minecraft client** via Screenplay. Primitives and the compiler ship in `screenplay-common`. Mod scenarios stay under `resources/tests/` (e.g. `placeAndOpenTardis.yaml`). Vanilla demos ship inside `screenplay-common` resources.

Full product docs: `screenplay-fabric/README.md` and `metadata/screenplay/modrinth-body.md`.
Step/selector reference for authors: this directory’s `README.md`.

## Commands
- Fabric: `./gradlew runScreenplay -Pscreenplay=<yaml-filename-stem>` / `./gradlew runScreenplayTests`
- Forge: `xvfb-run -a ./gradlew -p dwm-loaders :forge:runScreenplay` / `:forge:runScreenplayTests`
- NeoForge: `xvfb-run -a ./gradlew -p dwm-loaders :neoforge:executeScreenplay` / `:neoforge:runScreenplayTests`
- Override step timeout (seconds): `-PscreenplayTimeout=60`
- Display mode: `-PscreenplayDisplay=display|xvfb` (default `display`). Forge/NeoForge lack Loom `useXvfb` — wrap Gradle with `xvfb-run -a` so `$DISPLAY` is set.
- Reports: `build/screenplay/` (Fabric) or `dwm-loaders/<loader>/build/screenplay/`
- Unit tests for the harness: `./gradlew :screenplay-common:test`

## Conventions
- YAML files live recursively under `resources/tests/`. Role is set by frontmatter `type`: `test` (runnable) or `command` (composite).
- Stable ID is the **filename stem**. Duplicate IDs across mod + Screenplay demo roots are rejected by `runScreenplayTests`.
- Prefer adding shared primitives upstream in `screenplay-common`; mod-only steps can use `ServiceLoader` registration.
- Selectors match exact widget `name` + `type` (`button`, `cycle`, `tab`, `editbox`, `label`, `screen`).

## Common Pitfalls
- **Display required** — use `-PscreenplayDisplay=xvfb` for headless/CI Linux runs.
- **CI** — `.github/workflows/scenario-tests.yml` runs Screenplay on Fabric, Forge, and NeoForge under xvfb (perf compare remains Fabric-primary).
- Each run deletes saved worlds under `build/screenplay/run/saves` and clears `build/screenplay/vanilla-server/world`.
- Quote relative coords in YAML: `"~"` not bare `~` (YAML null).
