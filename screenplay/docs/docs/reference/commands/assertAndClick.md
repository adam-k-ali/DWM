# assertAndClick

Assert that a [selector](../selectors.md) is visible, then click it. This is a
shipped **composite** (`type: command`), not a Java primitive — the compiler
inlines the nested steps.

## Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `type` | string | yes | Selector type (`button`, `cycle`, `tab`, `editbox`, `label`) |
| `name` | string | yes | Exact selector name |

Expands to:

1. [`assertVisible`](assertVisible.md) with the same selector
2. [`click`](click.md) with the same selector

## Usage examples

```yaml
- assertAndClick:
  - type: button
    name: "Singleplayer"
```

```yaml
- assertAndClick:
    type: editbox
    name: "Server Address"
```

## Notes

- `type: screen` is invalid for the click half of this composite.
- Definition: `assertAndClick.yaml` under Screenplay’s library tests resources
  (filename stem = command ID).
- Author custom composites as described in [Writing scenarios](../../writing-scenarios.md).

## Related commands

- [assertVisible](assertVisible.md)
- [click](click.md)
- [debugScreen](debugScreen.md)
