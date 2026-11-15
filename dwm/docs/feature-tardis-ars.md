# Feature: TARDIS Architectural Reconfiguration System (ARS)

See also: [Docs Index](./index.md), [TARDIS Exterior Block](./feature-tardis-block.md), [Building Content System](./feature-building-content.md)

> **Status: in development.** Pieces can be grown and jettisoned in a TARDIS interior with the ops command
> `/tardis ars`. Only the console control and the ARS screen remain to be built.

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
- **Layout rules** (`ArsLayoutLogic`): pure logic for placing and removing pieces on the grid.
- **Growing and jettisoning** pieces in the interior (`ArsService`, `ArsRoomPlacer`), with the console annex and
  console doorway, and the ops command `/tardis ars`.

## Growing and Jettisoning
A TARDIS's layout is the list of placed pieces in its saved data (`arsPieces`: piece id, anchor cell, rotation).
`ArsService` checks the rules, changes the world and updates that list. Every message it returns is a translation
key (`dwm.ars.*`); piece names are `dwm.ars.piece.<name>`.

**Growing a piece** at an anchor cell with a rotation is refused, with nothing changed, when:
- the TARDIS is in flight, or its console room has not been generated yet;
- the layout rules refuse: unknown piece, rotation outside `0..3`, outside the grid, on the console room's cells,
  overlapping a piece, not connected to the console room (directly or through connected pieces), or not enough
  artron;
- the piece's structure template is missing.

Otherwise it spends the piece's artron cost (free in creative), clears the piece's cells to full height (so blocks
left by a piece whose datapack definition was removed do not linger), places the template rotated so its footprint
starts at the anchor cell, and opens every doorway the piece now shares: both 4×4 wall plugs, the piece's own and the
neighbour's facing one, become air. Unmatched connectors keep their plugs.

**Jettisoning** the piece covering a cell is refused, with nothing changed, when:
- the TARDIS is in flight or has no console room;
- there is no piece there, or another piece is only reachable through it;
- a player is inside the piece, or inside the annex when it is the last piece.

Otherwise everything in the piece is destroyed: blocks, container contents and non-player entities, with no
drops. Each neighbour that shared a doorway gets its own wall plug back (its template is re-placed, clipped to that
4×4 layer). Half the cost, rounded down, is refunded.

### Console annex and doorway
Growing the first piece also places the console annex (below) and carves a 2-wide, 3-high doorway through the console
room's east wall at plot-local `x = 10`, `z = 7..8`, `y = 1..3`. Jettisoning the last piece clears the annex and puts
the console room's wall back from its template. Re-placing the console room (first entry, deferred preload or
`/tardis rebuild`) re-opens the doorway whenever the layout has a piece.

`/tardis rebuild` only rebuilds the console room. It does not re-place ARS rooms or the annex.

### Command
Ops (permission 2), standing inside the TARDIS interior to change:

```
/tardis ars place <piece> <cellX> <cellZ> [rotation]   # rotation 0..3, default 0
/tardis ars remove <cellX> <cellZ>
```

`<piece>` suggests every registered piece id. A player in creative mode builds for free. The command replies with
the result or the reason it was refused.

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
- No console control or ARS screen yet; pieces are placed with `/tardis ars`.
- Room blocks are not protected: players can still break walls by hand.
- Removing a piece whose datapack definition no longer exists is not possible; its blocks stay until a new piece is
  grown over its cells.
- Pieces share a single floor; there are no multi-level layouts.

## Future Opportunities
- More rooms as plain data and templates.
