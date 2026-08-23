# captureScreenshot

Capture the current framebuffer (world and GUI) to a PNG under the client run
screenshots directory (`build/screenplay/run/screenshots/` by default).

## Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `name` | string | no | PNG stem or filename. `.png` is appended if missing. Must be a simple file name (no `/`, `\`, or `..`). |

With no arguments, Minecraft chooses a vanilla timestamped filename.

## Usage examples

```yaml
- captureScreenshot
```

```yaml
- captureScreenshot:
    name: world-ready.png
```

```yaml
- captureScreenshot:
    name: after-world-tab
```

## Notes

- The step waits until the file has been written before succeeding.
- Tiny PNGs (under ~40KB at the default 854×480 window) are treated as blank/black frames.
  Screenplay retries capture for a short settle window, then fails the step if the frame stays empty.
- `runScreenplayTests` clears `build/screenplay/run/screenshots/` before each scenario and
  fails the suite when archived PNGs still look blank, so CI does not upload leftover or black demos.

## Related commands

- [debugScreen](debugScreen.md)
- [launchGame](launchGame.md)
