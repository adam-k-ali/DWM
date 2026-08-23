# Gradle plugin

Plugin ID: `com.adamkali.screenplay`

Registered by the `screenplay-gradle-plugin` included build. It wires prepare /
run tasks for Fabric, Forge, or NeoForge and applies display-mode wrappers.

## Extension (`screenplay { }`)

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `loader` | `String` | auto when unambiguous | `fabric`, `forge`, or `neoforge` |
| `testsDir` | `File` | `src/screenplayTests/resources/tests` | Primary YAML root |
| `extraTestsDirs` | `List<File>` | `[]` | Additional roots scanned by `runScreenplayTests` |
| `runDir` | `String` | `build/screenplay/run` | Client run directory (project-relative) |
| `outputDir` | `String` | `screenplay` | Build-relative output directory name for reports |

```groovy
screenplay {
    loader = 'fabric'
    testsDir = file('src/screenplayTests/resources/tests')
    extraTestsDirs = [file('screenplay-common/src/main/resources/tests')]
    runDir = 'build/screenplay/run'
    outputDir = 'screenplay'
}
```

## Tasks

| Task | Purpose |
| --- | --- |
| `runScreenplay` | Run one scenario (`-Pscreenplay=<id>` required) |
| `runScreenplayTests` | Discover and run every `type: test` YAML |

## Gradle properties

| Property | Description |
| --- | --- |
| `-Pscreenplay=<id>` | Scenario filename stem |
| `-PscreenplayTimeout=<seconds>` | Per-step timeout (default 30) |
| `-PscreenplayDisplay=display\|xvfb` | Framebuffer strategy (default `display`) |

## Dependencies

```groovy
dependencies {
    runtimeOnly "com.adamkali.screenplay:screenplay-fabric:<version>"
}
```

| Loader | Artifact |
| --- | --- |
| Fabric | `screenplay-fabric` |
| Forge | `screenplay-forge` |
| NeoForge | `screenplay-neoforge` |

## Related

- [Quick start](../quickstart.md)
- [Running tests](../running-tests.md)
