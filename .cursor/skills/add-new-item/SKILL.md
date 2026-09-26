---
name: add-new-item
description: >-
  Add a new Minecraft item to DWM: registry, creative tab, datagen (models,
  recipes, tags, lang), client textures, and tests. Use when asked to add,
  register, or wire a new item, ingot, gem, powder, tool, armor piece,
  spawn egg, circuit, or handheld gadget — not a BlockItem.
---

# Add a New Item (DWM)

## Keywords

add item, register item, new ingot, gem, powder, tool, armor, spawn egg,
circuit, creative tab, datagen, item model, handheld

## Overview

Wire a **new `dwm:` item** through this monorepo’s live registration and
datagen path. This skill covers **Java + datagen + resource layout**, not
pixel art.

**Output:** a registered item (not a BlockItem), generated data under
`dwm/src/main/generated/` when datagen owns the model, and a texture at
`dwm/src/client/resources/assets/dwm/textures/item/<id>.png` (unless a special
renderer uses an entity atlas).

**Read references on demand** — more than one can apply (e.g. sonic = custom
+ hand-modeled):

| Reference | When |
|-----------|------|
| [flat-datagen.md](references/flat-datagen.md) | Default: flat ingredient (ingot, gem, powder, circuit sprite) |
| [equipment.md](references/equipment.md) | Tool set, armor, spawn egg |
| [hand-modeled.md](references/hand-modeled.md) | Skip `generateFlatItem`; hand JSON (sonic, TARDIS key, field guide) |
| [custom.md](references/custom.md) | `Item` subclass, data components, special renderer |

**Do not duplicate live repo knowledge.** Read the cited classes at
implementation time. Textures: [minecraft-sprite-style](../minecraft-sprite-style/SKILL.md).
TARDIS gameplay: [tardis/AGENTS.md](../../../dwm/src/main/java/com/adamkali/dwm/tardis/AGENTS.md).
Repo rules: [AGENTS.md](../../../AGENTS.md).
Placeable blocks: [add-new-block](../add-new-block/SKILL.md).

Gold-standard flat item: `STEEL_INGOT` in
[`DWMItems.java`](../../../dwm/src/main/java/com/adamkali/dwm/item/DWMItems.java).

## Hard constraints

| Allowed | Forbidden |
|---------|-----------|
| Common register/tags/recipes/lang in `dwm/src/main/java` | Client renderer / HUD / screen classes on the common path |
| Models via `DWMModelProvider.generateFlatItem` **or** hand JSON under `dwm/src/client/resources` | Hand-editing generated models/recipes/tags/`en_us.json` |
| Fresh `Item.Properties()` per `register(...)` | Reusing a `Properties` that already had `setId` for a *new* independent item |
| `DWMItems.register(...)` for standalone items | Adding a normal block / BlockItem to `DWMItems` |
| `content.accept(...)` in `DWMItems.initialize()` | Relying on the static field alone for creative-tab visibility |
| Prune-allowlist substring for datagen-owned `items/<id>.json` | Forgetting `pruneDatagenItemModels` so datagen output is deleted |
| `./dwm/gradlew` | Root `./gradlew` |

`DWMBlocks.register()` already creates the BlockItem. Wood boats/signs come
from `WoodFamilyRegistrar` — do not copy those into a parallel register path.

Static `register(...)` is enough for most items. Spawn eggs (and wood
sign/boat fields) are assigned in `initialize()` because they depend on
entity types / wood families. [`DWMMain`](../../../dwm/src/main/java/com/adamkali/dwm/DWMMain.java)
already calls `DWMItems.initialize()` after `DWMEntityTypes`.

## Classify

1. **Flat ingredient** (`Item::new`, `FLAT_ITEM`) — [flat-datagen.md](references/flat-datagen.md)
2. **Tool / armor / spawn egg** — [equipment.md](references/equipment.md)
3. **Hand JSON** (display transforms, GUI select, skip datagen models) — [hand-modeled.md](references/hand-modeled.md)
4. **Gameplay subclass** (use/tick, data components, special renderer) — [custom.md](references/custom.md)

If the request is TARDIS exterior, interior dimension, console, or portals,
stop and follow [tardis/AGENTS.md](../../../dwm/src/main/java/com/adamkali/dwm/tardis/AGENTS.md)
instead of expanding this skill. If the request is a placeable block, follow
[add-new-block](../add-new-block/SKILL.md).

## Workflow

1. **Classify** and open every matching reference. Copy the closest live sibling
   (steel ingot, zeiton powder, azbantium tools, EVA suit, TARDIS key, radiation
   meter) — do not invent a parallel pipeline.
2. **Name it** — `snake_case` registry id = texture filename = Java
   `SCREAMING_SNAKE` field. Full id is `dwm:<id>`.
3. **Register** in
   [`DWMItems`](../../../dwm/src/main/java/com/adamkali/dwm/item/DWMItems.java)
   with a **fresh** `Item.Properties()` so `setId` cannot collide. Default stack
   size is 64; uniques use `.stacksTo(1)`; circuits use `.stacksTo(16)`.
4. **Creative tab** — `content.accept(...)` under the right
   [`DWMCreativeTabs`](../../../dwm/src/main/java/com/adamkali/dwm/item/DWMCreativeTabs.java)
   callback (`INGREDIENTS`, `TOOLS_AND_UTILITIES`, `COMBAT`, `SPAWN_EGGS`).
   Exception: `FIELD_GUIDE` is granted, not tabbed — copy that only if the
   task is a grant-only item.
5. **Texture** — 16×16 PNG via
   [minecraft-sprite-style](../minecraft-sprite-style/SKILL.md).
6. **Datagen** — providers are registered from
   [`DWMClientDataGenerator`](../../../dwm/src/client/java/com/adamkali/dwm/DWMClientDataGenerator.java).
   Lang source of truth is
   [`DWMLanguageProvider.addItem`](../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMLanguageProvider.java).
   Datagen-owned item defs must survive
   [`pruneDatagenItemModels`](../../../dwm/build.gradle) (see
   [flat-datagen.md](references/flat-datagen.md)).
7. **Run** `./dwm/gradlew runDatagen`. Commit generated files. Delete
   `dwm/src/main/generated/.cache/` before commit.
8. **Tests + docs** — extend a family/registration test; extract `*Logic` for
   JUnit; in-world use goes in
   `dwm/src/main/java/com/adamkali/dwm/gametest/`. Update
   `dwm/docs/feature-*.md` when player-visible.
9. **Verify** `./dwm/gradlew test` (`build` before handoff).
   `ResourceValidationTests.modelDefinedTexturesExist` fails if the PNG is missing.

## Out of scope

- Drawing sprites (use [minecraft-sprite-style](../minecraft-sprite-style/SKILL.md))
- Placeable blocks / BlockItems (use [add-new-block](../add-new-block/SKILL.md))
- Wood boats, signs, hanging signs (wood registrar — [families.md](../add-new-block/references/families.md))
- TARDIS travel, interiors, portals, chameleon (tardis `AGENTS.md`)
- Field Guide catalog pages (see [`docs/feature-field-guide.md`](../../../dwm/docs/feature-field-guide.md))
- Blockbench mesh authoring (this skill only says where JSON lands)
