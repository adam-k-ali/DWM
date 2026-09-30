# Cube / ore / panel (datagen)

Default path for a full-cube block whose model is `minecraft:block/cube_all`.
Gold example: `dwm:silver_dalekanium_panel`. Sibling cubes: citadel wall/panel/tile,
`azbantium_block`, Gallifrey stone.

Do **not** hand-write blockstate/model/item JSON for this path — `DWMModelProvider`
emits them under `dwm/src/main/generated/`.

## 1. Settings

Add a **fresh** factory in
[`DWMBlockSettings.java`](../../../../dwm/src/main/java/com/adamkali/dwm/block/DWMBlockSettings.java)
when this is a new material. `setId` mutates `Properties`; a shared
`static final Properties` reused across unrelated blocks will collide.

Copy an existing factory when the material matches:

- Metal architecture — `dalekaniumMetal(MapColor)` (hardness 4, `requiresCorrectToolForDrops`)
- Azbantium — `azbantium()` (diamond tool, very hard)
- Stone-like — `GALLIFREY_STONE` / `CITADEL` only when **extending that family**
- Ore in stone — `dalekaniumOre()` / `gallifreyVanillaOre()` / `azbantium()`

## 2. Register

In [`DWMBlocks.java`](../../../../dwm/src/main/java/com/adamkali/dwm/block/DWMBlocks.java):

```java
public static final Block EXAMPLE_PANEL = register(
        Block::new,
        DWMBlockSettings.dalekaniumMetal(MapColor.COLOR_LIGHT_GRAY),
        "example_panel"
);
```

`register(...)` also registers `dwm:example_panel` as a `BlockItem`. Do not add
an entry in `DWMItems`.

If the block belongs to a set, append the field to the matching `List<Block>`
(`DALEKANIUM_ARCHITECTURE`, `CITADEL_BUILDING_BLOCKS`, …). Creative tabs iterate
those lists in `initialize()`.

## 3. Creative tab

Static init is not enough. In `DWMBlocks.initialize()`, accept the block (or
its family list) on the right vanilla tab via
[`DWMCreativeTabs`](../../../../dwm/src/main/java/com/adamkali/dwm/item/DWMCreativeTabs.java):

| Tab | Typical blocks |
|-----|----------------|
| `BUILDING_BLOCKS` | Panels, walls, cubes, architecture |
| `NATURAL_BLOCKS` | Terrain, ores, plants, logs |
| `FUNCTIONAL_BLOCKS` | `TARDIS_BLOCK`, console |
| `REDSTONE_BLOCKS` | Door button, wood buttons/plates/doors |

Dalekanium architecture is accepted from `DALEKANIUM_ARCHITECTURE` under
`BUILDING_BLOCKS`. Ores go under `NATURAL_BLOCKS`.

## 4. Texture

Write `dwm/src/client/resources/assets/dwm/textures/block/<id>.png` with
[minecraft-sprite-style](../../minecraft-sprite-style/SKILL.md). Filename
**must** equal the registry path (`silver_dalekanium_panel.png`).

`createTrivialCube` looks up `dwm:block/<id>`. Missing PNG →
`ResourceValidationTests.modelDefinedTexturesExist` fails.

Multi-face cubes (grass, logs, sandstone) are **not** `registerCubeAll` — copy
the sibling helper in `DWMModelProvider` (`registerGallifreyGrass`,
`registerSandstone`, wood client datagen).

## 5. Datagen (required)

Providers are attached in
[`DWMClientDataGenerator`](../../../../dwm/src/client/java/com/adamkali/dwm/DWMClientDataGenerator.java).
Edit the Java providers; never patch generated JSON.

### Models

[`DWMModelProvider.registerCubeAll`](../../../../dwm/src/client/java/com/adamkali/dwm/datagen/DWMModelProvider.java)
— `createTrivialCube` + `registerSimpleItemModel`. Dalekanium loops
`DWMBlocks.DALEKANIUM_ARCHITECTURE`; adding to that list is enough for models.

### Loot

[`DWMLootTableProvider`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMLootTableProvider.java):

- Building cube — `dropSelf(block)` (or loop the family list)
- Ore that drops an item — `add(ore, createOreDrop(ore, item))`
- Silk-touch glass — `createSilkTouchOnlyTable`

### Recipes

[`DWMRecipeProvider`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMRecipeProvider.java)
when the family crafts. Dalekanium panels: 4 ingots → 8 panels, then
`stonecutterResultFromBase` between panel and riveted wall. Ores: `oreSmelting`
/ `oreBlasting`. Skip recipes for purely decorative cubes that have none today
(citadel still has stonecutting — copy that family if this is a citadel sibling).

### Tags

[`DWMBlockTagProvider`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMBlockTagProvider.java):

- Mod tag in [`DWMBlockTags`](../../../../dwm/src/main/java/com/adamkali/dwm/block/DWMBlockTags.java)
  when the block is part of a queryable set; mirror in
  [`DWMItemTags`](../../../../dwm/src/main/java/com/adamkali/dwm/item/DWMItemTags.java)
  and `copy(...)` from
  [`DWMItemTagProvider`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMItemTagProvider.java)
- Mining: `#minecraft:mineable/pickaxe` or `mineable/shovel`
- Harvest: `needs_stone_tool` / `needs_iron_tool` / `needs_diamond_tool` to
  match `.requiresCorrectToolForDrops()`
- Shape tags (`stairs`, `slabs`, `walls`, `impermeable`) only when relevant

Roundels / TARDIS walls are **not** currently in mineable tags — do not add
them unless the task asks.

### Lang

[`DWMLanguageProvider.addBlockAndItem`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMLanguageProvider.java)
with Title Case English (`"Silver Dalekanium Panel"`). Do not edit
`dwm/src/main/generated/assets/dwm/lang/en_us.json` by hand.

## 6. Worldgen (ores only)

Decorative cubes skip this. Ores need:

- Configured feature — [`DWMConfiguredFeatureBootstrap`](../../../../dwm/src/main/java/com/adamkali/dwm/world/DWMConfiguredFeatureBootstrap.java)
- Placed feature — [`DWMPlacedFeatureBootstrap`](../../../../dwm/src/main/java/com/adamkali/dwm/world/DWMPlacedFeatureBootstrap.java)
- Biome injection — dimension bootstrap and/or `BiomeModifications` in
  [`DWMMain`](../../../../dwm/src/main/java/com/adamkali/dwm/DWMMain.java)
- Replaceables tag (`#dwm:gallifrey_ore_replaceables` or
  `#minecraft:stone_ore_replaceables`)

Copy the closest ore (`DALEKANIUM_ORE`, `AZBANTIUM_ORE`, `ZEITON_ORE`).

## 7. Tests and docs

- Unit: extend
  [`DalekaniumArchitectureFamilyTest`](../../../../dwm/src/test/java/com/adamkali/dwm/block/DalekaniumArchitectureFamilyTest.java)
  (id, list size, hardness, `requiresCorrectToolForDrops`) or the matching
  `*FamilyTest`.
- GameTest: mining/smelting/stonecutting in-world — see
  [`DalekaniumGameTests`](../../../../dwm/src/main/java/com/adamkali/dwm/gametest/DalekaniumGameTests.java),
  [`AzbantiumGameTests`](../../../../dwm/src/main/java/com/adamkali/dwm/gametest/AzbantiumGameTests.java).
  Run `./dwm/gradlew runGametest` when GameTest code changes.
- Docs: [`docs/feature-dalekanium.md`](../../../../dwm/docs/feature-dalekanium.md),
  [`docs/feature-gallifrey-building.md`](../../../../dwm/docs/feature-gallifrey-building.md),
  [`docs/feature-azbantium.md`](../../../../dwm/docs/feature-azbantium.md),
  [`docs/feature-zeiton.md`](../../../../dwm/docs/feature-zeiton.md).

## 8. Generate

```bash
./dwm/gradlew runDatagen
rm -rf dwm/src/main/generated/.cache
./dwm/gradlew test
```

Commit intentional generated outputs (models, blockstates, item defs, loot,
recipes, tags, lang). Leave `.cache/` untracked.
