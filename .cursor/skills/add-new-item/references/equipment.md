# Tools, armor, spawn eggs

Use when the request is a **set** (full tool tier, armor suit) or a spawn egg
— not a single ingredient. Classify, then copy the matching sibling. Do not
invent a new `ToolMaterial` / `ArmorMaterial` unless the task is a new metal
or suit; extend the existing family instead.

Still register in
[`DWMItems`](../../../../dwm/src/main/java/com/adamkali/dwm/item/DWMItems.java)
and run the [flat-datagen](flat-datagen.md) prune-allowlist + lang + recipe
steps. Models use `FLAT_HANDHELD_ITEM` for tools and `FLAT_ITEM` for armor /
spawn eggs.

## Tools

Gold example: steel tools (`STEEL_SWORD` … `STEEL_HOE`). Siblings: silver /
bronze dalekanium, azbantium.

1. Material lives on
   [`DWMToolMaterials`](../../../../dwm/src/main/java/com/adamkali/dwm/item/DWMToolMaterials.java)
   (`incorrectBlocksForDrops`, durability, speed, attack bonus, enchantability,
   `REPAIRS_*` tag). Steel sits between iron and diamond — copy those numbers
   when adding a sibling metal, do not guess.
2. Register with vanilla factories (same attack/speed as the steel set):

```java
public static final Item EXAMPLE_SWORD = register(
        Item::new,
        new Item.Properties().sword(DWMToolMaterials.EXAMPLE, 3.0F, -2.4F),
        "example_sword"
);
public static final Item EXAMPLE_SHOVEL = register(
        props -> new ShovelItem(DWMToolMaterials.EXAMPLE, 1.5F, -3.0F, props),
        "example_shovel"
);
```

Sword / pickaxe use `Item::new` + `Properties.sword/pickaxe`. Shovel / axe /
hoe use the dedicated item classes. Copy steel exactly.

3. Models:
   `itemModelGenerator.generateFlatItem(..., ModelTemplates.FLAT_HANDHELD_ITEM)`
   in
   [`DWMModelProvider.generateItemModels`](../../../../dwm/src/client/java/com/adamkali/dwm/datagen/DWMModelProvider.java).
4. Tags in
   [`DWMItemTagProvider`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMItemTagProvider.java):
   `ItemTags.SWORDS` / `SHOVELS` / `PICKAXES` / `AXES` / `HOES`, plus
   `builder(DWMItemTags.REPAIRS_EXAMPLE_EQUIPMENT).add(DWMItems.EXAMPLE_INGOT...)`.
   Declare the repair key on
   [`DWMItemTags`](../../../../dwm/src/main/java/com/adamkali/dwm/item/DWMItemTags.java)
   first.
5. Recipes: copy the azbantium / steel shaped patterns in
   [`DWMRecipeProvider`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMRecipeProvider.java)
   (`COMBAT` for swords, `TOOLS` for the rest).
6. Tabs: swords under `COMBAT`; shovel / pickaxe / axe / hoe under
   `TOOLS_AND_UTILITIES`.
7. Textures: sprite-skill tool archetypes (`tool — pickaxe`, `tool — sword`).
   Filename = registry path (`steel_pickaxe.png`).
8. Tests: extend
   [`SteelFamilyTest`](../../../../dwm/src/test/java/com/adamkali/dwm/item/SteelFamilyTest.java)
   / [`DalekaniumFamilyTest`](../../../../dwm/src/test/java/com/adamkali/dwm/item/DalekaniumFamilyTest.java).
   Prune guard: copy `generatedSteelItemModelsExist`.

## Armor

Gold example: azbantium (`AZBANTIUM_HELMET` … `BOOTS`). EVA suit is leather-tier
plus a visor overlay — copy it **only** when the task needs a camera overlay.

1. Material on
   [`DWMArmorMaterials`](../../../../dwm/src/main/java/com/adamkali/dwm/item/DWMArmorMaterials.java)
   — durability, defense map, equip sound, toughness, `REPAIRS_*` tag, and an
   `EquipmentAsset` key (`dwm:azbantium`).
2. Register with `Properties.humanoidArmor(material, ArmorType.*)`.
3. Models: `generateFlatItem(..., ModelTemplates.FLAT_ITEM)`.
4. Worn layers (hand client assets, not datagen):
   - [`equipment/azbantium.json`](../../../../dwm/src/client/resources/assets/dwm/equipment/azbantium.json)
     (`humanoid` + `humanoid_leggings` layers pointing at `dwm:<asset_id>`)
   - `textures/entity/equipment/humanoid/<asset_id>.png`
   - `textures/entity/equipment/humanoid_leggings/<asset_id>.png`
5. Tags: `ItemTags.HEAD_ARMOR` / `CHEST_ARMOR` / `LEG_ARMOR` / `FOOT_ARMOR`
   plus the repair tag. EVA also has `DWMItemTags.EVA_SUIT`.
6. Recipes: copy azbantium shaped armor in `DWMRecipeProvider` (`COMBAT`).
7. Tab: `COMBAT`.
8. EVA extra — helmet uses `.component(DataComponents.EQUIPPABLE,
   DWMArmorMaterials.evaSuitHelmet())` for a first-person visor
   (`textures/misc/eva_suit_overlay.png`). Other pieces stay on
   `humanoidArmor` defaults. Tests:
   [`EvaSuitOverlayTest`](../../../../dwm/src/test/java/com/adamkali/dwm/item/EvaSuitOverlayTest.java),
   `evaSuitTexturesMatchContract` / `generatedEvaSuitItemModelsExist`.

## Spawn eggs

Gold example: `DALEK_SPAWN_EGG`. Fields are declared uninitialized and assigned
inside `DWMItems.initialize()` **after**
[`DWMEntityTypes`](../../../../dwm/src/main/java/com/adamkali/dwm/entity/DWMEntityTypes.java)
exists (`DWMMain` already orders this).

```java
BROAKIR_SPAWN_EGG = register(
        SpawnEggItem::new,
        new Item.Properties().spawnEgg(DWMEntityTypes.BROAKIR),
        "broakir_spawn_egg"
);
```

- Model: `generateFlatItem(..., ModelTemplates.FLAT_ITEM)`
- Tab: `SPAWN_EGGS`
- Prune allowlist must include a substring of the id (`broakir`, `dalek`, …)
  plus a `generated*ItemModelExists` guard (see
  `generatedDalekSpawnEggItemModelExists` in
  [`ResourceValidationTests`](../../../../dwm/src/test/java/com/adamkali/dwm/ResourceValidationTests.java))
- Do not add a spawn egg unless the entity already exists (or is part of the
  same task). Entity registration is out of this skill.

## What stays generated vs hand-authored

| Asset | Hand (`src/client/resources`) | Datagen (`src/main/generated`) |
|-------|-------------------------------|--------------------------------|
| Flat / handheld item JSON | no | yes |
| Armor equipment JSON + worn PNGs | yes | no |
| Recipes, tags, lang | no | yes |
