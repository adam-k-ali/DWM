# Quick start

Add Screenplay to a Fabric (or Forge / NeoForge) Gradle project, write a YAML scenario, and run it.

## 1. Apply the plugin

```groovy
plugins {
    id 'net.fabricmc.fabric-loom' version '<loom-version>'
    id 'com.adamkali.screenplay' version '<version>'
}
```

That is the whole Gradle install. The plugin:

- Detects Fabric, Forge, or NeoForge
- Adds the matching harness dependency (`screenplay-fabric`, `screenplay-forge`, or `screenplay-neoforge`)
- Creates a Screenplay client run
- Reads YAML from `src/screenplayTests/resources/tests/` on disk (not from the production mod jar)

No `runtimeOnly` line and no `screenplay { }` block are required. See the [Gradle plugin reference](reference/gradle-plugin.md) for optional extension fields.

## 2. Add a scenario

Create `src/screenplayTests/resources/tests/myFirstTest.yaml`:

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

If that folder is empty, `./gradlew runScreenplay` writes this starter file for you.

The filename stem (`myFirstTest`) is the scenario ID used on the command line.

## 3. Run it

On a machine with a display:

```bash
./gradlew runScreenplay
```

If the tests folder has **one** scenario, that command runs it. If it has several, pass the id:

```bash
./gradlew runScreenplay -Pscreenplay=myFirstTest
```

Headless Linux CI (needs `xvfb`):

```bash
./gradlew runScreenplay -Pscreenplay=myFirstTest -PscreenplayDisplay=xvfb
```

| Property | Purpose |
| --- | --- |
| `-Pscreenplay=<id>` | YAML filename stem to run (optional when only one test exists) |
| `-PscreenplayDisplay=display\|xvfb` | Framebuffer strategy (default `display`) |
| `-PscreenplayTimeout=<seconds>` | Per-step timeout (default 30) |
| `-PscreenplayRecord=true\|false` | Screen-record the client (requires `ffmpeg`) |

Results land under `build/screenplay/` (JUnit XML, metrics, diagnostics, screenshots, optional recordings). Details: [Running tests](running-tests.md).

## 4. Next steps

- Learn YAML frontmatter, selectors, and composites in [Writing scenarios](writing-scenarios.md).
- Look up each step in [Commands available](reference/commands.md).
- Register custom primitives with [Extending](extending.md).
