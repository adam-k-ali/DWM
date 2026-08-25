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

The **filename stem** is the stable ID. For example,
`subflows/assertAndClick.yaml` defines `assertAndClick`. Duplicate test or
command IDs are rejected even when files live in different directories.

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
