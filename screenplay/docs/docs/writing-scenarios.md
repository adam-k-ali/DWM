# Writing scenarios

YAML definitions live recursively under your configured tests directory (default
`src/screenplayTests/resources/tests/`). Directory layout does not determine
role; the frontmatter `type` does.

## Frontmatter

```yaml
---
name: Create World
type: test
---
steps:
  - launchGame
```

| `type` | Role |
| --- | --- |
| `test` | Executable scenario selected with `-Pscreenplay=<id>` |
| `command` | Reusable composite command invoked by filename stem |
| `suite` | One-client group of tests with shared hooks |

The **filename stem** is the stable ID. For example,
`subflows/assertAndClick.yaml` defines `assertAndClick`. Duplicate test,
command, or suite IDs are rejected even when files live in different
directories.

## Suites

A `type: suite` document runs member tests in **one** Minecraft client session
with xUnit-style hooks:

```yaml
---
name: Creative World Suite
type: suite
---
before-all:
  - launchGame
  - createWorld:
      worldType: superflat
      gameMode: creative
  - closeScreen
before-each:
  - runCommand: "/time set day"
after-each:
  - closeScreen
after-all:
  - captureScreenshot:
      name: suite-end
tests:
  - suitePlaceDirt
  - suiteCaptureInventory
```

| Key | Required | Role |
| --- | --- | --- |
| `tests` | yes | Non-empty list of `type: test` filename stems |
| `before-all` | no | Once before the first member |
| `before-each` | no | Before every member body |
| `after-each` | no | After every member body (still runs if that member failed) |
| `after-all` | no | Once after the last member (still runs after failures) |

Execution order:

```
before-all
  for each member:
    before-each
    test body
    after-each
after-all
```

Suites may not declare `steps` or `parameters`. Member tests are normal
`type: test` documents (often body-only when the suite owns world setup).
On failure, remaining members are skipped, `after-all` still runs, and the
process exits non-zero.

## Steps

Each item under `steps` is either:

- A bare primitive name with no arguments (`launchGame`)
- A map keyed by primitive or composite ID with arguments

```yaml
steps:
  - launchGame
  - assertVisible:
      type: button
      name: "Singleplayer"
  - click:
      type: button
      name: "Singleplayer"
```

A single-item list form is also accepted for selector-style arguments:

```yaml
- assertVisible:
  - type: tab
    name: "World"
```

Scalar shorthands map to a `text` field for some commands (for example
`keyboardInput: "hello"` or `waitTicks: 25`). See each command page for details.

## Selectors

Widget and screen matching uses exact `name` plus one of:

`button`, `cycle`, `tab`, `editbox`, `label`, `screen`

Full rules: [Selectors](reference/selectors.md).

## Composite commands

A `type: command` document declares string parameters and uses them in nested
steps via `{{ param }}` templates:

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

Invoke by filename ID:

```yaml
- assertAndClick:
  - type: button
    name: "Singleplayer"
```

Screenplay ships [`assertAndClick`](reference/commands/assertAndClick.md) as a
built-in composite. Unknown steps, missing or extra parameters, unsupported
types, malformed documents, and recursive command cycles fail **before** the
client flow begins.
