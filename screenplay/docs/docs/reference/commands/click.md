# click

Wait for an active matching widget and dispatch `mouseClicked` on that widget at
its center.

## Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `type` | string | yes | Selector type — **not** `screen` |
| `name` | string | yes | Exact widget name |

## Usage examples

```yaml
- click:
    type: button
    name: "Multiplayer"
```

```yaml
- click:
  - type: tab
    name: "World"
```

## Notes

- Retries while the widget is missing, `active == false`, or the widget ignores
  the click.
- The event is sent to the matched widget, not via `Screen.getChildAt`. Vanilla
  hit-testing returns the first overlapping active child, and `Screen.mouseClicked`
  reports success even when that child does not handle the press.
- `type: screen` is rejected at validation time.
- Prefer [`assertAndClick`](assertAndClick.md) when you want an explicit assert
  before the click.

## Related commands

- [assertVisible](assertVisible.md)
- [assertAndClick](assertAndClick.md)
- [keyboardInput](keyboardInput.md)
- [Selectors](../selectors.md)
