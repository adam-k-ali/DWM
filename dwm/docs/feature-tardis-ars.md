# Feature: TARDIS Architectural Reconfiguration System (ARS)

See also: [Docs Index](./index.md), [TARDIS Exterior Block](./feature-tardis-block.md), [Building Content System](./feature-building-content.md)

> **Status: in development.** This page covers what exists today: the grid geometry, the piece definition
> registry, and the room and corridor structure templates. **Nothing places these pieces in a TARDIS yet.**
> In-world placement and the console control arrive in later PRs.

## Product Intent
Let a TARDIS crew grow the ship's interior beyond the console room: corridors, crew quarters, a library and the ARS
chamber itself, laid out on a grid so pieces join cleanly and can be removed again.

## Implemented Now
- **Plot grid** (`ArsGrid`): each interior plot is an 8×8 grid of 8×8-block cells with a single floor
  (`y = 0`). Pieces are at most 16 blocks tall. Cells (0..1, 0..2) are reserved for the console room, which joins
  the grid at the west face of cell (2, 1). Rotation counts clockwise quarter turns (`0..3`).
- **Piece definitions:** a synced datapack registry. Entries load from `data/<namespace>/tardis/ars_piece/<name>.json`
  and are sent to clients on join.
- **Structure templates** for eight pieces plus the console annex, under `data/dwm/structure/tardis/ars/`.
- **Layout rules** (`ArsLayoutLogic`): pure logic for placing and removing pieces on the grid; not wired to the
  game yet.

## Piece Definition JSON
```json
{
  "kind": "room",
  "structure": "dwm:tardis/ars/library",
  "cells": [2, 2],
  "connectors": [
    { "cell": [0, 0], "side": "north" },
    { "cell": [1, 1], "side": "east" }
  ],
  "artron_cost": 40,
  "map_color": "#B08D57"
}
```

| Field | Meaning |
| --- | --- |
| `kind` | `room` or `corridor`. |
| `structure` | Structure template id (`data/<ns>/structure/<path>.nbt`). |
| `cells` | Unrotated footprint `[width, depth]` in cells; each `1..8`. |
| `connectors` | Doorways: a footprint-local cell `[x, z]` (unrotated) and a horizontal `side`. |
| `artron_cost` | Cost to build the piece; `>= 0`. |
| `map_color` | `#RRGGBB` colour for the ARS map. |

The codec rejects a definition with:
- `cells` outside `1..8`, or a negative `artron_cost`;
- a `map_color` that is not `#RRGGBB`;
- no connectors, or two identical connectors (same cell and side);
- a connector cell outside the footprint, or a side that is vertical or not on the footprint's outer boundary.

## Structure Contract
Every piece template must follow these rules; `ArsPieceResourceTest` checks them for every shipped piece.

1. **Size:** X = `width × 8`, Z = `depth × 8`, Y ≤ 16. The origin is the min corner of cell (0, 0); the floor is
   the `y = 0` layer and the walking surface is `y = 1`.
2. **Closed shell:** templates are authored with every doorway closed. No air may sit on the outer boundary
   (`x`, `y` or `z` at 0 or max). Blocks outside the shell may be structure void.
3. **Apertures:** for each connector, the aperture is on that cell's outermost block layer on the connector side:
   the 4 blocks at offsets 2..5 along the face, for `y = 1..4` (16 blocks). In the template these are solid,
   full-cube wall blocks (`*_tardis_wall`, `*_roundel_a/b`, `*_big_roundel_a/b`), the removable door plug. The 16
   blocks one step inward are air, and the floor under both is solid. Offsets 2..5 are centred in the cell, so the
   aperture lands in the same place whichever way a piece is rotated.
4. **Walkable:** every doorway's inner space is connected to every other one through 2-high air (`y = 1..2`).
5. **Lit:** each piece contains light-emitting blocks (roundels give light 10, ceiling vents 12).
6. **Deterministic:** the same layout code always produces the same NBT.

## Pieces

| Piece | Kind | Cells | Template (X×Y×Z) | Connectors (rotation 0) | Artron | Map colour |
| --- | --- | --- | --- | --- | --- | --- |
| `dwm:corridor_straight` | corridor | 1×1 | 8×6×8 | north, south | 5 | `#B8C4D0` |
| `dwm:corridor_corner` | corridor | 1×1 | 8×6×8 | north, east | 5 | `#B8C4D0` |
| `dwm:corridor_t` | corridor | 1×1 | 8×6×8 | west, north, east | 5 | `#B8C4D0` |
| `dwm:corridor_cross` | corridor | 1×1 | 8×6×8 | north, east, south, west | 5 | `#B8C4D0` |
| `dwm:corridor_dead_end` | corridor | 1×1 | 8×6×8 | north | 5 | `#B8C4D0` |
| `dwm:bedroom` | room | 2×1 | 16×6×8 | cell (0, 0) north | 20 | `#7FA8D9` |
| `dwm:library` | room | 2×2 | 16×10×16 | cell (0, 0) north; cell (1, 1) east | 40 | `#B08D57` |
| `dwm:ars_chamber` | room | 3×3 | 24×14×24 | cell (1, 0) north; cell (1, 2) south | 80 | `#E0B341` |

- **Corridors:** white roundel passages, 4 wide and 4 high, matching the console room. Arms run only toward the
  piece's connectors. The dead end is a short stub ending in a wall of big roundels, with two columns and the
  TARDIS globe.
- **Bedroom:** light blue crew quarters. A double bed between nightstands with lanterns, a writing desk between
  short bookcases, chairs, chests and a glass partition between columns.
- **Library:** brown, 10 blocks tall. Bookshelf walls split into bays by roundel pilasters and bands, two
  free-standing shelf rows, a reading table with chairs and lecterns on a rug, a reading nook with the globe, and
  hanging lanterns.
- **ARS chamber:** yellow, 14 blocks tall. A waxed copper tree rises from a railed quartz dais: root flares,
  eight branches spiralling out and up, glowing orbs hung from them on chains, and a sea-lantern heart. It is
  decorative only.

### Console annex
`dwm:tardis/ars/console_annex` is a fixed 5×6×9 lobby, not a registry piece. It sits at plot-local (11, 0, 6), just
east of the console room's east wall (plot-local `x = 10`), which closes its open west side. Its east wall has a
permanently open 4×4 doorway at annex `z = 4..7`, `y = 1..4`, which lines up with the west aperture of cell (2, 1).
The closed-shell rule does not apply to its west face or east doorway.

## Regenerating Templates
Templates are not built in game. They are described in Java in the test source set
(`dwm/src/test/java/com/adamkali/dwm/world/structure/TardisArsTemplates.java`) and written to `.nbt`:

```bash
./dwm/gradlew -p dwm generateStructureTemplates   # rewrite the committed .nbt files
./dwm/gradlew -p dwm test                         # drift check + ARS resource contract
```

`StructureTemplateGeneratorTest` fails if a committed template no longer matches its layout. The client GameTest
`TardisArsTemplatesClientGameTest` places each template on its own and saves interior screenshots
(`ars_<name>*.png`) to `dwm/build/run/clientGameTest/screenshots/`.

## Known Constraints
- No in-world placement, removal or console control yet; those arrive in later PRs.
- Pieces share a single floor; there are no multi-level layouts.

## Future Opportunities
- More rooms as plain data and templates.
