# useItem

Use the main-hand item on the currently targeted block (same path as right-click
place / interact).

## Parameters

None.

## Usage examples

```yaml
- useItem
```

## Notes

- Requires a local player, game mode, **no open screen**, and a block crosshair
  hit (`hitResult` is a block).
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
