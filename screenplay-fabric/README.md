# Screenplay

**Screenplay** is a multi-loader library for real-client Minecraft scenario tests with CI screenshots.

**Documentation:** [Quick start](https://adam-k-ali.github.io/DWM/quickstart/) · [Commands available](https://adam-k-ali.github.io/DWM/reference/commands/) · [Full docs](https://adam-k-ali.github.io/DWM/)

| Module | Role |
| --- | --- |
| `screenplay-common` | Shared YAML compiler, runner, primitives (unit-tested here) |
| `screenplay-fabric` | Fabric client mod + access widener (embeds common) |
| `screenplay-loaders/forge` | Forge client mod + access transformer (included build) |
| `screenplay-loaders/neoforge` | NeoForge client mod + access transformer (included build) |
| `screenplay-gradle-plugin` | `com.adamkali.screenplay` plugin (`runScreenplay`, `runScreenplayTests`, xvfb) |

Forge and NeoForge live in the `screenplay-loaders` included build so NeoGradle does not conflict with Fabric Loom run configs in a Fabric root project.

```bash
./gradlew -p screenplay-loaders :forge:compileJava :neoforge:compileJava
```

Marketing listing drafts: [`metadata/screenplay/`](../metadata/screenplay/).

## Consumer commands (Fabric)

```bash
./gradlew runScreenplay -Pscreenplay=createWorld -PscreenplayDisplay=xvfb
./gradlew runScreenplayTests -PscreenplayDisplay=xvfb
./gradlew :screenplay-common:test
```

Gradle properties: `-Pscreenplay=<id>`, `-PscreenplayTimeout=<seconds>`, `-PscreenplayDisplay=display|xvfb`.

System properties (set by the plugin): `screenplay`, `screenplay.step-timeout-seconds`, `screenplay.report-file`, `screenplay.vanilla-server-dir`.

Default YAML root: `src/screenplayTests/resources/tests/`. Outputs land under `build/screenplay/`.

## Extending primitives

Implement `com.adamkali.screenplay.primitive.ScenarioPrimitive` and register via `META-INF/services/com.adamkali.screenplay.primitive.ScenarioPrimitive` (`ServiceLoader`).
