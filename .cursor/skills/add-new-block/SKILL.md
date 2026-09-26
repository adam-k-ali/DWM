---
name: add-new-block
description: >-
  Add a new Minecraft block to DWM: registry, BlockItem, creative tab, datagen
  (models, loot, recipes, tags, lang), client textures, and tests. Use when
  asked to add, register, or wire a new block, cube, ore, panel, roundel,
  stairs/slab/wall set, wood family, or decorative interior prop.
---

# Add a New Block (DWM)

## Keywords

add block, register block, new cube, panel, ore, roundel, stairs, slab, wall,
wood family, block item, datagen, blockstate, loot table, creative tab

## Overview

Wire a **new `dwm:` block** through this monorepo’s live registration and
datagen path. This skill covers **Java + datagen + resource layout**, not
pixel art.

**Output:** a placeable block (usually with a same-id BlockItem), generated
data under `dwm/src/main/generated/`, and a texture at
`dwm/src/client/resources/assets/dwm/textures/block/<id>.png`.

**Read references on demand:**

| Reference | When |
|-----------|------|
| [cube-datagen.md](references/cube-datagen.md) | Default: cube / ore / panel (copy dalekanium or citadel) |
| [hand-modeled.md](references/hand-modeled.md) | Custom mesh JSON (roundels, TARDIS chairs/column/vent) |
| [families.md](references/families.md) | Stairs/slab/wall, colour sets, wood registrar, plants |
| [complex.md](references/complex.md) | Block entity, BER, special item renderer |

**Do not duplicate live repo knowledge.** Read the cited classes at
implementation time. Textures: [minecraft-sprite-style](../minecraft-sprite-style/SKILL.md).
TARDIS gameplay: [tardis/AGENTS.md](../../../dwm/src/main/java/com/adamkali/dwm/tardis/AGENTS.md).
Repo rules: [AGENTS.md](../../../AGENTS.md).

Gold-standard cube: `SILVER_DALEKANIUM_PANEL` in
[`DWMBlocks.java`](../../../dwm/src/main/java/com/adamkali/dwm/block/DWMBlocks.java).

## Hard constraints

| Allowed | Forbidden |
|---------|-----------|
| Common register/settings/tags/loot/recipes/lang in `dwm/src/main/java` | Client renderer/BER classes on the common path |
| Models via `DWMModelProvider` **or** hand JSON under `dwm/src/client/resources` | Hand-editing generated models/loot/recipes/tags/`en_us.json` |
| Fresh `DWMBlockSettings` factory (or `ofLegacyCopy` for stairs/slab/wall) | Reusing a `static final Properties` that already had `setId` for a *new* independent block |
| `register(...)` for a same-id BlockItem | Adding a normal block to [`DWMItems`](../../../dwm/src/main/java/com/adamkali/dwm/item/DWMItems.java) |
| `content.accept(...)` in `DWMBlocks.initialize()` | Relying on the static field alone for creative-tab visibility |
| Wood sets via `WoodFamilyRegistrar` | Copy-pasting ~20 wood blocks by hand |
| `./dwm/gradlew` | Root `./gradlew` |

`DWMBlocks.register()` already creates the BlockItem. Use
`registerWithoutItem` only for flower pots and wood signs (items come from the
wood registrar / `DWMItems`).

## Classify

1. **Cube / ore / panel** with `cube_all` — [cube-datagen.md](references/cube-datagen.md)
2. **Custom mesh** (Blockbench JSON, facing, non-full collision) — [hand-modeled.md](references/hand-modeled.md)
3. **Set** (stairs/slab/wall, 17-colour roundels, full wood, cross plant) — [families.md](references/families.md)
4. **Block entity / BER / `builtin/entity` item** — [complex.md](references/complex.md)

If the request is TARDIS exterior, interior dimension, console, or portals,
stop and follow [tardis/AGENTS.md](../../../dwm/src/main/java/com/adamkali/dwm/tardis/AGENTS.md)
instead of expanding this skill.

## Workflow

1. **Classify** and open the matching reference. Copy the closest live sibling
   (dalekanium panel, citadel cube, Gallifrey brick family, roundel colour,
   Ash wood, TARDIS chair) — do not invent a parallel pipeline.
2. **Name it** — `snake_case` registry id = texture filename = Java
   `SCREAMING_SNAKE` field. Full id is `dwm:<id>`.
3. **Settings + register** in
   [`DWMBlockSettings`](../../../dwm/src/main/java/com/adamkali/dwm/block/DWMBlockSettings.java)
   and [`DWMBlocks`](../../../dwm/src/main/java/com/adamkali/dwm/block/DWMBlocks.java).
   Prefer a factory that returns **fresh** `Properties` (`dalekaniumMetal`,
   `azbantium`, `tardisRoundel`) so `setId` cannot collide.
4. **Family list + creative tab** — append a `List<Block>` when the block
   belongs to a set; `content.accept(...)` under the right
   [`DWMCreativeTabs`](../../../dwm/src/main/java/com/adamkali/dwm/item/DWMCreativeTabs.java)
   callback (`BUILDING_BLOCKS`, `NATURAL_BLOCKS`, `FUNCTIONAL_BLOCKS`,
   `REDSTONE_BLOCKS`).
5. **Texture** — 16×16 PNG via
   [minecraft-sprite-style](../minecraft-sprite-style/SKILL.md).
6. **Datagen** — providers are registered from
   [`DWMClientDataGenerator`](../../../dwm/src/client/java/com/adamkali/dwm/DWMClientDataGenerator.java).
   Lang source of truth is
   [`DWMLanguageProvider.addBlockAndItem`](../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMLanguageProvider.java)
   (older roundels use item-only `addBlockItem`; **new** blocks should set both).
7. **Run** `./dwm/gradlew runDatagen`. Commit generated files. Delete
   `dwm/src/main/generated/.cache/` before commit.
8. **Tests + docs** — extend a family/hardness test; mining, recipes, or
   worldgen that need a real world go in
   `dwm/src/main/java/com/adamkali/dwm/gametest/`. Update
   `dwm/docs/feature-*.md` when player-visible.
9. **Verify** `./dwm/gradlew test` (`build` before handoff).
   `ResourceValidationTests.modelDefinedTexturesExist` fails if the PNG is missing.

## Out of scope

- Drawing sprites (use [minecraft-sprite-style](../minecraft-sprite-style/SKILL.md))
- Non-block items (use [add-new-item](../add-new-item/SKILL.md); they remain in `DWMItems`)
- TARDIS travel, interiors, portals, chameleon (tardis `AGENTS.md`)
- Blockbench mesh authoring (this skill only says where JSON lands)
