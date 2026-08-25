# Selectors

Selectors identify UI widgets or the current screen for
[`assertVisible`](commands/assertVisible.md), [`click`](commands/click.md),
[`waitUntil`](commands/waitUntil.md) visibility conditions, and composites such
as [`assertAndClick`](commands/assertAndClick.md).

## Shape

Only two fields are allowed; both are required and non-empty:

| Field | Description |
| --- | --- |
| `type` | One of the types below |
| `name` | Exact match string for that type |

Unknown fields are rejected at compile/validate time.

```yaml
- assertVisible:
    type: button
    name: "Singleplayer"
```

A single-item list form is equivalent:

```yaml
- assertVisible:
  - type: button
    name: "Singleplayer"
```

## Types

| `type` | Matches |
| --- | --- |
| `button` | Visible `Button` whose message equals `name` |
| `cycle` | Visible `CycleButton` whose message equals `name` |
| `tab` | Visible `TabButton` whose message equals `name` |
| `editbox` | Visible `EditBox` whose accessibility narration label (`getMessage()`) equals `name` — not the current field value |
| `label` | Visible `StringWidget` message, or painted status text on the vanilla connect screen (for example `"Connecting to the server..."`) |
| `screen` | Current GUI’s Java **simple class name** equals `name` (for example `LevelLoadingScreen`, not the fully qualified name) |

First match wins among visible widgets.

## Rules of thumb

- Prefer localized UI text as shown in English client options (the harness
  writes deterministic English options).
- Direct Connection’s IP field is `"Server Address"`.
- [`click`](commands/click.md) cannot target `type: screen` (validation error).
- [`click`](commands/click.md) also cannot activate painted connect-screen
  status text matched only as a `label`; it only clicks real widgets.
- Use [`debugScreen`](commands/debugScreen.md) to dump visible widgets while
  authoring selectors.
