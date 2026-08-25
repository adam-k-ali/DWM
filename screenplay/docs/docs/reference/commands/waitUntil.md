# waitUntil

Poll until **exactly one** condition is true.

## Parameters

Provide exactly one of the following keys:

| Key | Value | Description |
| --- | --- | --- |
| `visible` | selector object | Matching widget/screen is visible (same as [`assertVisible`](assertVisible.md)) |
| `notVisible` | selector object | No match on the current tick (including if it has never appeared) |
| `holding` | item id string or `{id: ...}` | Main hand holds the item (`minecraft:` may be omitted; empty hand is `minecraft:air`) |
| `block` | `{id, x, y, z}` | Block id at coordinates (relative/absolute rules same as [`lookAt`](lookAt.md)) |

## Usage examples

```yaml
- waitUntil:
    visible:
      type: button
      name: "Singleplayer"
```

```yaml
- waitUntil:
    notVisible:
      type: screen
      name: LevelLoadingScreen
```

```yaml
- waitUntil:
    holding: minecraft:dirt
```

```yaml
- waitUntil:
    block:
      id: minecraft:dirt
      x: "~1"
      y: "~"
      z: "~"
```

## Notes

- Retries each tick until the step timeout.
- Quote relative coords: `"~"` not bare `~`.

## Related commands

- [assertVisible](assertVisible.md)
- [runCommand](runCommand.md)
- [useItem](useItem.md)
- [walkUntil](walkUntil.md)
- [Selectors](../selectors.md)
