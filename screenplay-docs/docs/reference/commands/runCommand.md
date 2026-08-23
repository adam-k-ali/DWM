# runCommand

Send an in-game slash command as the local player (same path as typing `/give`
in chat, without opening the chat GUI).

## Parameters

Provide exactly one of:

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `command` | string | one of | Command text |
| `text` | string | one of | Alias for `command` (used by scalar shorthand) |

Leading `/` is stripped. Length must be ≤ 256 and non-blank after normalize.

## Usage examples

```yaml
- runCommand: "/give @s minecraft:diamond 1"
```

```yaml
- runCommand:
    command: "/give @s minecraft:diamond 1"
```

## Notes

- Succeeds as soon as the command packet is sent — it does **not** wait for
  success chat or inventory updates. Pair with [`waitUntil`](waitUntil.md).
- Cheats (singleplayer) or operator (dedicated server) are required for
  privileged commands. [`startVanillaServer`](startVanillaServer.md) does not
  OP the player.

## Related commands

- [waitUntil](waitUntil.md)
- [selectHotbar](selectHotbar.md)
- [useItem](useItem.md)
