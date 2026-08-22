# AGENTS.md

## Scope
This file applies to `src/scenarioTest/` — a separate Loom source set and Fabric mod (`dwm-scenario-test`) that is **not** bundled into the production mod jar.

## Local Context
YAML scenarios drive the **real Minecraft client** by dispatching widget clicks, keyboard input, and in-world actions. The compiler (`ScenarioCompiler`) validates documents at load time; primitives live under `com.adamkali.dwm.scenariotest.primitive`. Unit tests for compiler/primitives are in `src/test/java/.../scenariotest/` and run with `./gradlew test`.

Full step/selector reference: `README.md` in this directory.

## Commands
- Run a scenario: `./gradlew runScenarioTest -Pscenario=<yaml-filename-stem>`
- Override step timeout (seconds): `-PscenarioTimeout=60`
- Reports: `build/scenario-test/report.xml`, `build/scenario-test/diagnostics.txt`
- Screenshots: `build/scenario-test/run/screenshots/`
- Vanilla server harness dir: `build/scenario-test/vanilla-server/`

## Conventions
- YAML files live recursively under `resources/tests/`. Role is set by frontmatter `type`: `test` (runnable) or `command` (composite).
- Stable ID is the **filename stem** (e.g. `subflows/assertAndClick.yaml` → `assertAndClick`). Duplicate IDs across directories are rejected.
- Add new behaviour as a `ScenarioPrimitive` in `primitive/`, register it in `ScenarioPrimitives`, and add JUnit coverage in `src/test/java/.../scenariotest/`.
- Composite commands use `{{ parameter }}` templating; cycles and unknown steps fail at compile time.
- Selectors match exact widget `name` + `type` (`button`, `cycle`, `tab`, `editbox`, `label`, `screen`).

## Common Pitfalls
- **Display required** — unlike GameTests, this harness boots the client; it will not run headless in Cursor Cloud.
- **Not in CI** — `./gradlew build` compiles this source set but does not execute scenarios.
- Each run deletes saved worlds under `build/scenario-test/run/saves` and clears `build/scenario-test/vanilla-server/world`.
- `startVanillaServer` and `createWorld` use a 120s timeout floor; `-PscenarioTimeout` only raises it.
- `runCommand` sends packets immediately — pair with `waitUntil` for inventory/world assertions.
- Quote relative coords in YAML: `"~"` not bare `~` (YAML null).
