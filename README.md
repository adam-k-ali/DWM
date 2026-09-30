# DWM

The Doctor Who Mod is a Minecraft Fabric mod. The Gradle project lives under [`dwm/`](dwm/). The root `./gradlew` is a shim that prints this path and exits.

## Common commands

```bash
./dwm/gradlew runClient
./dwm/gradlew test
./dwm/gradlew build
./dwm/gradlew runDatagen
./dwm/gradlew runGametest
```

Minecraft / Loom / Fabric versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml). Keep [`dwm/gradle.properties`](dwm/gradle.properties) aligned with that catalog.

Agent-oriented detail: [`AGENTS.md`](AGENTS.md).
