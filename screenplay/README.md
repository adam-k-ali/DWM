# Screenplay

**Screenplay** is a multi-loader library for real-client Minecraft scenario tests with CI screenshots.

**Documentation:** [Quick start](https://adam-k-ali.github.io/DWM/quickstart/) · [Commands available](https://adam-k-ali.github.io/DWM/reference/commands/) · [Full docs](https://adam-k-ali.github.io/DWM/)

This directory is its own Gradle root (`./screenplay/gradlew`). DWM lives in the sibling [`../dwm/`](../dwm/) build and consumes this project via composite `includeBuild`.

| Module | Role |
| --- | --- |
| `.` (this root) | Fabric client mod + access widener (embeds `common`; artifact `screenplay-fabric`) |
| `common/` | Shared YAML compiler, runner, primitives (unit-tested here) |
| `loaders/forge` | Forge client mod + access transformer (included build) |
| `loaders/neoforge` | NeoForge client mod + access transformer (included build) |
| `gradle-plugin/` | `com.adamkali.screenplay` plugin (`runScreenplay`, `runScreenplayTests`, xvfb). Applying it adds the harness dependency and loads YAML from `src/screenplayTests/resources/tests/`. |

Forge and NeoForge live in the `loaders` included build so NeoGradle does not conflict with Fabric Loom run configs.

```bash
./gradlew -p loaders :forge:compileJava :neoforge:compileJava
```

Marketing listing drafts: [`metadata/`](metadata/).

## Commands

```bash
./gradlew runClient
./gradlew runScreenplay -Pscreenplay=createWorld -PscreenplayDisplay=xvfb
./gradlew runScreenplayTests -PscreenplayDisplay=xvfb
./gradlew :common:test
```

From the monorepo root, prefer `./screenplay/gradlew …` so DWM and Screenplay never share a task namespace.

Gradle properties: `-Pscreenplay=<id>`, `-PscreenplayTimeout=<seconds>`,
`-PscreenplayDisplay=display|xvfb`, optional `-PscreenplayBaselinesDir=<dir>` for
`captureScreenshot` compare against main CI baselines.

Default YAML root for this build: `common/src/main/resources/tests/`. Outputs land under `build/screenplay/`.

## Extending primitives

Implement `com.adamkali.screenplay.primitive.ScenarioPrimitive` and register via `META-INF/services/com.adamkali.screenplay.primitive.ScenarioPrimitive` (`ServiceLoader`).
