# Sightline

**Sightline** is a multi-loader library for real-client Minecraft scenario tests with CI screenshots.

| Module | Role |
| --- | --- |
| `sightline-common` | Shared YAML compiler, runner, primitives (unit-tested here) |
| `sightline-fabric` | Fabric client mod + access widener (embeds common) |
| `sightline-loaders/forge` | Forge client mod + access transformer (included build) |
| `sightline-loaders/neoforge` | NeoForge client mod + access transformer (included build) |
| `sightline-gradle-plugin` | `com.adamkali.sightline` plugin (`runSightline`, `runSightlineTests`, xvfb) |

Forge and NeoForge live in the `sightline-loaders` included build so NeoGradle does not conflict with Fabric Loom run configs in a Fabric root project.

```bash
./gradlew -p sightline-loaders :forge:compileJava :neoforge:compileJava
```

Marketing listing drafts: [`metadata/sightline/`](../metadata/sightline/).

## Consumer commands (Fabric)

```bash
./gradlew runSightline -Psightline=createWorld -PsightlineDisplay=xvfb
./gradlew runSightlineTests -PsightlineDisplay=xvfb
./gradlew :sightline-common:test
```

Gradle properties: `-Psightline=<id>`, `-PsightlineTimeout=<seconds>`, `-PsightlineDisplay=display|xvfb`.

System properties (set by the plugin): `sightline`, `sightline.step-timeout-seconds`, `sightline.report-file`, `sightline.vanilla-server-dir`.

Default YAML root: `src/sightlineTests/resources/tests/`. Outputs land under `build/sightline/`.

## Extending primitives

Implement `com.adamkali.sightline.primitive.ScenarioPrimitive` and register via `META-INF/services/com.adamkali.sightline.primitive.ScenarioPrimitive` (`ServiceLoader`).
