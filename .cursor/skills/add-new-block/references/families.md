# Block families

Use when the request is a **set**, not a single cube: stairs/slab/wall,
colour-keyed interiors, a full wood type, or Gallifrey plants. Classify the
set, then reuse the matching registrar/datagen loop. Do not copy twenty
individual `register(...)` calls for wood.

## Stairs / slab / wall on a cube

Gold example: Gallifrey cobble / stone brick in
[`DWMBlocks`](../../../../dwm/src/main/java/com/adamkali/dwm/block/DWMBlocks.java).

1. Register the full cube first (`Block::new`).
2. Add variants with the private helpers (they `ofLegacyCopy` the base):

```java
public static final Block EXAMPLE_STAIRS = registerStairs(EXAMPLE, "example_stairs");
public static final Block EXAMPLE_SLAB = registerSlab(EXAMPLE, "example_slab");
public static final Block EXAMPLE_WALL = registerWall(EXAMPLE, "example_wall");
```

Orange sandstone uses the same `StairBlock` / `SlabBlock` / `WallBlock`
factories inline when it needs extra settings — copy that sibling if
`registerStairs` is too tight.

3. Models:
   [`DWMModelProvider.registerCubeFamily`](../../../../dwm/src/client/java/com/adamkali/dwm/datagen/DWMModelProvider.java)
   (`full` + stairs + slab + wall). Slab-only: `registerCubeSlab`.
4. Loot: `dropSelf` on solids; `createSlabItemTable` on slabs (see
   `GALLIFREY_STONE_SLABS` / petrified slab in
   [`DWMLootTableProvider`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMLootTableProvider.java)).
5. Tags: `#minecraft:stairs` / `slabs` / `walls` plus the family’s mineable
   tag. Item copies often follow from `DWMItemTagProvider.copy(...)`.
6. Recipes: copy the Gallifrey / orange sandstone stonecut + shaped stair/slab
   recipes in
   [`DWMRecipeProvider`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMRecipeProvider.java).
7. Put every member on the family `List<Block>` so creative tab, tags, and
   tests stay in lockstep. Extend
   [`GallifreyStoneFamilyTest`](../../../../dwm/src/test/java/com/adamkali/dwm/block/GallifreyStoneFamilyTest.java)
   (or the matching `*FamilyTest`).

Texture: usually one `cube_all` PNG on the full block; stairs/slab/wall reuse
it via datagen. New look →
[minecraft-sprite-style](../../minecraft-sprite-style/SKILL.md).

## Colour sets (roundels, TARDIS walls, chronoplasm)

Seventeen colours, four roundel geometries (A, B, big A, big B), plus walls
and powder. Adding a **new colour** means registering every geometry the
existing colours have, then following [hand-modeled.md](hand-modeled.md) per
id. Adding a **new geometry** means a new shared parent JSON plus one remap
file per colour.

Keep colour in the id prefix: `<color>_<family>_<variant>`
(`teal_big_roundel_a`). Reuse `tardisRoundel()` / `tardisRoundelNoOcclusion()`
factories so each block gets fresh `Properties`.

## Wood families

Do **not** hand-register planks, logs, signs, boats, … Copy Ash / Dark Ash /
Cardinal.

1. Tags: log block tag in
   [`DWMBlockTags`](../../../../dwm/src/main/java/com/adamkali/dwm/block/DWMBlockTags.java)
   and matching item tag in
   [`DWMItemTags`](../../../../dwm/src/main/java/com/adamkali/dwm/item/DWMItemTags.java).
2. [`DWMWoodTypes`](../../../../dwm/src/main/java/com/adamkali/dwm/block/DWMWoodTypes.java)
   + sapling in
   [`DWMSaplingGenerators`](../../../../dwm/src/main/java/com/adamkali/dwm/block/DWMSaplingGenerators.java).
3. `WoodFamilyRegistrar.registerBlocks(new WoodFamilyDefinition(...))` in
   `DWMBlocks`. Features:
   [`WoodFamilyFeature`](../../../../dwm/src/main/java/com/adamkali/dwm/block/wood/WoodFamilyFeature.java)
   (`DOOR` vs `TALL_DOOR`, optional custom door/trapdoor JSON).
4. Alias fields from `family.blocks()` (`CARDINAL_PLANKS = CARDINAL.blocks().planks()`).
5. Append to `DWMBlocks.WOOD_FAMILIES`. Datagen loops that list:
   - [`WoodFamilyDatagen`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/WoodFamilyDatagen.java)
     (loot, recipes, tags, lang)
   - [`WoodFamilyClientDatagen`](../../../../dwm/src/client/java/com/adamkali/dwm/datagen/WoodFamilyClientDatagen.java)
     (block/item models)
6. `WoodFamilyRegistrar.wireRuntime` (already looped from
   `DWMBlocks.initialize()`) handles stripping, flammability, tabs.
7. Textures: `textures/block/<id>_planks.png`, `<id>_log.png`,
   `<id>_log_top.png`, leaves, sapling, plus
   `textures/entity/boat/<id>.png` (`ResourceValidationTests` requires distinct
   boat atlases).
8. Tests: copy
   [`AshWoodFamilyGameTests`](../../../../dwm/src/main/java/com/adamkali/dwm/gametest/AshWoodFamilyGameTests.java)
   / `WoodFamilyGameTestSupport`; unit coverage under
   `dwm/src/test/java/com/adamkali/dwm/block/`.

Signs, hanging signs, and potted saplings use `registerBlockWithoutItem`; the
registrar creates the `SignItem` / `HangingSignItem`.

## Gallifrey plants

Cross flowers: `FlowerBlock` + `gallifreyCrossPlant()`, potted via
`registerWithoutItem` + `FlowerPotBlock` + `gallifreyPottedPlant()`. Cane:
`SaccharineCaneBlock`. Models: `createPlantWithDefaultItem` /
`createCrossBlock` in `DWMModelProvider`. Loot: `dropSelf` +
`dropPottedContents`. Tags: `#minecraft:small_flowers` / `flower_pots`.
Composting is registered in `DWMBlocks.initialize()`. Tests:
[`GallifreyPlantsGameTests`](../../../../dwm/src/main/java/com/adamkali/dwm/gametest/GallifreyPlantsGameTests.java).

## Petrified (Skaro)

Not a `WoodFamilyDefinition`. Manual `RotatedPillarBlock` logs +
`registerStairs`/`registerSlab`/`registerWall` on planks +
`StrippableBlockRegistry` in `initialize()`. Copy `PETRIFIED_*` rather than
the wood registrar.
