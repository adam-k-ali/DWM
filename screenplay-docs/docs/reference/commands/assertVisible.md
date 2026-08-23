# assertVisible

Wait until a [selector](../selectors.md) matches a visible widget or the current
screen. Retries each client tick until success or the step timeout.

## Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `type` | string | yes | Selector type |
| `name` | string | yes | Exact match string |

Accepts a direct object or a single-item list.

## Usage examples

```yaml
- assertVisible:
    type: button
    name: "Singleplayer"
```

```yaml
- assertVisible:
  - type: screen
    name: LevelLoadingScreen
```

## Notes

- Does not click or change UI state.
- Equivalent visibility condition: [`waitUntil`](waitUntil.md) with `visible`.
- Use [`debugScreen`](debugScreen.md) if a selector never matches.

## Related commands

- [click](click.md)
- [assertAndClick](assertAndClick.md)
- [waitUntil](waitUntil.md)
- [debugScreen](debugScreen.md)
