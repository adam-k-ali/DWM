# Flat ingredient (datagen)

Default path for a standalone item whose model is `minecraft:item/generated`
(`FLAT_ITEM`). Gold example: `dwm:steel_ingot`. Sibling ingredients: `azbantium`,
dalekanium ingots, `zeiton_crystals` / `zeiton_powder`, `ferrite_powder`,
console circuit sprites.

Do **not** hand-write item/model JSON for this path — `DWMModelProvider`
emits them under `dwm/src/main/generated/`.

## 1. Register

In [`DWMItems.java`](../../../../dwm/src/main/java/com/adamkali/dwm/item/DWMItems.java):

```java
public static final Item EXAMPLE_INGOT = register(Item::new, "example_ingot");
```

The two-arg `register` uses a fresh `new Item.Properties()`. Pass an explicit
`Properties` only when stack size or components differ (circuits:
`.stacksTo(16)` via `registerCircuit`).

Do not add a BlockItem here. Placeable cubes belong in
[add-new-block](../../add-new-block/SKILL.md).

## 2. Creative tab

Static init is not enough. In `DWMItems.initialize()`, accept the item on the
right vanilla tab via
[`DWMCreativeTabs`](../../../../dwm/src/main/java/com/adamkali/dwm/item/DWMCreativeTabs.java):

| Tab | Typical items |
|-----|----------------|
| `INGREDIENTS` | Ingots, gems, powders, crystals |
| `TOOLS_AND_UTILITIES` | Circuits, gadgets (not tools — see [equipment.md](equipment.md)) |

Steel / azbantium / dalekanium / zeiton / ferrite ingredients are accepted
under `INGREDIENTS`. Circuits go under `TOOLS_AND_UTILITIES`.

## 3. Texture

Write `dwm/src/client/resources/assets/dwm/textures/item/<id>.png` with
[minecraft-sprite-style](../../minecraft-sprite-style/SKILL.md). Filename
**must** equal the registry path (`steel_ingot.png`).

`generateFlatItem` looks up `dwm:item/<id>`. Missing PNG →
`ResourceValidationTests.modelDefinedTexturesExist` fails.

Ingots / gems / powders have archetype templates in that sprite skill
(`ingot item`, `gem / crystal item`, `powder / dust`).

## 4. Datagen (required)

Providers are attached in
[`DWMClientDataGenerator`](../../../../dwm/src/client/java/com/adamkali/dwm/DWMClientDataGenerator.java).
Edit the Java providers; never patch generated JSON.

### Models

[`DWMModelProvider.generateItemModels`](../../../../dwm/src/client/java/com/adamkali/dwm/datagen/DWMModelProvider.java):

```java
itemModelGenerator.generateFlatItem(DWMItems.EXAMPLE_INGOT, ModelTemplates.FLAT_ITEM);
```

That writes both `assets/dwm/models/item/<id>.json` and
`assets/dwm/items/<id>.json`.

### Prune allowlist (required for datagen items)

[`pruneDatagenItemModels`](../../../../dwm/build.gradle) runs after
`runDatagen` and **deletes** every generated `assets/dwm/items/<id>.json`
whose filename does **not** contain an allowlisted substring (`steel`,
`azbantium`, `zeiton`, `ferrite`, `circuit`, `dalek`, …).

A new datagen item **must**:

1. Add a stable substring to that allowlist (prefer the family token already
   in the id: `example` for `example_ingot`).
2. Add a `generated*ItemModelsExist` guard in
   [`ResourceValidationTests`](../../../../dwm/src/test/java/com/adamkali/dwm/ResourceValidationTests.java)
   (copy `generatedSteelItemModelsExist`).

Hand-modeled items skip this — they never write generated item defs. See
[hand-modeled.md](hand-modeled.md).

### Recipes

[`DWMRecipeProvider`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMRecipeProvider.java)
when the family crafts. Typical ingredient patterns:

- 9 ingots ↔ compact block (`azbantium` / `AZBANTIUM_BLOCK`)
- Ore smelt / blast into the item (`oreSmelting` / `oreBlasting`)
- Shapeless blend (steel: iron + coal)

Skip recipes for items that have none today. Copy the closest sibling.

### Tags

[`DWMItemTagProvider`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMItemTagProvider.java)
only when something queries the item:

- New tool metal — `REPAIRS_*` key in
  [`DWMItemTags`](../../../../dwm/src/main/java/com/adamkali/dwm/item/DWMItemTags.java)
  plus `builder(DWMItemTags.REPAIRS_...).add(...)`
- Block-family copies (`copy(blockTag, itemTag)`) belong to add-new-block,
  not this path

Ingredients that are only recipe inputs do not need a new tag.

### Lang

[`DWMLanguageProvider.addItem`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMLanguageProvider.java)
with Title Case English (`"Steel Ingot"`). Do not edit
`dwm/src/main/generated/assets/dwm/lang/en_us.json` by hand.

## 5. Tests and docs

- Unit: extend
  [`SteelFamilyTest`](../../../../dwm/src/test/java/com/adamkali/dwm/item/SteelFamilyTest.java)
  or
  [`DalekaniumFamilyTest`](../../../../dwm/src/test/java/com/adamkali/dwm/item/DalekaniumFamilyTest.java)
  (registry non-null; material stats when this is a new metal).
- GameTest: smelting / crafting in-world — copy
  [`AzbantiumGameTests`](../../../../dwm/src/main/java/com/adamkali/dwm/gametest/AzbantiumGameTests.java)
  / dalekanium GameTests when the family already has them.
  Run `./dwm/gradlew runGametest` when GameTest code changes.
- Docs: [`docs/feature-steel.md`](../../../../dwm/docs/feature-steel.md),
  [`docs/feature-azbantium.md`](../../../../dwm/docs/feature-azbantium.md),
  [`docs/feature-dalekanium.md`](../../../../dwm/docs/feature-dalekanium.md),
  [`docs/feature-zeiton.md`](../../../../dwm/docs/feature-zeiton.md).

## 6. Generate

```bash
./dwm/gradlew runDatagen
rm -rf dwm/src/main/generated/.cache
./dwm/gradlew test
```

Commit intentional generated outputs (item defs, models, recipes, tags, lang).
Leave `.cache/` untracked.
