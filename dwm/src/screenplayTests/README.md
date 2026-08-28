# YAML client scenarios

This directory holds mod-owned **Screenplay** YAML scenarios. The
`com.adamkali.screenplay` plugin adds the harness (composite includeBuild)
includeBuild) and loads these files from disk. They are not included in the
production mod jar.

Full documentation (quick start + command API reference):
https://adam-k-ali.github.io/DWM/

## Running a scenario

Run a test by its YAML filename without the extension:

```bash
./dwm/gradlew runScreenplay -Pscreenplay=placeAndOpenTardis
./dwm/gradlew runScreenplay -Pscreenplay=fieldGuide -PscreenplayDisplay=xvfb
```

Mod-owned scenarios include `placeAndOpenTardis` (TARDIS door/interior flow) and
`fieldGuide` (Field Guide UI snapshots). PNGs from `captureScreenshot` land under
`build/screenplay/run/screenshots/` and are copied into
`build/screenplay/results/<id>/screenshots/` by `runScreenplayTests`. **Do not**
commit baseline PNGs — review screenshot diffs across CI runs or local runs when
the Field Guide UI changes.

Discover every `type: suite` and standalone `type: test` YAML under
`resources/tests/`. Suites share one client session; standalone tests still get
a fresh client each:

```bash
./dwm/gradlew runScreenplayTests
./dwm/gradlew runScreenplayTests -PscreenplayDisplay=xvfb -PscreenplayTimeout=120
```

`runScreenplayTests` skips `type: command` documents and skips `type: test` IDs
that are listed as members of a discovered suite. It continues after individual
failures, and fails the aggregate with the list of failed IDs.

The client uses an isolated directory under `build/screenplay/run`. Before
each run, the harness removes its saved worlds, clears
`build/screenplay/vanilla-server/world`, and writes deterministic English
client options.

### Display modes

Select the framebuffer strategy with `-PscreenplayDisplay` (default `display`):

| Value | Behavior |
| --- | --- |
| `display` | Real `$DISPLAY` / GLFW window. Fails fast on Linux when `$DISPLAY` is unset. |
| `xvfb` | Wraps the client with Loom’s `xvfb-run` path (Linux only). Still boots a real client against a virtual framebuffer; requires `xvfb` installed (`apt install xvfb`). Sets soft-GL-friendly client options and `LIBGL_ALWAYS_SOFTWARE=1`. |

```bash
./dwm/gradlew runScreenplay -Pscreenplay=placeAndOpenTardis -PscreenplayDisplay=display
./dwm/gradlew runScreenplay -Pscreenplay=placeAndOpenTardis -PscreenplayDisplay=xvfb -PscreenplayTimeout=120
```

The default per-step timeout is 30 seconds. Override it when debugging (or for
slow soft-GL CI runs):

```bash
./dwm/gradlew runScreenplay -Pscreenplay=placeAndOpenTardis -PscreenplayTimeout=60
```

Results are written to:

- `build/screenplay/report.xml` — JUnit XML
- `build/screenplay/metrics.json` — wall-clock step timings for perf compare
  (listed in `.gitignore`; CI uploads the file as a workflow artifact)
- `build/screenplay/diagnostics.txt` — current screen and visible widgets
- `build/screenplay/run/screenshots/` — PNGs from `captureScreenshot`
- `build/screenplay/run/recordings/` — MP4s when `-PscreenplayRecord=true` or YAML
  `record: true` (requires `ffmpeg`)
- `build/screenplay/results/<id>/` — per-scenario copies of report, metrics,
  diagnostics, screenshots, and recordings written by `runScreenplayTests`
- `build/screenplay/vanilla-server/` — official dedicated-server run dir from
  `startVanillaServer` (`server-jar.path`, `eula.txt`, `server.properties`,
  `world/`, `logs/harness.log`)

The Gradle process exits non-zero when loading, validation, or execution fails.

### Performance vs main (CI)

The [Screenplay Tests](../../../.github/workflows/scenario-tests.yml) workflow runs
`runScreenplayTests` and uploads `build/screenplay/results/*/metrics.json`
(plus the last-run top-level files). On pull requests, a follow-up job
downloads the latest successful `main` run’s artifacts, compares totals and
matching step names with
[`.github/scripts/compare-scenario-perf.py`](../../../.github/scripts/compare-scenario-perf.py)
(default: 20% slower **and** more than 50ms), and upserts a single PR comment
marked `<!-- dwm-screenplay-perf -->`. The compare is **advisory** — regressions
do not fail CI.

Local fixture check for the compare script:

```bash
../../../.github/scripts/run_compare_scenario_perf_fixtures.sh
```

## Files and discovery

YAML definitions live recursively under `resources/tests/`. Their directory
does not determine their role; the frontmatter `type` does:

```yaml
---
name: Create World
type: test
---
steps:
  - launchGame
```

Supported frontmatter types are:

- `test` — an executable scenario selected with `-Pscreenplay`.
- `command` — a reusable composite command.
- `suite` — one-client group of tests with `before-all` / `before-each` /
  `after-each` / `after-all` hooks and a `tests:` member list.

The filename stem is the stable ID. For example,
`subflows/assertAndClick.yaml` defines `assertAndClick`. Duplicate test,
command, or suite IDs are rejected even when the files are in different
directories.

## Steps and selectors

The MVP primitives are:

- `launchGame` — waits until the title screen is ready.
- `assertVisible` — waits for a visible matching widget.
- `click` — waits for an active matching widget and dispatches a real screen
  mouse click at its center.
- `debugScreen` — immediately logs the current screen class and every visible
  widget (type, name, active, bounds). It takes no arguments and always
  succeeds on the tick it runs.
- `captureScreenshot` — captures the current framebuffer (world and GUI) to
  `build/screenplay/run/screenshots/`. With no arguments it uses a vanilla
  timestamped filename. An optional `name` sets the PNG stem. Optional
  `compare: true` (requires `name`) diffs against a baseline PNG when
  `-PscreenplayBaselinesDir` / `screenplay.baselines-dir` is set (CI supplies
  green `main` artifacts). Optional `maxDiffPixels` defaults to `0`. The step
  waits until the file has been written before succeeding.
- `startVanillaServer` — launches Mojang’s official dedicated-server jar as a
  child process (no Fabric/DWM on the server). It writes offline-mode superflat
  settings, waits until `127.0.0.1` accepts TCP connections, and stops the
  process when the scenario finishes. This step does **not** connect the client.
  It uses a 120 second timeout floor; `-PscreenplayTimeout` still raises that
  floor when set higher.
- `createWorld` — opens vanilla Create World, applies the given settings, and
  waits until the local player is in the loaded world. Omitted keys use
  test-friendly defaults: superflat, creative, peaceful, commands on, seed
  `"42"`. An optional `name` overrides vanilla’s “New World”. This step does
  **not** click through the Create World tabs. It uses a 120 second timeout
  floor; `-PscreenplayTimeout` still raises that floor when set higher.
- `keyboardInput` — waits until a focused, editable text field can consume
  input, then types the given string once via real `charTyped` events. It does
  not click or select the field; click the matching `editbox` first.
- `waitUntil` — polls until exactly one condition is true: a nested selector is
  `visible` or `notVisible`, the main hand is `holding` an item id, or a
  `block` id is at `x`/`y`/`z`. `visible` is equivalent to `assertVisible`.
  `notVisible` succeeds as soon as the selector does not match on the current
  tick, including if it has not appeared yet. Item and block ids may omit
  `minecraft:`. Block coordinates use the same relative/absolute rules as
  `lookAt`.
- `waitTicks` — waits until the client world's game time has advanced by the
  given number of ticks. A scalar positive integer is accepted
  (`waitTicks: 25`), or an object with `ticks`. Use this for short animations
  that have no `waitUntil` condition (for example door swing).
- `openInventory` — waits until a local player exists, then opens the survival
  or creative inventory GUI. It takes no arguments. If the inventory is already
  showing, the step succeeds without reopening it.
- `closeScreen` — waits until a local player exists, then closes the current
  GUI (`setScreen(null)` / `closeContainer()`). If no screen is open, the step
  succeeds without changing anything. World actions such as `useItem` wait
  until the screen is gone.
- `setSneaking` — sets the client's held sneak-key state from a required
  boolean `enabled` argument. The state persists until another step releases it.
- `selectHotbar` — waits until a local player and play connection exist, then
  selects hotbar slot `0`–`8` and syncs that slot to the server. A scalar
  integer is accepted.
- `lookAt` — waits until a local player exists, then aims the camera. Supply
  either `yaw` and `pitch`, or block coordinates `x`, `y`, and `z`. Coordinate
  components may be absolute integers or relative (`"~"`, `"~1"`, `"~-1"`).
  Quote `"~"` in YAML; a bare `~` is YAML null. Relative values use the
  player's block position. Pitch must be between `-90` and `90`.
- `walkUntil` — waits until a local player exists and no GUI is open, then holds
  forward movement (`keyUp`) until one end condition is met: block coordinates
  `x`/`y`/`z` (same relative/absolute rules as `lookAt`; re-aims at the target
  each tick), or `dimension` (a namespaced world id such as `dwm:tardis`).
  Exactly one mode is required. The forward key is released when the step
  succeeds.
- `useItem` — waits until a local player, game mode, and no open screen exist,
  then uses the main-hand item on the targeted block by default. `useItem: air`
  invokes the item's in-air use path. It succeeds as soon as the use is sent;
  it does not wait for the world to update.
- `runCommand` — waits until a local player and play connection exist, then
  sends an in-game slash command as that player (the same path as typing `/give`
  in chat, without opening the chat GUI). The step succeeds as soon as the
  command packet is sent; it does not wait for success chat or inventory
  updates. Cheats (singleplayer) or operator (dedicated server) are required
  for privileged commands such as `/give`. `startVanillaServer` does not OP
  the player.
- `pressKey` — presses a keyboard key once (for example `g` for a bound key or
  `escape` for the pause menu). Pair with `waitUntil` when the key opens a
  screen on a later client tick.

Field Guide scenario (`fieldGuide.yaml`) uses `pressKey: g`, chapter/page
`assertAndClick` navigation, and `pressKey: escape` + pause-menu `"Field Guide"`
for the secondary access path. No mod-specific Screenplay primitives are required.

```yaml
- waitUntil:
    visible:
      type: button
      name: "Singleplayer"
- waitUntil:
    notVisible:
      type: screen
      name: LevelLoadingScreen
- waitUntil:
    holding: minecraft:dirt
- waitUntil:
    block:
      id: minecraft:dirt
      x: "~1"
      y: "~"
      z: "~"
```

```yaml
- closeScreen
- selectHotbar: 0
- selectHotbar:
    slot: 3
- lookAt:
    x: "~1"
    y: "~-1"
    z: "~"
- lookAt:
    yaw: 90
    pitch: 45
- walkUntil:
    x: "~3"
    y: "~"
    z: "~"
- walkUntil:
    dimension: dwm:tardis
- useItem
- waitTicks: 25
- waitTicks:
    ticks: 25
```

```yaml
- startVanillaServer
- startVanillaServer:
    port: 25565
```

```yaml
- createWorld
- createWorld:
    worldType: superflat
    gameMode: creative
    difficulty: peaceful
    allowCommands: true
    seed: "42"
    name: Scenario World
```

```yaml
- keyboardInput: "localhost:25565"
- keyboardInput:
    text: "localhost:25565"
```

```yaml
- runCommand: "/give @s minecraft:diamond 1"
- runCommand:
    command: "/give @s minecraft:diamond 1"
```

```yaml
- captureScreenshot
- captureScreenshot:
    name: after-world-tab
- captureScreenshot:
    name: world-ready.png
    compare: true
    maxDiffPixels: 0
```

Selectors require an exact `name` and one of these types:

- `button`
- `cycle`
- `tab`
- `editbox`
- `label`
- `screen`

For `editbox`, `name` is the accessibility narration label (`getMessage()`),
not the current field value. Direct Connection’s IP field is `"Server Address"`.

For `label`, `name` is a `StringWidget` message, or the painted status text on
the vanilla connect screen (for example `"Connecting to the server..."`).
`click` only targets widgets, so it cannot activate painted connect-screen
status text.

For `screen`, `name` is the Java simple class name of the current GUI
(`LevelLoadingScreen`, not the fully qualified name). It matches when that
screen is open. `click` cannot target `type: screen`.

```yaml
steps:
  - assertVisible:
    - type: tab
      name: "World"
  - click:
    - type: tab
      name: "World"
  - assertAndClick:
    - type: editbox
      name: "Server Address"
  - keyboardInput: "localhost:25565"
```

The single-item list form above and a direct object are both accepted.

## Composite commands

A command declares string parameters and can use them in nested steps:

```yaml
---
name: Assert and Click
type: command
---
parameters:
  - name: type
    type: string
  - name: name
    type: string
steps:
  - assertVisible:
    - type: "{{ type }}"
      name: "{{ name }}"
  - click:
    - type: "{{ type }}"
      name: "{{ name }}"
```

Invoke it by filename ID:

```yaml
- assertAndClick:
  - type: button
    name: "Singleplayer"
```

Unknown steps, missing or extra parameters, unsupported parameter/selector
types, malformed documents, and recursive command cycles fail before the client
flow begins.
