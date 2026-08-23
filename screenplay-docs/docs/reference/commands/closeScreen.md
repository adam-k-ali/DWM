# closeScreen

Close the current GUI (`setScreen(null)` / `closeContainer()`).

## Parameters

None.

## Usage examples

```yaml
- closeScreen
```

## Notes

- Waits until a local player exists.
- If no screen is open, the step succeeds without changing anything.
- World actions such as [`useItem`](useItem.md) and [`walkUntil`](walkUntil.md)
  require no open screen.

## Related commands

- [openInventory](openInventory.md)
- [useItem](useItem.md)
- [walkUntil](walkUntil.md)
