# AGENTS.md

## Project Snapshot
- Project type: Minecraft multi-loader mod (Fabric primary via `fabric-loom`; Forge/NeoForge via `dwm-loaders` included build).
- Language/runtime baseline: Java 25.
- Current target stack is defined in `gradle.properties` and `build.gradle` (Minecraft, Yarn mappings, Fabric Loader, Fabric API, Forge, NeoForge).
- Objective: maximize safe, repeatable AI-agent-driven development with strong automated verification.

## Repository Map
- `dwm-common/`: Shared gameplay + client render (loader-neutral; talks through `DwmPlatform` / `DwmClientPlatform`).
- Root project: Fabric adapters (`platform/fabric`), entrypoints, datagen, GameTests, Screenplay Fabric runs; embeds `dwm-common` sources.
- `dwm-loaders/`: Included build for `dwm-forge` and `dwm-neoforge` (isolated from Loom); embeds `dwm-common`.
- `src/main/java` / `src/client/java`: Fabric-only adapters, datagen, GameTests (shared code lives under `dwm-common`).
- `src/test/java`: JUnit 5 unit tests and scenario compiler/primitive tests.
- `src/screenplayTests/`: Mod-owned Screenplay YAML scenarios (resources only; not shipped in the mod jar).
- `src/main/resources`: Fabric metadata (`fabric.mod.json`, mixins, access widener).
- `src/main/generated/`: Datagen output — commit intentional changes; delete `.cache/` before commit.
- `dwm-common/src/*/resources`: Shared assets/data consumed by all loaders.
- `docs/`: Product-facing feature docs and release policy — read before changing player-visible behaviour.
- `tools/`: Offline Python scripts (TARDIS SFX generation/analysis); not part of Gradle build.
- `metadata/`: Modrinth listing (`modrinth.json`, `modrinth-body.md`).
- `screenplay-common` / `screenplay-fabric`: Screenplay real-client scenario library (Fabric path used by this mod).
- `screenplay-loaders/`: Included build for `screenplay-forge` and `screenplay-neoforge` (isolated from Loom).
- `screenplay-gradle-plugin`: Gradle plugin `com.adamkali.screenplay` (`runScreenplay`, `runScreenplayTests`).
- `metadata/screenplay/`: Screenplay Modrinth listing drafts (`modrinth.json`, `modrinth-body.md`).
- `.cursor/skills/`: Agent skills for GameTests, asset import, Blockbench models, MCP verify, etc.
- `version.json`: Release changelog and Modrinth/CurseForge promos — synced via `./gradlew syncVersionJson`.

## Agent-First Engineering Principles
- Prefer small, focused, reviewable diffs over broad rewrites.
- Keep behavior deterministic where practical (avoid hidden side effects and implicit global coupling).
- Design for testability first: clear boundaries, injectable collaborators, and pure logic extraction.
- Keep names and package placement consistent with existing project conventions.
- Use non-interactive, scriptable workflows and commands suitable for CI and autonomous agents.
- Do not make speculative refactors outside the requested scope.

## Working Rules For Agents
- Before coding, identify whether change is common, client-only, data-driven, or test-only.
- Preserve `main`/`client` source-set separation.
- Avoid touching large generated/resource surfaces unless the task requires it.
- When fixing bugs, prefer the smallest change that addresses root cause and add regression coverage.
- If a requested behavior cannot be fully automated/tested, document the limitation and propose follow-up automation.
- Product backlog lives on GitHub: issues in [adam-k-ali/DWM](https://github.com/adam-k-ali/DWM/issues) and the [DWM project board](https://github.com/users/adam-k-ali/projects/7/views/1). Titles keep stable IDs (`DWM-NNN — …`, `E-NNN — …`). Status is Project Status (`Backlog` / `In progress` / `Done`) plus issue state; priority is the project Priority field (`P0`–`P3`). Epics are parent issues with ticket sub-issues. A local `tickets/` folder may exist as a gitignored archive — do not use it as the live board.

## Fabric-Specific Development Rules
- Keep Java 25 compatibility.
- Favor Fabric API events/hooks/utilities before introducing Mixins.
- Use Mixins only when needed; keep them minimal and as targeted as possible.
- Maintain compatibility-minded behavior (avoid fragile assumptions about execution order or side effects).

## Networking & Side Safety
- Treat server as authoritative for gameplay/world state changes.
- Keep client-only classes out of common/server paths.
- Use side checks where required to prevent desync/crashes.
- For custom networking payloads:
  - Define payload ID/type/codec clearly.
  - Register payload types before handler registration/sending.
  - Validate all C2S payload inputs server-side before applying state changes.

## Data Generation Rules
- Use datagen for data-driven resources when appropriate.
- When changing data-generated content, run `./gradlew runDatagen` and include required generated outputs.
- Avoid committing unrelated generated churn.
- Keep datagen providers deterministic (same inputs should produce same outputs).

## Testability-First Design Rules
- New behavior should include automated tests by default.
- Bug fixes should include a regression test whenever feasible.
- Prefer unit tests for logic-heavy code and decision branches.
- Add integration/gameplay-focused tests when unit tests cannot prove behavior.
- If no automated test is added, explicitly state why and what test should be added later.

## Testing Expectations
- **Unit tests** (`./gradlew test`): JUnit 5 via `fabric-loader-junit` for logic, codecs, datagen helpers, and scenario compiler/primitives. Included in `./gradlew build` and CI.
- **GameTests** (`./gradlew runGametest`): Headless dedicated-server in-world tests (Fabric-only). Not run by CI today — run locally when GameTest code changes. See `.cursor/skills/fabric-gametest/SKILL.md`.
- **Screenplay tests** (real client YAML):
  - Fabric: `./gradlew runScreenplay -Pscreenplay=<id>` / `./gradlew runScreenplayTests`
  - Forge: `./gradlew -p dwm-loaders :forge:runScreenplayTests`
  - NeoForge: `./gradlew -p dwm-loaders :neoforge:runScreenplayTests` (entry task is `executeScreenplay` to avoid NeoGradle run-type clashes)
  - Requires a display; CI uses xvfb (`-PscreenplayDisplay=xvfb`). See `src/screenplayTests/AGENTS.md`.
- **Forge/NeoForge compile**: `./gradlew -p dwm-loaders :forge:compileJava :neoforge:compileJava`
- Keep test runs reproducible and suitable for unattended agent execution.

## Automation Pipeline For Agents
- During development:
  - Run targeted tests for changed scope when available.
- Before handoff/PR:
  - Run `./gradlew test`.
  - Run `./gradlew build` for full compile/package confidence on larger changes.
  - Run `./gradlew runDatagen` when data-driven content changed.
- Prefer fast feedback loops, but do not skip required quality gates.

## Build/Test/Validation
- Main automated commands:
  - `./gradlew test` — JUnit suite
  - `./gradlew build` — compile all source sets + JUnit (CI gate)
  - `./gradlew runDatagen` — regenerate `src/main/generated/` (only when datagen providers or promoted assets change); finalized by `pruneDatagenItemModels`
  - `./gradlew runGametest` — headless GameTests → `build/gametest/report.xml`
  - `./gradlew runScreenplay -Pscreenplay=<yaml-stem>` — client YAML scenarios → `build/screenplay/report.xml` (display required)
  - `./gradlew syncVersionJson` / `checkVersionSync` — keep `version.json` aligned with `gradle.properties`
- If a command fails, surface the failure clearly and fix root causes before handoff where possible.

## Releases / CI
- CI runs on GitHub Actions ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)); CircleCI is retired.
- Releases are intentional git tags `v{minecraft}-{mod}` — see [docs/release-policy.md](docs/release-policy.md).
- Do not bump `mod_version` or `version.json` promos except when cutting a release; use `./gradlew syncVersionJson` at cut time.
- The release workflow publishes the GitHub Release, Modrinth version, CurseForge file, and Discord `#releases` announcement from `version.json` (`summary` + changelog lists). Requires `MODRINTH_TOKEN`, `CURSEFORGE_TOKEN`, and `DISCORD_WEBHOOK_URL` secrets.
- Modrinth project listing fields live in `metadata/` (`modrinth.json` + `modrinth-body.md`); sync them with the manual **Sync Modrinth Project** workflow (`PROJECT_WRITE` on `MODRINTH_TOKEN`). CurseForge listing (description/categories) has no official PATCH API — edit by hand on CurseForge.

## Change Scope & Safety
- Keep unrelated files untouched.
- Do not change dependency versions/tooling unless the task requires it.
- Preserve backward compatibility assumptions unless the task explicitly allows breaking changes.
- Minimize risk in networking, persistence, and registry code by adding/adjusting tests first.

## Delivery Checklist
- Change is scoped and minimal.
- Source-set sidedness (`main` vs `client`) is preserved.
- Automated tests were added/updated when behavior changed.
- Required automation commands were run (or explicitly reported as not run).
- Datagen was run when relevant.
- Handoff notes include what changed, what was validated, and any follow-up automation tasks.

## Git Conventions

### Commits

Conventional Commits: `<type>(<scope>): <description>`
Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

Commits must be atomic - one change per commit.

### Pull requests

The default pull request description structure is defined in `.github/pull_request_template.md`. When creating or updating a pull request, write the **body** using that template: keep the three sections below in this order, replace placeholder bullets with concrete content, and do not leave a section empty — if nothing applies, use a single line `None`.

- **Why this matters** — business value / product reason, tightened for clarity
- **Proposed Implementation** — brief summary of what changed
- **Problems Encountered / Decisions Made** — deviations from the plan or issues that surfaced during implementation

### Pull request labels

After creating or updating a pull request, apply the appropriate label(s) using the `EditPullRequestLabels` tool:

- **`documentation`** — the PR contains changes to documentation only (e.g. `AGENTS.md`, `README.md`, inline code comments, or other docs)
- **`enhancement`** — the PR introduces a change in intended behaviour, an improvement to an existing feature, or a new feature
- **`bug`** — the PR fixes a defect / unintended behaviour

A single PR may carry more than one label if it touches multiple categories.

## Nested Context
- `src/screenplayTests/AGENTS.md` — YAML client scenario framework (primitives, composite commands, Gradle properties).
- `src/main/java/com/adamkali/dwm/tardis/AGENTS.md` — TARDIS domain layout (logic vs data vs interior vs portal rendering).
- `tools/AGENTS.md` — offline Python audio tooling and fixture rules.
- `.cursor/skills/fabric-gametest/SKILL.md` — authoring and registering Fabric GameTests.
- `.cursor/skills/asset-import-pipeline/SKILL.md` — promoting archive textures and wiring datagen.

## Cursor Cloud specific instructions

These notes are for agents running in the Cursor Cloud VM. The standard build/test/datagen commands are already documented above (`./gradlew build`, `./gradlew test`, `./gradlew runDatagen`); this section only captures non-obvious environment caveats.

- Java 25 is what Gradle targets (`./gradlew` picks up the system JDK; no `JAVA_HOME` tweaking needed). Local agents may need `JAVA_HOME` pointed at a JDK 25 install.
- The startup update script runs `./gradlew dependencies -q`, which resolves all configurations and lets Fabric Loom provision Minecraft, Yarn mappings, and remap the mod dependencies. The very first Loom configuration on a cold cache is slow and network-heavy (it decompiles Minecraft and remaps ~50 mods from `maven.fabricmc.net`, `maven.shedaniel.me`, `maven.terraformersmc.com`, and Maven Central); once cached, subsequent Gradle invocations are fast.
- Running the mod end-to-end without a display: use `./gradlew runGametest`. This boots a real headless Minecraft server, loads the mod, and executes the registered in-world Fabric GameTests (TARDIS door/interior flows and chameleon networking). It is the best headless smoke test of core gameplay.
- YAML client scenario tests (`./gradlew runScreenplay`) boot a real Fabric client. In this headless VM use `-PscreenplayDisplay=xvfb` (requires `xvfb` / Mesa); `-PscreenplayDisplay=display` needs a real `$DISPLAY`. See `src/screenplayTests/README.md`. Prefer `runGametest` for server-side gameplay smoke tests.
- Forge/NeoForge compile: `./gradlew -p dwm-loaders :forge:compileJava :neoforge:compileJava`. Screenplay on those loaders: `./gradlew -p dwm-loaders :forge:runScreenplayTests` / `:neoforge:runScreenplayTests` with `-PscreenplayDisplay=xvfb`.
- `./gradlew runClient` and `./gradlew runServer` start the actual game; `runClient` needs a GUI/display and will not work in the headless VM. Prefer `runGametest` for automated verification.
- `./gradlew build` also compiles the `client` source set and runs the full JUnit suite, so a green `build` covers both compile and unit-test confidence.
- `./gradlew runDatagen` writes generated resources under `src/main/generated/` and also leaves an untracked `src/main/generated/.cache/` directory — delete that `.cache` dir before committing to avoid stray churn.