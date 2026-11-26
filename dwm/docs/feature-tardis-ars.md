# Feature: TARDIS Architectural Reconfiguration System (ARS)

See also: [Docs Index](./index.md), [TARDIS Exterior Block](./feature-tardis-block.md), [Building Content System](./feature-building-content.md)

## Product Intent
Let a TARDIS crew grow the ship's interior beyond the console room: corridors, crew quarters, a library and the ARS
chamber itself, laid out on a grid so pieces join cleanly and can be removed again.

## Implemented Now
- **Plot grid** (`ArsGrid`): each interior plot's 10000×10000 window is a 1250×1250 grid of 8×8-block cells,
  cells −621..628 on each axis, with a single floor (`y = 0`). Pieces are at most 16 blocks tall and 8×8 cells. The
  console room sits in the middle: cells (3..4, 3..5) are reserved for it, leaving 624 free columns west and east,
  624 rows north and 623 south. It joins the grid at two root links, the west face of cell (5, 4) and the east face
  of cell (2, 4). Rotation counts clockwise quarter turns (`0..3`).
- **Piece definitions:** a synced datapack registry. Entries load from `data/<namespace>/tardis/ars_piece/<name>.json`
  and are sent to clients on join.
- **Structure templates** for eight pieces plus the two console lobbies, under `data/dwm/structure/tardis/ars/`.
- **Layout rules** (`ArsLayoutLogic`): pure logic for placing and removing pieces on the grid.
- **Growing and jettisoning** pieces in the interior (`ArsService`, `ArsRoomPlacer`), with a console lobby and
  console doorway on each side, and the ops command `/tardis ars`.
- **Console control and ARS screen:** the owner opens a pannable map of the plot from the First Doctor console, grows
  pieces from a palette and jettisons them.
- **Plot migration:** interiors built on the old 64-block plots are copied to the new plots the first time a world
  loads (see [Plot Migration](#plot-migration)).

## Plot Window and Grid Coordinates
Every TARDIS is given a **plot slot** the first time one is needed (`TardisPlotSlots`): the lowest free index of a
square spiral out from slot (0, 0), so plots stay close to the world origin. The slot is saved in the TARDIS's data
(`plotSlotX`, `plotSlotZ`) straight away and never changes or goes to another TARDIS, so plots never collide. Slots
run over −2999..2999 on both axes, which keeps every window inside the world border.

Each plot has two origins (`TardisPlotAllocator`):
- The **anchor** (`plotOrigin`) is the slot × 10000, at `y = 64`. The console room (11×7×17), its doors and
  entrance, the BOTI relative coordinates and the portal stream centre are anchor-relative.
- The **window** (`windowOrigin` = anchor − (4995, 0, 4995)) is the 10000×10000 box, any height, that the TARDIS owns,
  with the console room in the middle. Every ownership check and clip uses it: the plot index, monitors and
  `/tardis claim`, flight audio, the ARS request check and the ARS chunk clamp. Neighbouring windows tile exactly.
  The BOTI stream is still capped at 3 chunks around the anchor (`BotiInteriorSampler.MAX_STREAM_RADIUS_CHUNKS`), so
  rooms further out are not shown through the doors and portal cost does not grow with the plot.

ARS cells and boxes are **grid-local**. Grid-local (0, 0, 0) is the **grid origin**, anchor − (27, 0, 27)
(`ArsGrid.gridOrigin`), not the window origin, so the console room's template origin is grid-local (27, 0, 27)
(`ArsGrid.CONSOLE_ORIGIN`), the room covers x 27..37, z 27..43, and cells west and north of it are negative. The
window covers grid-local −4968..5031 on x and z.

Before a TARDIS's console room is first placed, leftovers in its window are cleared (`TardisPlotPreparer`): the
window can overlap old content, such as legacy interiors of TARDISes whose data is gone. Only chunk columns that exist
(loaded, or saved in a region file) are loaded; every block and non-player entity in the window is removed, with no
drops, and the TARDIS is marked `plotPrepared` so it only happens once.

| Root link | Cell face | Console doorway (grid-local, 2×3) | Lobby | Lobby x (grid-local) |
| --- | --- | --- | --- | --- |
| East | west face of (5, 4) | `x = 37`, `z = 34..35`, `y = 1..3` | `dwm:tardis/ars/console_lobby_east` (2×6×6) | 38..39 |
| West | east face of (2, 4) | `x = 27`, `z = 34..35`, `y = 1..3` | `dwm:tardis/ars/console_lobby_west` (3×6×6) | 24..26 |

Both doorways sit at along-face offsets 2..3 of the root row `cz = 4`, inside its standard aperture (`z = 34..37`,
`y = 1..4`). The west doorway replaces the console room's two decorative ash doors while it is open.

## Console Control and ARS Screen
The **ARS control** sits on the console's Panel5 (Systems) bottom row, below the refueler: a small copper tree on a
map-grid plate with an amber lamp and a brass dial. Its HUD label is *Architectural Reconfiguration*. Only the
TARDIS owner can use it (visitors see *You do not own this TARDIS*); no circuit is needed. In flight it refuses with
the in-flight overlay. Otherwise it opens the ARS screen, which does not pause the game.

The screen has two halves:
- **Map** (left): an 8×8-cell window onto the grid as a floor plan, north up (cell x grows to the right/east, z
  downward/south; the screen says *North*). It opens on the **home view**, cells (0..7, 0..7), with the console room
  in the middle, and pans over the whole grid: drag with any mouse button (a press that moves less than 3 pixels is
  still a click), use the arrow keys or `W` `A` `S` `D` (one cell per press), or the mouse wheel (up/down pans north
  and south, sideways east and west). **Centre** or `C` returns to the home view. The view stops at the grid's edges,
  and the title bar shows the coordinates of the cell under the cursor. The console room's six cells, in the middle of the map, are drawn as the console room with
  the console marked, and the TARDIS's main doors (the entrance on the console room's north wall) shown as a blue
  bar labelled *Entrance*. The two root links, the west face of cell (5, 4) and the east face of cell (2, 4), are
  the console doorways: each is amber while closed and joined once a piece opens it.
  Each placed piece fills its cells in its `map_color` with an outline; its connectors are notches on the cell
  edges, and matched connectors (open links) are drawn as doorways across the shared edge.
- **Palette** (right): every registered piece, corridors first and then rooms, each by id, with its name, footprint
  and artron cost. Below it a readout shows the artron reserve (and that building is free in creative), the
  selected piece's cost and rotation, and why a placement or jettison is refused.

Interactions:
- **Grow:** click a palette piece, then hover the map. A ghost of its rotated footprint and connectors follows the
  cursor (its anchor cell is the one under the cursor), green when the layout rules allow it and red with the
  reason otherwise; the client uses the same `ArsLayoutLogic` rules as the server. `R` or **Rotate** turns it a
  quarter turn clockwise. Clicking a valid cell asks the server to grow it. Clicking the palette entry again
  clears it.
- **Jettison:** with nothing selected in the palette, click a placed piece (or right-click it at any time) to
  select it. The readout shows its name, the refund and whether it can be removed; **Jettison** asks the server to
  remove it and is disabled, with the reason as its tooltip, when the rules refuse.
- **Done** or `Esc` closes the screen. After every request the server shows the result as an overlay and re-sends
  the layout as a refresh: an ARS screen still open for that TARDIS updates in place, keeping the palette
  selection and rotation, and a refresh never reopens the screen once it has been closed. Only the console
  control opens it.

### Server checks
The screen only sends requests (`ArsGrowC2SPayload`, `ArsJettisonC2SPayload`); the server re-checks everything:
the TARDIS must exist, the sender must own it, it must not be in flight, and the sender must be standing in
`dwm:tardis` inside that TARDIS's interior plot window (*Stand inside this TARDIS to reconfigure it*). Cells, rotation
and piece id are then passed to `ArsService`, which applies the layout rules, so out-of-range values and unknown
pieces are refused rather than trusted. Building is free when the sender is in creative. The layout and artron are
sent back with `OpenArsScreen` (piece definitions come from the synced registry).

## Plot Migration
Before 10000-block plots, each TARDIS's interior sat in a 64×64 window around an anchor hashed from its UUID
(`TardisPlotAllocator.legacyPlotOrigin`, kept frozen for this). When a world loads (`SERVER_STARTED`, before any
player can be inside), `TardisPlotMigration` moves every TARDIS whose data predates the change (`plotVersion` unset)
to its new plot:

- **What is copied:** the whole legacy window, at every height: every non-air block and block state, every block
  entity's full NBT, and every non-player entity with its passengers, keeping its UUID. It lands at the same
  anchor-relative place in the new plot, so the ARS layout (`arsPieces`) keeps its meaning with no rewrite.
  Positions saved inside that NBT that point into the legacy window move with it (`LegacyPositionRewriter`): every
  `BlockPos` (paintings, item frames, leash knots, bee hive flowers, villager memories in `dwm:tardis`, the console
  controls' console, chairs, Dalek posts) and every entity position. Positions elsewhere are left alone.
- **Order:** every interior is captured to a snapshot file (`tardis_data/plot_migration/<id>.nbt`) before any legacy
  window is cleared, because a new window can cover another TARDIS's legacy window. Then every legacy window is
  cleared, then each new window is cleared of leftovers and its snapshot pasted. Blocks are set without neighbour
  updates, drops or side effects, so nothing pops off or fires.
- **Crash-safe:** each phase (`SNAPSHOTTED`, `CLEARED`, `PASTED`, `DONE`) is recorded in
  `plot_migration/state.json` only after the interior level has been saved to disk. Every step is repeatable, so a
  crash resumes from the last recorded phase without losing or duplicating anything. Snapshot files are deleted when
  the migration is done.
- **References outside the copy:** an exterior's recorded interior entrance is recomputed from the new plot the next
  time it is used (`TardisInteriorService.refreshEntrance`). Players whose save files existed when the migration ran
  are checked once on their next join: anyone standing in a migrated legacy window, or with a respawn point there,
  is moved by the same offset. Players who first join later are never moved.
- **Back up first:** the migration is one-way. Once it reaches `DONE` the legacy windows are empty and there is no
  undo or dry-run mode. Players and server owners should back up their world before loading it with this version
  (the release notes for the first release with 10000-block plots must say so). Each moved interior is logged at
  INFO (`TARDIS <id>: interior moved from <legacy anchor> to <new anchor>`), and while a migration is unfinished its
  snapshots stay in `tardis_data/plot_migration/`.
- **Not copied, by design:** entities that are never saved (leash knots, which are rebuilt from the leashed mob,
  and the console's control hitboxes, which the console re-spawns) are left out, exactly as when a chunk unloads.
- **Not covered:** pending block and fluid ticks are not carried over, so copied redstone and flowing fluids settle
  on their next update. Absolute positions in NBT that are not `BlockPos` int triples are copied unchanged.

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
- a player is inside the piece, or inside a lobby whose root link the jettison would close (the piece is the one
  on that root link and nothing else keeps it open). A player in the other side's lobby does not block it.

Otherwise everything in the piece is destroyed: blocks, container contents and non-player entities, with no
drops. Each neighbour that shared a doorway gets its own wall plug back (its template is re-placed, clipped to that
4×4 layer). Half the cost, rounded down, is refunded.

### Console lobbies and doorways
Each root link has its own lobby and console doorway, handled on its own. Growing the piece that opens a root link
places that side's lobby and carves that side's doorway through the console room's wall (east wall `x = 37` or
west wall `x = 27`, `z = 34..35`, `y = 1..3`, grid-local). Jettisoning the piece that closes a root link clears that
side's lobby and puts the console room's wall back from its template (on the west side this restores the ash
doors), even if pieces cut off from the console room remain; the other side stays open. Re-placing the console room
(first entry, deferred preload or `/tardis rebuild`) re-opens the doorway of each side whose root link is open.

`/tardis rebuild` only rebuilds the console room. It does not re-place ARS rooms or the lobbies.

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
| `cells` | Unrotated footprint `[width, depth]` in cells; each `1..8` (`ArsGrid.MAX_PIECE_CELLS`). |
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

### Console lobbies
`dwm:tardis/ars/console_lobby_east` (2×6×6) and `dwm:tardis/ars/console_lobby_west` (3×6×6) are fixed lobbies, not
registry pieces, placed unrotated at grid-local (38, 0, 33) and (24, 0, 33). Each is a short white passage with the
corridors' 4×4 cross-section (`z = 34..37`, `y = 1..4`) between roundel walls at `z = 33` and `z = 38`, floor
`y = 0` and ceiling `y = 5`, open at both x ends: the console room's wall closes one end and the root cell's piece
the other. The closed-shell rule does not apply to them; `ArsPieceResourceTest` checks their size, open ends and
solid walls.

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
- The palette shows eight rows at a time; datapacks with more pieces scroll it with the mouse wheel.
- Room blocks are not protected: players can still break walls by hand.
- Removing a piece whose datapack definition no longer exists is not possible; its blocks stay until a new piece is
  grown over its cells.
- Pieces share a single floor; there are no multi-level layouts.

## Future Opportunities
- More rooms as plain data and templates.
