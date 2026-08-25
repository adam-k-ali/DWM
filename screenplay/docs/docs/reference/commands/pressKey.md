# pressKey

Press a keyboard key once through Minecraft's normal key handling path. Use for
in-game keybinds (for example opening a mod screen) or global keys such as
Escape to open the pause menu.

## Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `key` | string | yes | Key name (see supported values below) |

A scalar string shorthand sets `key`:

```yaml
- pressKey: g
```

## Supported keys (v1)

| Name | Effect |
| --- | --- |
| `g`–`z` (single letter) | Dispatches through `KeyMapping.click` for bound keys |
| `escape`, `esc` | Screen `keyPressed` when a GUI is open; otherwise pause-menu keybind |
| `space` | Space bar |

Unsupported names fail validation before the client boots the scenario.

## Usage examples

```yaml
- pressKey: g
```

```yaml
- pressKey:
    key: escape
```

```yaml
- closeScreen
- pressKey: escape
- waitUntil:
    visible:
      type: screen
      name: PauseScreen
```

## Notes

- Pair with [`waitUntil`](waitUntil.md) when the key opens a screen asynchronously
  (keybind handlers run on later client ticks).
- Does **not** type into text fields — use [`keyboardInput`](keyboardInput.md) for
  `EditBox` entry.
- When a screen is open and handles the key (for example Escape closing a dialog),
  the screen receives `keyPressed` first.

## Related commands

- [keyboardInput](keyboardInput.md)
- [closeScreen](closeScreen.md)
- [assertAndClick](assertAndClick.md)
