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
| `f1` | Hides the HUD and first-person hand (idempotent; for stable screenshot compares) |

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

```yaml
# Hide HUD + hand before an in-world visual-regression capture
- pressKey: f1
- waitTicks: 2
- captureScreenshot:
    name: portal-view
    compare: true
    maxDiffPixels: 5000
```

## Notes

- Pair with [`waitUntil`](waitUntil.md) when the key opens a screen asynchronously
  (keybind handlers run on later client ticks).
- Does **not** type into text fields — use [`keyboardInput`](keyboardInput.md) for
  `EditBox` entry.
- When a screen is open and handles the key (for example Escape closing a dialog),
  the screen receives `keyPressed` first.
- `f1` sets the HUD hidden (does not toggle back on). Use before
  [`captureScreenshot`](captureScreenshot.md) compares so player skin / hotbar
  chrome cannot dominate the pixel budget.

## Related commands

- [keyboardInput](keyboardInput.md)
- [closeScreen](closeScreen.md)
- [assertAndClick](assertAndClick.md)
