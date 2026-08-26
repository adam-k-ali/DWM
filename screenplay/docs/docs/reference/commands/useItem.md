# useItem

Use the main-hand item on the currently targeted block, or explicitly use it in
air.

## Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `target` | string | no | `block` (default) or `air` |

A scalar `air` or `block` shorthand sets `target`.

## Usage examples

```yaml
- useItem
```

```yaml
- useItem: air
```

## Notes

- Requires a local player, game mode, and **no open screen**.
- The default `block` target requires a block crosshair hit. `air` invokes the
  main-hand item's normal in-air use path.
- Succeeds as soon as the use is sent — it does **not** wait for the world to
  update. Pair with [`waitUntil`](waitUntil.md) `block`.
- Aim first with [`lookAt`](lookAt.md); select the item with
  [`selectHotbar`](selectHotbar.md) / [`runCommand`](runCommand.md).

## Related commands

- [lookAt](lookAt.md)
- [selectHotbar](selectHotbar.md)
- [runCommand](runCommand.md)
- [waitUntil](waitUntil.md)
- [closeScreen](closeScreen.md)
