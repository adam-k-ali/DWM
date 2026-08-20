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
each run, the harness removes its saved worlds and writes deterministic English
client options. A display is required.

The default per-step timeout is 30 seconds. Override it when debugging:

```bash
./gradlew runScenarioTest -Pscenario=createWorld -PscenarioTimeout=60
```

Results are written to:

- `build/scenario-test/report.xml` — JUnit XML
- `build/scenario-test/diagnostics.txt` — current screen and visible widgets
- `build/scenario-test/run/screenshots/` — PNGs from `captureScreenshot`

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

```yaml
- captureScreenshot
- captureScreenshot:
    name: after-world-tab
```

Selectors require an exact rendered `name` and one of these types:

- `button`
- `cycle`
- `tab`

```yaml
steps:
  - assertVisible:
    - type: tab
      name: "World"
  - click:
    - type: tab
      name: "World"
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
