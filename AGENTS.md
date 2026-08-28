# AGENTS.md

## Project Snapshot
- Monorepo with **two sibling Gradle builds** (separate wrappers — task names never collide):
  - [`dwm/`](dwm/) — Minecraft Fabric mod *The Doctor Who Mod* (`./dwm/gradlew`)
  - [`screenplay/`](screenplay/) — Screenplay real-client scenario library (`./screenplay/gradlew`)
- Language/runtime baseline: Java 25.
- Shared version catalog: [`gradle/libs.versions.toml`](gradle/libs.versions.toml) (keep each build's `gradle.properties` aligned).
- Objective: maximize safe, repeatable AI-agent-driven development with strong automated verification.

## Repository Map
- `dwm/src/main/java`: Common logic safe on both logical client and logical server (`com.adamkali.dwm.*`).
- `dwm/src/client/java`: Client-only logic (rendering, HUD, client integration).
- `dwm/src/test/java`: JUnit 5 unit tests and scenario compiler/primitive tests for DWM.
- `dwm/src/screenplayTests/`: Mod-owned Screenplay YAML scenarios (loaded from disk; not shipped in the mod jar).
- `dwm/src/main/resources`: Common resources (`fabric.mod.json`, data, tags, recipes, lang, worldgen).
- `dwm/src/main/generated/`: Datagen output — commit intentional changes; delete `.cache/` before commit.
- `dwm/src/client/resources`: Hand-maintained client assets (models, blockstates, textures, sounds).
- `dwm/docs/`: Product-facing feature docs and release policy — read before changing player-visible behaviour.
- `dwm/tools/`: Offline Python scripts (TARDIS SFX generation/analysis); not part of Gradle build.
- `dwm/metadata/`: Modrinth listing for DWM (`modrinth.json`, `modrinth-body.md`).
- `dwm/version.json`: DWM release changelog and Modrinth/CurseForge promos — synced via `./dwm/gradlew syncVersionJson`.
- `screenplay/`: Fabric Screenplay mod root (`screenplay-fabric` artifact); `common/`, `gradle-plugin/`, `loaders/`, `docs/`, `metadata/`.
- `screenplay/common`: Shared YAML compiler, runner, primitives (unit-tested here).
- `screenplay/gradle-plugin`: Gradle plugin `com.adamkali.screenplay` (`runScreenplay`, `runScreenplayTests`).
- `screenplay/loaders/`: Included build for Forge and NeoForge (isolated from Loom).
- `screenplay/docs/`: Screenplay GitHub Pages site (MkDocs Material) — https://adam-k-ali.github.io/DWM/
- `screenplay/metadata/`: Screenplay Modrinth listing drafts + `version.json`.
- `.cursor/skills/`: Agent skills for GameTests, asset import, Blockbench models, MCP verify, etc.
- `dwm/minecraft-sources/`: Generated Fabric Loom named Minecraft sources (gitignored `.java`; run `./dwm/gradlew unpackMinecraftSources`). See **Reading Minecraft sources**.

## Gradle wrappers (do not use root `./gradlew` for builds)

| Intent | Command |
| --- | --- |
| DWM client | `./dwm/gradlew runClient` |
| Screenplay client (no DWM) | `./screenplay/gradlew runClient` |
| DWM YAML scenario | `./dwm/gradlew runScreenplay -Pscreenplay=<id>` |
| Screenplay library demos | `./screenplay/gradlew runScreenplay -Pscreenplay=createWorld` |
| Minecraft named sources (Grep) | `./dwm/gradlew unpackMinecraftSources` |

DWM consumes Screenplay via composite `includeBuild('../screenplay')` (Maven coords + dependency substitution), not as a Gradle subproject.

## Agent-First Engineering Principles
- Prefer small, focused, reviewable diffs over broad rewrites.
- Keep behavior deterministic where practical (avoid hidden side effects and implicit global coupling).
- Design for testability first: clear boundaries, injectable collaborators, and pure logic extraction.
- Keep names and package placement consistent with existing project conventions.
- Use non-interactive, scriptable workflows and commands suitable for CI and autonomous agents.
- Do not make speculative refactors outside the requested scope.

## Working Rules For Agents
- Before coding, identify whether change is DWM, Screenplay, common, client-only, data-driven, or test-only.
- Preserve `main`/`client` source-set separation inside DWM.
- Avoid touching large generated/resource surfaces unless the task requires it.
- When fixing bugs, prefer the smallest change that addresses root cause and add regression coverage.
- If a requested behavior cannot be fully automated/tested, document the limitation and propose follow-up automation.
- Product backlog lives on GitHub: issues in [adam-k-ali/DWM](https://github.com/adam-k-ali/DWM/issues) and the [DWM project board](https://github.com/users/adam-k-ali/projects/7/views/1). Titles keep stable IDs (`DWM-NNN — …`, `E-NNN — …`). Status is Project Status (`Backlog` / `In progress` / `Done`) plus issue state; priority is the project Priority field (`P0`–`P3`). Epics are parent issues with ticket sub-issues. A local `tickets/` folder may exist as a gitignored archive — do not use it as the live board.

## Fabric-Specific Development Rules
- Keep Java 25 compatibility.
- Favor Fabric API events/hooks/utilities before introducing Mixins.
- Use Mixins only when needed; keep them minimal and as targeted as possible.
- Maintain compatibility-minded behavior (avoid fragile assumptions about execution order or side effects).

## Reading Minecraft sources
DWM and Screenplay Fabric use **Mojang official mappings** for the Minecraft version in [`dwm/gradle.properties`](dwm/gradle.properties) / [`gradle/libs.versions.toml`](gradle/libs.versions.toml). There is no `yarn_mappings` line — do not guess Yarn 1.20/1.21 names.

- Path: [`dwm/minecraft-sources/net/minecraft/...`](dwm/minecraft-sources/) (Grep/Read with that path; the repo index will not contain it).
- If the tree is missing or [`dwm/minecraft-sources/.version`](dwm/minecraft-sources/.version) does not match `minecraft_version`, run `./dwm/gradlew unpackMinecraftSources`. First run may take several minutes (`genSources`); later unpacks are cheap.
- **Do not** glob `~/.gradle/caches` for `*.java` — that hits NeoForge `ng_execute` transforms (wrong loader). **Do not** extract jars to the repo root.
- Before reimplementing chunk streaming, lighting, tickets, or similar: open the vanilla class under `dwm/minecraft-sources` and wrap it. Matching vanilla policy is not the same as copying `PlayerChunkSender` (that class sends whole chunks to a `ServerPlayer`).

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
- When changing data-generated content, run `./dwm/gradlew runDatagen` and include required generated outputs.
- Avoid committing unrelated generated churn.
- Keep datagen providers deterministic (same inputs should produce same outputs).

## Testability-First Design Rules
- New behavior should include automated tests by default.
- Bug fixes should include a regression test whenever feasible.
- Prefer unit tests for logic-heavy code and decision branches.
- Add integration/gameplay-focused tests when unit tests cannot prove behavior.
- If no automated test is added, explicitly state why and what test should be added later.

## Testing Expectations
- **DWM unit tests** (`./dwm/gradlew test`): JUnit 5 via `fabric-loader-junit`. Included in `./dwm/gradlew build` and CI.
- **Screenplay unit tests** (`./screenplay/gradlew :common:test`): harness compiler/primitives.
- **GameTests** (`./dwm/gradlew runGametest`): Headless dedicated-server in-world tests in `dwm/src/main/java/.../gametest/`. Not run by CI today — run locally when GameTest code changes. See `.cursor/skills/fabric-gametest/SKILL.md`.
- **DWM Screenplay tests** (`./dwm/gradlew runScreenplay -Pscreenplay=<id>`): Real Minecraft client + DWM YAML under `dwm/src/screenplayTests/`. Requires a display; CI uses xvfb.
- **Screenplay library demos** (`./screenplay/gradlew runScreenplayTests`): Bundled demos under `screenplay/common/src/main/resources/tests/`.
- Keep test runs reproducible and suitable for unattended agent execution.

## Automation Pipeline For Agents
- During development:
  - Run targeted tests for changed scope when available.
- Before handoff/PR:
  - Run `./dwm/gradlew test` (and/or `./screenplay/gradlew :common:test` when Screenplay changed).
  - Run `./dwm/gradlew build` / `./screenplay/gradlew build` for full compile/package confidence on larger changes.
  - Run `./dwm/gradlew runDatagen` when DWM data-driven content changed.
- Prefer fast feedback loops, but do not skip required quality gates.

## Build/Test/Validation
- Main automated commands:
  - `./dwm/gradlew test` — DWM JUnit suite
  - `./dwm/gradlew build` — compile DWM + JUnit (CI gate; also builds Screenplay via includeBuild)
  - `./screenplay/gradlew :common:test` — Screenplay compiler/primitives
  - `./screenplay/gradlew -p gradle-plugin test` — Gradle plugin unit tests
  - `./screenplay/gradlew build` — Screenplay Fabric artifact
  - `./screenplay/gradlew -p loaders :forge:build :neoforge:build` — Forge/NeoForge artifacts
  - `./dwm/gradlew runDatagen` — regenerate `dwm/src/main/generated/` (only when datagen providers or promoted assets change); finalized by `pruneDatagenItemModels`
  - `./dwm/gradlew runGametest` — headless GameTests → `dwm/build/gametest/report.xml`
  - `./dwm/gradlew runScreenplay -Pscreenplay=<yaml-stem>` — DWM client YAML scenarios → `dwm/build/screenplay/report.xml`
  - `./dwm/gradlew syncVersionJson` / `checkVersionSync` — keep `dwm/version.json` aligned with `dwm/gradle.properties`
  - `./dwm/gradlew unpackMinecraftSources` — explode Fabric named Minecraft sources into `dwm/minecraft-sources/` (local/agent; not CI)
- If a command fails, surface the failure clearly and fix root causes before handoff where possible.

## Releases / CI
- CI runs on GitHub Actions ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)); CircleCI is retired.
- DWM releases are intentional git tags `v{minecraft}-{mod}` — see [dwm/docs/release-policy.md](dwm/docs/release-policy.md).
- Screenplay releases use tags `screenplay-v{screenplay_version}`. The release workflow publishes loader jars to GitHub Releases and Modrinth, then `publishPlugins` to the Gradle Plugin Portal (`com.adamkali.screenplay`, same version). Requires `MODRINTH_TOKEN`, `GRADLE_PUBLISH_KEY`, and `GRADLE_PUBLISH_SECRET`.
- Outsiders apply the Portal plugin (no `includeBuild`). The plugin resolves `screenplay-fabric` / `-forge` / `-neoforge` from the matching GitHub Release. DWM still consumes Screenplay via composite `includeBuild`.
- Consumer CI: [`adam-k-ali/screenplay-action`](https://github.com/adam-k-ali/screenplay-action) (`@v1`).
- Do not bump `mod_version` or `version.json` promos except when cutting a release; use `./dwm/gradlew syncVersionJson` at cut time.
- The release workflow publishes the GitHub Release, Modrinth version, CurseForge file, and Discord `#releases` announcement from `dwm/version.json` (`summary` + changelog lists). Requires `MODRINTH_TOKEN`, `CURSEFORGE_TOKEN`, and `DISCORD_WEBHOOK_URL` secrets.
- Modrinth project listing fields live in `dwm/metadata/` and `screenplay/metadata/`; sync with the manual Sync Modrinth workflows.

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
- `dwm/src/screenplayTests/AGENTS.md` — YAML client scenario framework (primitives, composite commands, Gradle properties).
- `dwm/src/main/java/com/adamkali/dwm/tardis/AGENTS.md` — TARDIS domain layout (logic vs data vs interior vs portal rendering).
- `dwm/tools/AGENTS.md` — offline Python audio tooling and fixture rules.
- `.cursor/skills/fabric-gametest/SKILL.md` — authoring and registering Fabric GameTests.
- `.cursor/skills/asset-import-pipeline/SKILL.md` — promoting archive textures and wiring datagen.

## Cursor Cloud specific instructions

These notes are for agents running in the Cursor Cloud VM. The standard build/test/datagen commands are already documented above; this section only captures non-obvious environment caveats.

- Java 25 is what Gradle targets (`./dwm/gradlew` / `./screenplay/gradlew` pick up the system JDK; no `JAVA_HOME` tweaking needed). Local agents may need `JAVA_HOME` pointed at a JDK 25 install.
- The startup update script should resolve dependencies via `./dwm/gradlew dependencies -q` (and/or `./screenplay/gradlew dependencies -q`). The very first Loom configuration on a cold cache is slow and network-heavy; once cached, subsequent Gradle invocations are fast.
- Running the mod end-to-end without a display: use `./dwm/gradlew runGametest`.
- YAML client scenario tests (`./dwm/gradlew runScreenplay`) boot a real Fabric client. In this headless VM use `-PscreenplayDisplay=xvfb` (requires `xvfb` / Mesa). Prefer `runGametest` for server-side gameplay smoke tests.
- `./dwm/gradlew runClient` and `./dwm/gradlew runServer` start the actual game; `runClient` needs a GUI/display and will not work in the headless VM.
- `./dwm/gradlew build` also compiles the `client` source set and runs the full JUnit suite.
- `./dwm/gradlew runDatagen` writes generated resources under `dwm/src/main/generated/` and also leaves an untracked `.cache/` directory — delete that `.cache` dir before committing to avoid stray churn.
- IDE: open `dwm/` as the Gradle project for mod work (composite pulls Screenplay). Open `screenplay/` when working on the harness alone.
- `./dwm/gradlew unpackMinecraftSources` is local/agent (not CI); first `genSources` on a cold cache is slow.
