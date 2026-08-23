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
- Failure surfaces as a scenario error if screenshot capture reports failure.

## Related commands

- [debugScreen](debugScreen.md)
- [launchGame](launchGame.md)
