# createWorld

Open vanilla Create World, apply the given settings, and wait until the local
player is in the loaded world.

## Parameters

| Parameter | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `worldType` | string | no | `flat` | World preset aliases such as `flat` / `superflat`, `default` / `normal`, `largebiomes`, `amplified`, `singlebiome`, or a full preset id |
| `gameMode` | string | no | `creative` | `survival`, `hardcore`, or `creative` |
| `difficulty` | string | no | `peaceful` | `peaceful`, `easy`, `normal`, or `hard` |
| `allowCommands` | boolean | no | `true` | Whether cheats are enabled |
| `name` | string | no | vanilla default | Overrides “New World” when set (non-empty) |

## Usage examples

```yaml
- createWorld
```

```yaml
- createWorld:
    worldType: superflat
    gameMode: creative
    difficulty: peaceful
    allowCommands: true
    name: Scenario World
```

## Notes

- Uses a **120 second** timeout floor; `-PscreenplayTimeout` raises it when set
  higher.
- Allowed once per scenario.
- Does **not** click through Create World tabs manually — it drives the process
  APIs used by the harness.
- Completes when the player and level are ready and loading GUIs are gone.

## Related commands

- [launchGame](launchGame.md)
- [openInventory](openInventory.md)
- [closeScreen](closeScreen.md)
- [captureScreenshot](captureScreenshot.md)
