# debugScreen

Immediately log the current screen class and every visible widget (type, name,
active, bounds). Always succeeds on the tick it runs.

## Parameters

None.

## Usage examples

```yaml
- debugScreen
```

## Notes

- Side effect only: writes an INFO diagnostics dump (also useful when
  `diagnostics.txt` is captured after failures).
- Use while authoring [selectors](../selectors.md).

## Related commands

- [assertVisible](assertVisible.md)
- [click](click.md)
- [assertAndClick](assertAndClick.md)
