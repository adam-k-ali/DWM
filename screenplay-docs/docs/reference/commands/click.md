# click

Wait for an active matching widget and dispatch a real screen mouse click at its
center.

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

- Retries while the widget is missing or `active == false`.
- `type: screen` is rejected at validation time.
- Prefer [`assertAndClick`](assertAndClick.md) when you want an explicit assert
  before the click.

## Related commands

- [assertVisible](assertVisible.md)
- [assertAndClick](assertAndClick.md)
- [keyboardInput](keyboardInput.md)
- [Selectors](../selectors.md)
