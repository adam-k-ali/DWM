# Sightline

**Sightline** is a multi-loader library for real-client Minecraft scenario tests with CI screenshots.

This repository vendors Sightline as Gradle subprojects and consumes the Fabric artifact from DWM.

| Module | Role |
| --- | --- |
| `sightline-common` | Shared YAML compiler, runner, primitives (unit-tested here) |
| `sightline-fabric` | Fabric client mod + access widener (embeds common) |
| `sightline-forge` | Forge client mod + access transformer |
| `sightline-neoforge` | NeoForge client mod + access transformer |
| `sightline-gradle-plugin` | `com.adamkali.sightline` plugin (`runSightline`, `runAllSightlineTests`, xvfb) |

Marketing listing drafts: [`metadata/sightline/`](../metadata/sightline/).

## Consumer commands (DWM / Fabric)

```bash
./gradlew runSightline -Psightline=createWorld -PsightlineDisplay=xvfb
./gradlew runSightline -Psightline=placeAndOpenTardis -PsightlineDisplay=xvfb
./gradlew runAllSightlineTests -PsightlineDisplay=xvfb
./gradlew :sightline-common:test
```

Gradle properties: `-Psightline=<id>`, `-PsightlineTimeout=<seconds>`, `-PsightlineDisplay=display|xvfb`.

System properties (set by the plugin): `sightline`, `sightline.step-timeout-seconds`, `sightline.report-file`, `sightline.vanilla-server-dir`.

Outputs land under `build/sightline/`.

## Extending primitives

Implement `com.adamkali.sightline.primitive.ScenarioPrimitive` and register via `META-INF/services/com.adamkali.sightline.primitive.ScenarioPrimitive` (`ServiceLoader`).
