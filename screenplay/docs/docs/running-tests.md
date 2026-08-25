# Running tests

## Single scenario

Run a test by its YAML filename without the extension:

```bash
./screenplay/gradlew runScreenplay -Pscreenplay=createWorld
```

## All scenarios

Discover every `type: test` YAML under configured tests roots and run each one
(fresh client per scenario):

```bash
./screenplay/gradlew runScreenplayTests
./screenplay/gradlew runScreenplayTests -PscreenplayDisplay=xvfb -PscreenplayTimeout=120
```

`runScreenplayTests` skips `type: command` documents, continues after individual
failures, and fails the aggregate with the list of failed scenario IDs.

## Isolated run directory

The client uses an isolated directory under `build/screenplay/run`. Before each
run, the harness removes saved worlds, clears
`build/screenplay/vanilla-server/world`, and writes deterministic English client
options.

## Display modes

Select the framebuffer strategy with `-PscreenplayDisplay` (default `display`):

| Value | Behavior |
| --- | --- |
| `display` | Real `$DISPLAY` / GLFW window. Fails fast on Linux when `$DISPLAY` is unset. |
| `xvfb` | Wraps the client with Loom’s `xvfb-run` path (Linux only). Requires `xvfb` (`apt install xvfb`). Sets soft-GL-friendly options and `LIBGL_ALWAYS_SOFTWARE=1`. |

```bash
./screenplay/gradlew runScreenplay -Pscreenplay=createWorld -PscreenplayDisplay=display
./screenplay/gradlew runScreenplay -Pscreenplay=createWorld -PscreenplayDisplay=xvfb -PscreenplayTimeout=120
```

## Timeouts

The default per-step timeout is **30 seconds**. Override when debugging or for
slow soft-GL CI:

```bash
./screenplay/gradlew runScreenplay -Pscreenplay=createWorld -PscreenplayTimeout=60
```

[`createWorld`](reference/commands/createWorld.md) and
[`startVanillaServer`](reference/commands/startVanillaServer.md) use a **120
second** timeout floor; `-PscreenplayTimeout` still raises that floor when set
higher.

## Outputs

| Path | Contents |
| --- | --- |
| `build/screenplay/report.xml` | JUnit XML |
| `build/screenplay/metrics.json` | Wall-clock step timings |
| `build/screenplay/diagnostics.txt` | Current screen and visible widgets |
| `build/screenplay/run/screenshots/` | PNGs from `captureScreenshot` |
| `build/screenplay/results/<id>/` | Per-scenario copies from `runScreenplayTests` |
| `build/screenplay/vanilla-server/` | Official dedicated-server dir from `startVanillaServer` |

The Gradle process exits non-zero when loading, validation, or execution fails.

## System properties

The plugin sets (among others):

- `screenplay` — scenario ID
- `screenplay.step-timeout-seconds`
- `screenplay.report-file`
- `screenplay.vanilla-server-dir`

## CI tips

- Prefer `-PscreenplayDisplay=xvfb` on headless Linux runners.
- Upload `report.xml`, `metrics.json`, diagnostics, and screenshots as workflow
  artifacts for agent and human review.
