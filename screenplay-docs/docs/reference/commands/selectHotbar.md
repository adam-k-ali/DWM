# selectHotbar

Select hotbar slot `0`–`8` and sync that slot to the server.

## Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `slot` | int | yes | Hotbar index **0–8** |

A scalar integer shorthand sets `slot`.

## Usage examples

```yaml
- selectHotbar: 0
```

```yaml
- selectHotbar:
    slot: 3
```

## Notes

- Waits until a local player and play connection exist.

## Related commands

- [runCommand](runCommand.md)
- [useItem](useItem.md)
- [waitUntil](waitUntil.md)
