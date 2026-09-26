# Hand-modeled decorative blocks

Use when the block is **not** a trivial cube: custom `elements` JSON, facing,
or a non-full VoxelShape. Two live patterns:

| Pattern | Example | Block class |
|---------|---------|-------------|
| Shared parent + texture remap | `white_roundel_a` → parent `dwm:block/roundel_a` | `Block::new` + `tardisRoundel()` |
| Unique Blockbench mesh | `tardis_chair_small`, `decorational_column`, `tardis_ceiling_vent` | `TardisDecorBlock` / `TardisChairBlock` |

Entity-mesh decor (globe, scanners) and the TARDIS exterior are **not** this
path — see [complex.md](complex.md).

Skip `DWMModelProvider.registerCubeAll`. Still run loot, lang, tabs, and
recipes through datagen.

## Roundel / wall / powder colour variant

1. Register with `DWMBlockSettings.tardisRoundel()` (A / big A) or
   `tardisRoundelNoOcclusion()` (B / big B). Powder uses
   `CHRONOPLASM_POWDER_SETTINGS`.
2. Copy the closest colour’s **three** client JSON files, retargeting `<id>`:
   - `dwm/src/client/resources/assets/dwm/blockstates/<id>.json`
   - `dwm/src/client/resources/assets/dwm/models/block/<id>.json`
   - `dwm/src/client/resources/assets/dwm/items/<id>.json`
3. Keep the shared parent (`roundel_a`, `roundel_b`, `big_roundel_a`, …). Only
   remap `textures.outer` / `inner` / `particle` to existing TARDIS wall
   textures (see
   [`white_roundel_a.json`](../../../../dwm/src/client/resources/assets/dwm/models/block/white_roundel_a.json)).
4. New wall/powder colours also need a 16×16 PNG via
   [minecraft-sprite-style](../../minecraft-sprite-style/SKILL.md).
5. `dropSelf` in
   [`DWMLootTableProvider`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMLootTableProvider.java)
   (roundels are listed individually today — add a line).
6. Lang: prefer `addBlockAndItem`. Older roundels use item-only `addBlockItem`;
   do not spread that pattern to new blocks.
7. `content.accept(...)` in the `BUILDING_BLOCKS` callback next to the sibling
   colour.

Do not invent a new roundel parent mesh unless the task is a new geometry
(A/B/big). Do not generate cube models for these ids.

## Unique interior prop (JSON mesh, no BER)

Gold examples: `TARDIS_CHAIR_SMALL`, `DECORATIONAL_COLUMN`, `TARDIS_CEILING_VENT`.

1. Collision: add a north-facing `VoxelShape` on
   [`TardisDecorShapes`](../../../../dwm/src/main/java/com/adamkali/dwm/block/TardisDecorShapes.java)
   if the existing shapes do not fit. Placement facing is
   `TardisDecorShapes.facingForPlacement` (must match look direction, not
   opposite — covered by
   [`TardisDecorBlockTest`](../../../../dwm/src/test/java/com/adamkali/dwm/block/TardisDecorBlockTest.java)).
2. Register with `TardisDecorBlock` (or `TardisChairBlock` for sit-height) and
   `DWMBlockSettings.TARDIS_DECOR_SETTINGS`. Ceiling vent uses
   `tardisCeilingVent()` (fresh properties + light).
3. Hand assets under `dwm/src/client/resources/assets/dwm/`:
   - `blockstates/<id>.json` — `facing=north/south/east/west` with `y` rotations
     (copy [`tardis_chair_small` blockstate](../../../../dwm/src/client/resources/assets/dwm/blockstates/tardis_chair_small.json))
   - `models/block/<id>.json` — Blockbench `elements`
   - `items/<id>.json` — `{ "model": { "type": "minecraft:model", "model": "dwm:block/<id>" } }`
   - `textures/block/<id>.png`
4. Datagen: `dropSelf`, `addBlockAndItem`, recipes if the family crafts from
   TARDIS wall (chairs already do). No `registerCubeAll`.
5. Creative tab: `BUILDING_BLOCKS` with the other interior props.

`TardisDecorBlock` stays in the **common** source set. Do not put renderer
classes there.

## What stays generated vs hand-authored

| Asset | Hand (`src/client/resources`) | Datagen (`src/main/generated`) |
|-------|-------------------------------|--------------------------------|
| Custom block/item/blockstate JSON | yes | no |
| Cube models | no | yes |
| Loot, recipes, tags, lang | no | yes |

After adding hand JSON + PNG, still run `./dwm/gradlew runDatagen` so loot/lang
regenerate, then `./dwm/gradlew test`.
