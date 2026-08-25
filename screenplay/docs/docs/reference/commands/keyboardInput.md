# keyboardInput

Wait until a focused, editable text field can consume input, then type the given
string once via real `charTyped` events.

## Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `text` | string | yes | Non-empty string to type |

A scalar string shorthand sets `text`.

## Usage examples

```yaml
- keyboardInput: "localhost:25565"
```

```yaml
- keyboardInput:
    text: "localhost:25565"
```

## Notes

- Does **not** click or focus the field — click the matching `editbox` first
  (often via [`assertAndClick`](assertAndClick.md)).
- Retries while no focused editable `EditBox` is available.
- Throws if a codepoint is rejected by the field.

## Related commands

- [click](click.md)
- [assertAndClick](assertAndClick.md)
- [Selectors](../selectors.md)
