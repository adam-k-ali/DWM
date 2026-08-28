# interactWithEntity

Right-click interact with an in-world entity (villagers, interaction hitboxes,
mod entities, etc.).

## Parameters

| Parameter | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `type` | string | yes | — | Namespaced entity type id (for example `minecraft:villager`, `dwm:console_control`) |
| `mode` | string | no | `crosshair` | `crosshair` — entity under the crosshair must match `type`; `nearest` — closest matching entity within `maxDistance` |
| `maxDistance` | number | no | `6` | Search radius for `nearest` mode |
| `hand` | string | no | `main` | `main` or `off` — which hand sends the interact |

## Usage examples

```yaml
- lookAt:
    yaw: 180
    pitch: -10
- interactWithEntity:
    type: dwm:console_control
    mode: crosshair
```

```yaml
- interactWithEntity:
    type: minecraft:villager
    mode: nearest
    maxDistance: 8
```

## Notes

- Requires a local player, game mode, loaded level, and **no open screen**.
- **`crosshair` mode** retries until the crosshair hits an entity whose registry
  id equals `type`. Pair with [`lookAt`](lookAt.md) when several entities share
  the same type. Block hits can occlude small interaction hitboxes — prefer
  **`nearest`** when the target is an invisible interaction entity.
- **`nearest` mode** picks the closest matching entity within `maxDistance`.
- Succeeds as soon as the interact is sent — it does **not** wait for server
  side-effects. Pair with [`waitUntil`](waitUntil.md) when needed.

## Related commands

- [lookAt](lookAt.md)
- [useItem](useItem.md)
- [selectHotbar](selectHotbar.md)
- [runCommand](runCommand.md)
- [waitUntil](waitUntil.md)
