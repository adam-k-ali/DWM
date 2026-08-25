# waitTicks

Wait until the client world’s game time has advanced by the given number of
ticks.

## Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `ticks` | int | yes | Positive integer ≥ 1 |

A scalar positive integer shorthand sets `ticks`.

## Usage examples

```yaml
- waitTicks: 25
```

```yaml
- waitTicks:
    ticks: 25
```

## Notes

- Requires `client.level`.
- Use for short animations that have no [`waitUntil`](waitUntil.md) condition
  (for example a door swing).

## Related commands

- [waitUntil](waitUntil.md)
- [useItem](useItem.md)
