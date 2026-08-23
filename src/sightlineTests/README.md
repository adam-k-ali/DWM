# YAML client scenarios

This directory holds mod-owned **Sightline** YAML scenarios. The harness ships
as the `sightline-*` Gradle modules (`sightline-fabric` on the client run).
It is not included in the production mod jar.

## Running a scenario

Run a test by its YAML filename without the extension:

```bash
./gradlew runSightline -Psightline=createWorld
```

Discover every `type: test` YAML under `resources/tests/` and run each one
(fresh client per scenario):

```bash
./gradlew runSightlineTests
./gradlew runSightlineTests -PsightlineDisplay=xvfb -PsightlineTimeout=120
```

`runSightlineTests` skips `type: command` documents, continues after individual
failures, and fails the aggregate with the list of failed scenario IDs.

The client uses an isolated directory under `build/sightline/run`. Before
each run, the harness removes its saved worlds, clears
`build/sightline/vanilla-server/world`, and writes deterministic English
client options.

### Display modes

Select the framebuffer strategy with `-PsightlineDisplay` (default `display`):

| Value | Behavior |
| --- | --- |
| `display` | Real `$DISPLAY` / GLFW window. Fails fast on Linux when `$DISPLAY` is unset. |
| `xvfb` | Wraps the client with Loom’s `xvfb-run` path (Linux only). Still boots a real client against a virtual framebuffer; requires `xvfb` installed (`apt install xvfb`). Sets soft-GL-friendly client options and `LIBGL_ALWAYS_SOFTWARE=1`. |

```bash
./gradlew runSightline -Psightline=createWorld -PsightlineDisplay=display
./gradlew runSightline -Psightline=createWorld -PsightlineDisplay=xvfb -PsightlineTimeout=120
```

The default per-step timeout is 30 seconds. Override it when debugging (or for
slow soft-GL CI runs):

```bash
./gradlew runSightline -Psightline=createWorld -PsightlineTimeout=60
```

Results are written to:

- `build/sightline/report.xml` — JUnit XML
- `build/sightline/metrics.json` — wall-clock step timings for perf compare
  (listed in `.gitignore`; CI uploads the file as a workflow artifact)
- `build/sightline/diagnostics.txt` — current screen and visible widgets
- `build/sightline/run/screenshots/` — PNGs from `captureScreenshot`
- `build/sightline/results/<id>/` — per-scenario copies of report, metrics,
  diagnostics, and screenshots written by `runSightlineTests`
- `build/sightline/vanilla-server/` — official dedicated-server run dir from
  `startVanillaServer` (`server-jar.path`, `eula.txt`, `server.properties`,
  `world/`, `logs/harness.log`)

The Gradle process exits non-zero when loading, validation, or execution fails.

### Performance vs main (CI)

The [Sightline Tests](../../.github/workflows/scenario-tests.yml) workflow runs
`runSightlineTests` and uploads `build/sightline/results/*/metrics.json`
(plus the last-run top-level files). On pull requests, a follow-up job
downloads the latest successful `main` run’s artifacts, compares totals and
matching step names with
[`.github/scripts/compare-scenario-perf.py`](../../.github/scripts/compare-scenario-perf.py)
(default: 20% slower **and** more than 50ms), and upserts a single PR comment
marked `<!-- dwm-sightline-perf -->`. The compare is **advisory** — regressions
do not fail CI.

Local fixture check for the compare script:

```bash
.github/scripts/run_compare_scenario_perf_fixtures.sh
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

- `test` — an executable scenario selected with `-Pscenario`.
- `command` — a reusable composite command.

The filename stem is the stable ID. For example,
`subflows/assertAndClick.yaml` defines `assertAndClick`. Duplicate test or
command IDs are rejected even when the files are in different directories.

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
  `build/sightline/run/screenshots/`. With no arguments it uses a vanilla
  timestamped filename. An optional `name` sets the PNG stem. The step waits
  until the file has been written before succeeding.
- `startVanillaServer` — launches Mojang’s official dedicated-server jar as a
  child process (no Fabric/DWM on the server). It writes offline-mode superflat
  settings, waits until `127.0.0.1` accepts TCP connections, and stops the
  process when the scenario finishes. This step does **not** connect the client.
  It uses a 120 second timeout floor; `-PsightlineTimeout` still raises that
  floor when set higher.
- `createWorld` — opens vanilla Create World, applies the given settings, and
  waits until the local player is in the loaded world. Omitted keys use
  test-friendly defaults: superflat, creative, peaceful, commands on. An
  optional `name` overrides vanilla’s “New World”. This step does **not** click
  through the Create World tabs. It uses a 120 second timeout floor;
  `-PsightlineTimeout` still raises that floor when set higher.
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
- `useItem` — waits until a local player, game mode, no open screen, and a
  block crosshair hit exist, then uses the main-hand item on that block (the
  same path as right-click place/interact). It succeeds as soon as the use is
  sent; it does not wait for the world to update. Pair with `waitUntil.block`.
- `runCommand` — waits until a local player and play connection exist, then
  sends an in-game slash command as that player (the same path as typing `/give`
  in chat, without opening the chat GUI). The step succeeds as soon as the
  command packet is sent; it does not wait for success chat or inventory
  updates. Cheats (singleplayer) or operator (dedicated server) are required
  for privileged commands such as `/give`. `startVanillaServer` does not OP
  the player.

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
