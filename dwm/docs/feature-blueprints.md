# Feature: Blueprints

See also: [Docs Index](./index.md) · [Building Content](./feature-building-content.md)

## Product Intent
Let operators paste geometric structures into a world from JSON files stored with that world’s save, without hand-placing every block.

## Player Outcomes
- Ops place named blueprints at a chosen origin with one command.
- Blueprint authors iterate by editing JSON under the world save folder (no mod restart).

## Implemented Now
- Command (permission level 2 / gamemasters): `/blueprint place <name> <origin>`
- Blueprints live at `<world save>/blueprints/<name>.json` (folder created on server start).
- Shape types: `circle`, `rectangle`, `sphere`, `polygon`, `dome` (see schema below).
- Coordinates in the JSON are **relative to the command origin**.
- Later shapes in `shapes[]` overwrite earlier ones at the same voxel.
- Unknown block ids and invalid JSON fail the command before any blocks are placed.
- Hard cap of 250000 voxels per placement.

## How It Works In-Game
1. Create or open a world.
2. Drop a JSON file into `<save>/blueprints/`, e.g. `platform.json`.
3. As an op, run: `/blueprint place platform ~ ~ ~`
4. Blocks appear at the origin plus each shape’s relative offsets.

### Sample blueprint

```json
{
  "shapes": [
    {
      "type": "rectangle",
      "from": { "x": 0, "y": 0, "z": 0 },
      "to": { "x": 4, "y": 0, "z": 4 },
      "block": { "id": "minecraft:stone" },
      "fill": true
    },
    {
      "type": "circle",
      "origin": { "x": 2, "y": 1, "z": 2 },
      "block": { "id": "minecraft:oak_planks" },
      "filled": true,
      "radius": 2
    }
  ]
}
```

### Geometry notes
| Type | Plane / volume | Fill flag |
|------|----------------|-----------|
| `circle` | XZ disk at `origin.y` | `filled` |
| `rectangle` | Axis-aligned cuboid (`from`–`to`, inclusive) | `fill` (hollow = six faces) |
| `sphere` | Full ball | `fill` |
| `dome` | Upper hemisphere (`y >= origin.y`) | `fill` |
| `polygon` | Regular 5–12-gon in XZ (`n_sides`, circumradius) | `fill` |

Optional `block.state.facing`: `north` / `south` / `east` / `west` / `up` / `down`. Applied when the block has a compatible `facing` property; otherwise ignored.

JSON Schema for authoring/validation: `dwm/src/test/resources/schemas/blueprint.schema.json`.

## Known Constraints
- Op-only command; not a survival item or wand.
- Default blueprints are not shipped into new worlds.
- Only `facing` is supported among blockstate properties.
- No export of a world region back to blueprint JSON.

## Future Opportunities
- Builder starter kits that copy example JSON into `blueprints/`.
- Survival-facing tools that consume the same format.
