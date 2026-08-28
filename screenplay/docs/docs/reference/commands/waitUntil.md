# waitUntil

Poll until **exactly one** condition is true.

## Parameters

Provide exactly one of the following keys:

| Key | Value | Description |
| --- | --- | --- |
| `visible` | selector object | Matching widget/screen is visible (same as [`assertVisible`](assertVisible.md)) |
| `notVisible` | selector object | No match on the current tick (including if it has never appeared) |
| `holding` | item id string or `{id: ...}` | Main hand holds the item (`minecraft:` may be omitted; empty hand is `minecraft:air`) |
| `notHolding` | item id string or `{id: ...}` | Main hand does **not** hold the item |
| `block` | `{id, x, y, z}` | Block id at coordinates (relative/absolute rules same as [`lookAt`](lookAt.md)) |
| `overlay` | string | Action bar overlay text contains the string (English client text) |
| `toast` | object | A toast is visible matching the selector (see below) |

### Toast selector

| Key | Required | Description |
| --- | --- | --- |
| `type` | yes | Currently only `advancement` |
| `contains` | no* | Substring match on the advancement toast title |
| `id` | no* | Advancement id (for example `minecraft:dwm/first_circuit`; `minecraft:` namespace may be omitted) |

\* Provide at least one of `contains` or `id`.

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
    notHolding: dwm:circuit_stabilisers
```

```yaml
- waitUntil:
    block:
      id: minecraft:dirt
      x: "~1"
      y: "~"
      z: "~"
```

```yaml
- waitUntil:
    overlay: "This circuit is broken"
```

```yaml
- waitUntil:
    toast:
      type: advancement
      contains: "Spare Parts"
```

## Notes

- Retries each tick until the step timeout.
- Quote relative coords: `"~"` not bare `~`.
- `overlay` reads the vanilla action bar message (same channel used by mod circuit feedback).
- `toast` inspects visible advancement toasts only.

## Related commands

- [assertVisible](assertVisible.md)
- [runCommand](runCommand.md)
- [useItem](useItem.md)
- [walkUntil](walkUntil.md)
- [Selectors](../selectors.md)
