# captureScreenshot

Capture the current framebuffer (world and GUI) to a PNG under the client run
screenshots directory (`build/screenplay/run/screenshots/` by default).

## Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `name` | string | no | PNG stem or filename. `.png` is appended if missing. Must be a simple file name (no `/`, `\`, or `..`). Required when `compare` is `true`. |
| `compare` | boolean | no | When `true`, compare the saved PNG against a baseline with the same filename under `screenplay.baselines-dir` (see below). Default off. |
| `maxDiffPixels` | integer | no | Max pixels allowed to differ *beyond* the built-in per-channel color epsilon (`16`). Default `0`. Only meaningful with `compare: true`. |

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
    compare: true
    maxDiffPixels: 0
```

## Screenshot compare / visual regression

Baselines are **not** committed to git. CI (and local runs) supply them via
`-PscreenplayBaselinesDir=<dir>`, which sets the `screenplay.baselines-dir`
system property.

On pull requests, the Screenplay Tests GitHub Actions workflow downloads the latest
successful `main` Screenplay artifact, flattens PNGs into a baselines directory,
and passes that path into `runScreenplayTests`. Successful `main` uploads become
the next PR baseline.

Comparison is **not** raw ARGB equality: each channel may differ by up to `16`
before a pixel counts as different. That absorbs soft CI framebuffer noise
(xvfb/llvmpipe) without extra YAML knobs. `maxDiffPixels` still bounds how many
pixels may exceed that epsilon (edge AA / residual structural budget).

When `compare: true`:

| Condition | Result |
| --- | --- |
| `screenplay.baselines-dir` unset | Compare skipped (capture still succeeds) |
| Baseline PNG missing | Logged as `NO BASELINE`; step succeeds (new named shot) |
| Diff within `maxDiffPixels` (after color epsilon) | Step succeeds |
| Diff over threshold / size mismatch | Step fails; writes `{stem}-diff.png` next to the actual |

## Notes

- The step waits until the file has been written before succeeding.
- Failure surfaces as a scenario error if screenshot capture reports failure.

## Related commands

- [debugScreen](debugScreen.md)
- [launchGame](launchGame.md)
- [createWorld](createWorld.md)
