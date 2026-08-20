# YAML client scenarios

This source set contains a local-only Fabric test mod that starts the real
Minecraft client and drives its widgets from YAML scenarios. It is not included
in the production mod jar.

## Running a scenario

Run a test by its YAML filename without the extension:

```bash
./gradlew runScenarioTest -Pscenario=createWorld
```

The client uses an isolated directory under `build/scenario-test/run`. Before
each run, the harness removes its saved worlds, clears
`build/scenario-test/vanilla-server/world`, and writes deterministic English
client options. A display is required.

The default per-step timeout is 30 seconds. Override it when debugging:

```bash
./gradlew runScenarioTest -Pscenario=createWorld -PscenarioTimeout=60
```

Results are written to:

- `build/scenario-test/report.xml` — JUnit XML
- `build/scenario-test/diagnostics.txt` — current screen and visible widgets
- `build/scenario-test/run/screenshots/` — PNGs from `captureScreenshot`
- `build/scenario-test/vanilla-server/` — official dedicated-server run dir from
  `startVanillaServer` (`server-jar.path`, `eula.txt`, `server.properties`,
  `world/`, `logs/harness.log`)

The Gradle process exits non-zero when loading, validation, or execution fails.

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
  `build/scenario-test/run/screenshots/`. With no arguments it uses a vanilla
  timestamped filename. An optional `name` sets the PNG stem. The step waits
  until the file has been written before succeeding.
- `startVanillaServer` — launches Mojang’s official dedicated-server jar as a
  child process (no Fabric/DWM on the server). It writes offline-mode superflat
  settings, waits until `127.0.0.1` accepts TCP connections, and stops the
  process when the scenario finishes. This step does **not** connect the client.
  It uses a 120 second timeout floor; `-PscenarioTimeout` still raises that
  floor when set higher.
- `keyboardInput` — waits until a focused, editable text field can consume
  input, then types the given string once via real `charTyped` events. It does
  not click or select the field; click the matching `editbox` first.
- `waitUntil` — polls until a nested selector is `visible` or `notVisible`.
  Exactly one condition is required. `visible` is equivalent to `assertVisible`.
  `notVisible` succeeds as soon as the selector does not match on the current
  tick, including if it has not appeared yet.

```yaml
- waitUntil:
    visible:
      type: button
      name: "Singleplayer"
- waitUntil:
    notVisible:
      type: screen
      name: LevelLoadingScreen
```

```yaml
- startVanillaServer
- startVanillaServer:
    port: 25565
```

```yaml
- keyboardInput: "localhost:25565"
- keyboardInput:
    text: "localhost:25565"
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
