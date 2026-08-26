# setSneaking

Set the client's sneak key state. The state remains active across later steps
until another `setSneaking` step changes it.

## Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `enabled` | boolean | yes | `true` to hold sneak; `false` to release it |

## Usage example

```yaml
- setSneaking:
    enabled: true
- waitTicks: 2
- useItem: air
- setSneaking:
    enabled: false
```

## Notes

- Requires a local player.
- Allow at least one client tick after changing the state before an action that
  reads the player's sneak input.
- Always release sneak before the scenario finishes.

## Related commands

- [useItem](useItem.md)
- [waitTicks](waitTicks.md)
