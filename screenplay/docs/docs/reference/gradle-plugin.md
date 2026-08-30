# Gradle plugin

Plugin ID: `com.adamkali.screenplay`

Published to the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.adamkali.screenplay).
The plugin version matches the loader jars (for example `1.0.0+26.2`). Applying it is enough
for a Fabric, Forge, or NeoForge project:

- Detects the loader plugin
- Adds the matching harness dependency (`screenplay-fabric` / `screenplay-forge` /
  `screenplay-neoforge`) unless `screenplay.addHarnessDependency=false`
- Resolves those artifacts from the `screenplay-v*` GitHub Release (no extra `repositories {}`)
- Creates the Screenplay client run if missing
- Passes `screenplay.tests-dirs` so the client loads YAML from disk
- Writes `myFirstTest.yaml` when the tests directory has no YAML yet

This DWM monorepo still uses `includeBuild` for local plugin and loader development. Consumers
should apply the Portal plugin as in the [Quick start](../quickstart.md) — do not copy
`includeBuild` from this repository.

The `screenplay { }` block is optional. Defaults match a typical Fabric mod.

## Extension (`screenplay { }`)

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `loader` | `String` | auto when unambiguous | `fabric`, `forge`, or `neoforge` |
| `testsDir` | `File` | `src/screenplayTests/resources/tests` | Primary YAML root (filesystem, not a source set) |
| `extraTestsDirs` | `List<File>` | `[]` | Additional roots scanned by `runScreenplayTests` |
| `runDir` | `String` | `build/screenplay/run` | Client run directory (project-relative) |
| `outputDir` | `String` | `screenplay` | Build-relative output directory name for reports |

```groovy
screenplay {
    loader = 'fabric'
    testsDir = file('src/screenplayTests/resources/tests')
    extraTestsDirs = []  // library demos run from the Screenplay build
    runDir = 'build/screenplay/run'
    outputDir = 'screenplay'
}
```

Projects that *produce* a Screenplay loader jar (this repo’s Fabric/Forge/NeoForge
builds) must set `screenplay.addHarnessDependency=false` in `gradle.properties` so
they do not depend on themselves.

## Tasks

| Task | Purpose |
| --- | --- |
| `runScreenplay` | Run one scenario or suite. `-Pscreenplay=<id>` is required when more than one executable YAML exists; with a single test the plugin runs it; with none it writes `myFirstTest.yaml` and runs that |
| `runScreenplayTests` | Discover and run every suite plus standalone `type: test` YAML |

Suite member tests listed under a discovered suite's `tests:` key are not also
run standalone.

## Gradle properties

| Property | Description |
| --- | --- |
| `-Pscreenplay=<id>` | Scenario filename stem |
| `-PscreenplayTimeout=<seconds>` | Per-step timeout (default 30) |
| `-PscreenplayDisplay=display\|xvfb` | Framebuffer strategy (default `display`) |
| `-PscreenplayRecord=true\|false` | Override screen recording (requires `ffmpeg`) |
| `-PscreenplayBaselinesDir=<dir>` | Baseline PNGs for `captureScreenshot` compare |
| `screenplay.addHarnessDependency=false` | Skip auto-adding the loader artifact (for Screenplay’s own builds) |
| `screenplay.harnessRepositoryUrl` | Override the GitHub Release download base (tests only) |

The client also receives `screenplay.tests-dirs` (path-separator–joined absolute
paths of `testsDir` + `extraTestsDirs`). Consumer tests load from those folders;
bundled `type: command` documents such as `assertAndClick` still come from the
harness jar.

## Dependencies

The plugin adds the loader artifact for you. You do **not** need a `runtimeOnly`
line or a Screenplay `repositories {}` block in a consumer `build.gradle`.

| Loader | Artifact |
| --- | --- |
| Fabric | `com.adamkali.screenplay:screenplay-fabric` (`modRuntimeOnly` when Loom provides that configuration) |
| Forge | `com.adamkali.screenplay:screenplay-forge` |
| NeoForge | `com.adamkali.screenplay:screenplay-neoforge` |

Those coordinates resolve from GitHub Releases for the tag `screenplay-v{version}`. Override
the download base with `screenplay.harnessRepositoryUrl` only in tests.

Java 25 and a Minecraft version matching the Screenplay release are required.
The plugin fails with a short message when the project toolchain or Minecraft
version is readable and does not match.

## Related

- [Quick start](../quickstart.md)
- [Running tests](../running-tests.md)
