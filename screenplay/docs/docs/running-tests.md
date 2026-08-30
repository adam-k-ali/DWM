# Running tests

## Single scenario or suite

Run a test or suite by its YAML filename without the extension. From a consumer
mod (after applying the plugin):

```bash
./gradlew runScreenplay
./gradlew runScreenplay -Pscreenplay=createWorld
```

`-Pscreenplay` is required when more than one executable YAML exists. With a
single test, `runScreenplay` runs it. With none, it writes `myFirstTest.yaml`
and runs that.

From this repository's Screenplay build:

```bash
./screenplay/gradlew runScreenplay -Pscreenplay=createWorld
./screenplay/gradlew runScreenplay -Pscreenplay=creativeWorldSuite
```

## All scenarios

Discover every `type: suite` and standalone `type: test` YAML under configured
tests roots. Suites run in one client session; standalone tests still get a
fresh client each:

```bash
./screenplay/gradlew runScreenplayTests
./screenplay/gradlew runScreenplayTests -PscreenplayDisplay=xvfb -PscreenplayTimeout=120
```

`runScreenplayTests` skips `type: command` documents and skips `type: test`
IDs that are listed as members of a discovered suite (those run only via the
suite). It continues after individual failures and fails the aggregate with
the list of failed IDs.

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

## Screen recording

Opt in to capture the client display as an MP4 via ffmpeg x11grab (Linux/X11,
including `xvfb`):

```bash
./screenplay/gradlew runScreenplay -Pscreenplay=createWorld \
  -PscreenplayDisplay=xvfb \
  -PscreenplayRecord=true
```

| Source | Behavior |
| --- | --- |
| `-PscreenplayRecord=true\|false` | Explicit CLI override (sets `screenplay.record`) |
| YAML frontmatter `record: true` | Per-scenario/suite default when the Gradle property is unset |

Requires `ffmpeg` on `PATH` (`apt install ffmpeg`). One recording is written per
client session as `build/screenplay/run/recordings/<id>.mp4`.

## Outputs

| Path | Contents |
| --- | --- |
| `build/screenplay/report.xml` | JUnit XML |
| `build/screenplay/metrics.json` | Wall-clock step timings |
| `build/screenplay/diagnostics.txt` | Current screen and visible widgets |
| `build/screenplay/run/screenshots/` | PNGs from `captureScreenshot` (and `{stem}-diff.png` on compare failures) |
| `build/screenplay/run/recordings/` | MP4s when recording is enabled |
| `build/screenplay/results/<id>/` | Per-scenario copies from `runScreenplayTests` |
| `build/screenplay/vanilla-server/` | Official dedicated-server dir from `startVanillaServer` |

The Gradle process exits non-zero when loading, validation, or execution fails.

## Screenshot baselines

Optional visual regression uses `-PscreenplayBaselinesDir=<dir>` (system property
`screenplay.baselines-dir`). Point it at a flat directory of baseline PNGs keyed
by `captureScreenshot` `name`. CI pull requests populate this from the latest
green `main` Screenplay artifact; do not commit baseline PNGs to git.

```bash
./screenplay/gradlew runScreenplayTests \
  -PscreenplayDisplay=xvfb \
  -PscreenplayBaselinesDir=/path/to/baselines
```

## System properties

The plugin sets (among others):

- `screenplay` — scenario ID
- `screenplay.step-timeout-seconds`
- `screenplay.report-file`
- `screenplay.vanilla-server-dir`
- `screenplay.baselines-dir` — when `-PscreenplayBaselinesDir` is set
- `screenplay.record` — when `-PscreenplayRecord` is set

## GitHub Actions

Consumer mods can use [`adam-k-ali/screenplay-action`](https://github.com/adam-k-ali/screenplay-action).
Set up JDK 25 first; the action installs `xvfb` on Linux, runs `runScreenplayTests`, and uploads
`build/screenplay/`.

```yaml
name: Screenplay

on:
  pull_request:
  push:
    branches: [main]

jobs:
  screenplay:
    runs-on: ubuntu-latest
    timeout-minutes: 60
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "25"
      - uses: gradle/actions/setup-gradle@v4
      - uses: adam-k-ali/screenplay-action@v1
```

The same flow without the action:

```yaml
- uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: "25"
- uses: gradle/actions/setup-gradle@v4
- run: |
    sudo apt-get update
    sudo DEBIAN_FRONTEND=noninteractive apt-get install -y \
      xvfb libgl1-mesa-dri libgl1 libasound2t64 \
      libx11-6 libxext6 libxrender1 libxtst6 libxi6
    chmod +x ./gradlew
    ./gradlew runScreenplayTests -PscreenplayDisplay=xvfb -PscreenplayTimeout=120
- uses: actions/upload-artifact@v4
  if: always()
  with:
    name: screenplay-results
    path: build/screenplay/
    if-no-files-found: ignore
```

## CI tips

- Prefer `-PscreenplayDisplay=xvfb` on headless Linux runners.
- Upload `report.xml`, `metrics.json`, diagnostics, screenshots, and recordings as workflow
  artifacts for agent and human review (and as the next `main` screenshot baseline).
- This monorepo's PRs prepare baselines with `.github/scripts/prepare-screenplay-baselines.sh`
  before `runScreenplayTests`. Consumer first-run CI does not need that.
- Screen recording needs `ffmpeg` in addition to `xvfb`.
