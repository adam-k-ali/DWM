# DWM monorepo

This repository contains two sibling Gradle projects:

| Path | Product | Wrapper |
| --- | --- | --- |
| [`dwm/`](dwm/) | The Doctor Who Mod (Fabric) | `./dwm/gradlew` |
| [`screenplay/`](screenplay/) | Screenplay scenario harness (Fabric + Forge/NeoForge loaders) | `./screenplay/gradlew` |

There is **no** shared Gradle task namespace at the repo root. The root `./gradlew` is a shim that prints these paths and exits.

## Common commands

```bash
# DWM
./dwm/gradlew runClient
./dwm/gradlew test
./dwm/gradlew build
./dwm/gradlew runDatagen
./dwm/gradlew runGametest
./dwm/gradlew runScreenplay -Pscreenplay=<yaml-stem>
./dwm/gradlew runScreenplayTests -PscreenplayDisplay=xvfb

# Screenplay
./screenplay/gradlew runClient
./screenplay/gradlew build
./screenplay/gradlew :common:test
./screenplay/gradlew runScreenplay -Pscreenplay=createWorld
./screenplay/gradlew runScreenplayTests -PscreenplayDisplay=xvfb
./screenplay/gradlew -p loaders :forge:build :neoforge:build
```

Shared Minecraft / Loom / Fabric versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml). Keep each project's `gradle.properties` aligned with that catalog.

Agent-oriented detail: [`AGENTS.md`](AGENTS.md).
