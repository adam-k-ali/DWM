# Quick start

Add Screenplay to a Fabric (or Forge / NeoForge) Gradle project, write a YAML scenario, and run it.

## 1. Apply the plugin and dependency

```groovy
plugins {
    id 'com.adamkali.screenplay' version '<version>'
}

dependencies {
    runtimeOnly "com.adamkali.screenplay:screenplay-fabric:<version>"
}

screenplay {
    loader = 'fabric'
    testsDir = file('src/screenplayTests/resources/tests')
}
```

Forge and NeoForge use the same plugin with `loader = 'forge'` or `loader = 'neoforge'` and the matching `screenplay-forge` / `screenplay-neoforge` artifact.

See the [Gradle plugin reference](reference/gradle-plugin.md) for all extension fields.

## 2. Add a scenario

Create a file under your tests directory, for example
`src/screenplayTests/resources/tests/createWorld.yaml`:

```yaml
---
name: Create a flat world
type: test
---
steps:
  - launchGame
  - createWorld:
      worldType: flat
      gameMode: creative
  - captureScreenshot:
      name: world-ready.png
```

The filename stem (`createWorld`) is the scenario ID used on the command line.

## 3. Run it

```bash
./screenplay/gradlew runScreenplay -Pscreenplay=createWorld -PscreenplayDisplay=xvfb
```

On a machine with a real display, omit `-PscreenplayDisplay` or use `display`.

| Property | Purpose |
| --- | --- |
| `-Pscreenplay=<id>` | YAML filename stem to run |
| `-PscreenplayDisplay=display\|xvfb` | Framebuffer strategy (default `display`) |
| `-PscreenplayTimeout=<seconds>` | Per-step timeout (default 30) |
| `-PscreenplayRecord=true\|false` | Screen-record the client (requires `ffmpeg`) |

Results land under `build/screenplay/` (JUnit XML, metrics, diagnostics, screenshots, optional recordings). Details: [Running tests](running-tests.md).

## 4. Next steps

- Learn YAML frontmatter, selectors, and composites in [Writing scenarios](writing-scenarios.md).
- Look up each step in [Commands available](reference/commands.md).
- Register custom primitives with [Extending](extending.md).
