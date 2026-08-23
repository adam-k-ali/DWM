# lookAt

Aim the local player’s camera. Supply either a rotation or block coordinates —
not both.

## Parameters

### Rotation mode

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `yaw` | number | yes | Yaw degrees |
| `pitch` | number | yes | Pitch degrees, clamped to **−90…90** |

### Position mode

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `x` | int or relative string | yes | Absolute int or `"~"` / `"~N"` / `"~-N"` |
| `y` | int or relative string | yes | Same |
| `z` | int or relative string | yes | Same |

Relative values use the player’s block position. Quote `"~"` in YAML; a bare
`~` is YAML null.

## Usage examples

```yaml
- lookAt:
    yaw: 90
    pitch: 45
```

```yaml
- lookAt:
    x: "~1"
    y: "~-1"
    z: "~"
```

## Notes

- Waits until a local player exists, then applies the look immediately.
- Position mode aims at the block center from eye height.

## Related commands

- [useItem](useItem.md)
- [walkUntil](walkUntil.md)
- [waitUntil](waitUntil.md)
