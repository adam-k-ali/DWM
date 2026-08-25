# walkUntil

Hold forward movement (`keyUp`) until one end condition is met. The forward key
is released when the step succeeds.

## Parameters

Provide exactly one mode:

### Position mode

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `x` | int or relative string | yes | Absolute or `"~"` / `"~N"` |
| `y` | int or relative string | yes | Same |
| `z` | int or relative string | yes | Same |

Re-aims at the target each tick. Relative values use the player’s block
position. Quote `"~"` in YAML.

### Dimension mode

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `dimension` | string | yes | Namespaced world id (for example `dwm:tardis`) |

## Usage examples

```yaml
- walkUntil:
    x: "~3"
    y: "~"
    z: "~"
```

```yaml
- walkUntil:
    dimension: dwm:tardis
```

## Notes

- Requires a local player, level, and **no open screen** — call
  [`closeScreen`](closeScreen.md) first if needed.
- Uses the default per-step timeout; a blocked path can time out while still
  holding forward until failure cleanup.

## Related commands

- [closeScreen](closeScreen.md)
- [lookAt](lookAt.md)
- [waitUntil](waitUntil.md)
