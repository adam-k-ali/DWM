# Block entities and custom renderers

Use only when JSON block models cannot draw the mesh: TESR/BER, `builtin/entity`
item models, or `BaseEntityBlock`. Keep this checklist short and copy a live
sibling. TARDIS travel, interiors, portals, and chameleon belong in
[tardis/AGENTS.md](../../../../dwm/src/main/java/com/adamkali/dwm/tardis/AGENTS.md)
— do not re-derive them here.

## When you need this path

| Need | Sibling |
|------|---------|
| Invisible block + static entity mesh (globe, scanners) | `TardisDecorEntityBlock` + shared `TARDIS_DECOR_BLOCK_ENTITY` |
| Full exterior with persistence / portals | `TardisBlock` + `TardisBlockEntity` |
| Console | `FirstDoctorConsoleBlock` + `FirstDoctorConsoleBlockEntity` |
| Interior door | `TardisInteriorDoorBlock` + entity |

JSON-shaped chairs/column/vent stay on [hand-modeled.md](hand-modeled.md)
(`TardisDecorBlock` has **no** block entity).

## Common layers (all BE blocks)

1. **Block** in `dwm/src/main/java` — usually `BaseEntityBlock`. Register in
   [`DWMBlocks`](../../../../dwm/src/main/java/com/adamkali/dwm/block/DWMBlocks.java)
   with a dedicated settings factory (`TARDIS_BLOCK`, `tardisCeilingVent`-style
   fresh properties). `register(...)` still creates the BlockItem.
2. **Block entity** + type in
   [`DWMBlockEntities`](../../../../dwm/src/main/java/com/adamkali/dwm/block/entities/DWMBlockEntities.java).
   `DWMMain` already calls `DWMBlockEntities.initialize()`. Prefer adding the
   block to an **existing** type (`TARDIS_DECOR_BLOCK_ENTITY` already lists
   globe + both scanners) over a new type.
3. **Client BER** in `dwm/src/client/java/.../render/`, registered from
   [`DWMBlockEntityRendererFactories`](../../../../dwm/src/client/java/com/adamkali/dwm/DWMBlockEntityRendererFactories.java)
   (`BlockEntityRendererRegistry.register`). Never import that class from
   common/server code.
4. **Hand client assets**:
   - Blockstate often uses a dummy/particle model (see
     [`tardis_block.json`](../../../../dwm/src/client/resources/assets/dwm/models/block/tardis_block.json)
     / globe multipart).
   - Item: `minecraft:special` pointing at a `dwm:<renderer_id>` type
     ([`tardis_globe` item](../../../../dwm/src/client/resources/assets/dwm/items/tardis_globe.json),
     [`tardis_block` item](../../../../dwm/src/client/resources/assets/dwm/items/tardis_block.json)).
5. **Special item renderer** — implement `NoDataSpecialModelRenderer`, then
   `SpecialModelRenderers.ID_MAPPER.put(...)` in
   [`DWMClient`](../../../../dwm/src/client/java/com/adamkali/dwm/DWMClient.java)
   **before** resource reload. Id must match the item JSON `"type": "dwm:..."`.
6. Datagen still owns loot, lang (`addBlockAndItem`), recipes, and tab
   (`FUNCTIONAL_BLOCKS` for tardis/console; `BUILDING_BLOCKS` for decor
   entities). Skip `registerCubeAll`.

## Decor entity extra (globe / scanners)

- Block: [`TardisDecorEntityBlock`](../../../../dwm/src/main/java/com/adamkali/dwm/block/TardisDecorEntityBlock.java)
  + shape on [`TardisDecorShapes`](../../../../dwm/src/main/java/com/adamkali/dwm/block/TardisDecorShapes.java).
- BE: [`TardisDecorBlockEntity`](../../../../dwm/src/main/java/com/adamkali/dwm/block/entities/TardisDecorBlockEntity.java)
  — add the new block to the existing `register("tardis_decor", ...)` varargs.
- BER: [`TardisDecorBlockEntityRenderer`](../../../../dwm/src/client/java/com/adamkali/dwm/render/TardisDecorBlockEntityRenderer.java)
  (already registered for that type).
- Tests:
  [`TardisDecorBlockTest`](../../../../dwm/src/test/java/com/adamkali/dwm/block/TardisDecorBlockTest.java),
  [`TardisDecorModelTest`](../../../../dwm/src/test/java/com/adamkali/dwm/model/tileentity/TardisDecorModelTest.java).

## TARDIS exterior extra

Read [tardis/AGENTS.md](../../../../dwm/src/main/java/com/adamkali/dwm/tardis/AGENTS.md)
first. Product docs:
[`docs/feature-tardis-block.md`](../../../../dwm/docs/feature-tardis-block.md).
Logic lives in `tardis.logic` / `tardis.data`; the block class stays thin.
GameTests: `TardisDoorGameTests`, `TardisInteriorGameTests`,
`TardisLandingGameTests`. Screenplay only for real-client flows
(`placeAndOpenTardis`).

## Networking reminder

Gameplay state is server-authoritative. New C2S payloads: register type/codec
before handlers, validate on the server. See [AGENTS.md](../../../../AGENTS.md)
Networking & Side Safety.
