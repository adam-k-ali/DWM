# Feature: Block and Item Definitions

See also: [Docs Index](./index.md), [Entity Definitions](./feature-entity-definitions.md)

## Product Intent

Author plain blocks and items (properties, tool tiers, armour slots) as JSON instead of `register(...)` lines plus `DWMBlockSettings` helpers, so new content and balance changes are data edits. Behaviour-heavy classes (TARDIS blocks, plants, wood families, sonic screwdrivers, circuits, spawn eggs) stay in Java.

## Player Outcomes

Unchanged: every migrated block and item registers with the same id, class and properties as before. A snapshot of all `dwm:` blocks was diffed before and after the migration.

## Layout

- `data/dwm/block/definition/<id>.json` and `data/dwm/item/definition/<id>.json`; the file stem must equal the id.
- Loaded from the mod jar at init, before registration. `/reload` does not apply; restart to pick up edits.
- `parent` inherits from another definition; `properties` merge per key (child wins), everything else is replaced. `"abstract": true` marks a template that is never registered (e.g. `roundel_base`).
- Java keeps its constants: `public static final Block X = BlockDefinitions.register("x");` so datagen, tests and other code are unaffected.
- A definition with **no** Java field is registered automatically (`registerUnclaimed()`), so a data-only block/item needs just JSON plus its assets, lang and datagen entries. A stairs/slab/wall registered this way may name an unclaimed `base`; the base is registered first.

## Block JSON

```json
{ "block": "dwm:gallifrey_stone", "parent": "dwm:gallifrey_stone_base" }
```

| Field | Meaning |
| --- | --- |
| `block` | Block id (required) |
| `type` | `dwm:block` (default), `dwm:stairs`, `dwm:slab`, `dwm:wall`, `dwm:colored_falling` |
| `base` | Block whose properties/state stairs, slab and wall copy; for `BlockDefinitions.register(...)` calls from Java it must already be registered (data-only blocks order themselves) |
| `falling_dust_color` | RGB int for `dwm:colored_falling` |
| `item` | `false` skips the `BlockItem` (default `true`) |
| `creative_tab` | Vanilla tab id, for data-only blocks |
| `properties` | See below |

`properties` (all optional): `map_color`, `sound` (lower-case `MapColor` / `SoundType` constant name), `instrument`, `push_reaction`, `offset_type` (lower-case enum names), `strength` (`destroy_time`, optional `explosion_resistance` defaulting to `destroy_time`), `requires_correct_tool`, `light_level` (0-15), `occlude`, `collision`, `instabreak`, `random_ticks`, `ignited_by_lava`.

## Item JSON

```json
{ "item": "dwm:steel_sword", "type": "dwm:sword",
  "properties": { "tool": { "material": "steel", "attack_damage": 3.0, "attack_speed": -2.4 } } }
```

`type`: `dwm:item` (default), `dwm:sword`, `dwm:pickaxe`, `dwm:shovel`, `dwm:axe`, `dwm:hoe` (need `properties.tool`), `dwm:armor` (needs `properties.armor` with `material` and `slot`). `material` is the lower-case constant name in `DWMToolMaterials` / `DWMArmorMaterials`. Also `stacks_to` and `use_item_description_prefix`.

## Not yet migrated

TARDIS/console/decor blocks, plants, wood families, ores with custom classes, circuits, sonics, spawn eggs, EVA helmet, and `DWMBlockSettings` helpers still used by them.

## Validation

JSON Schemas live in `src/test/resources/schemas/{block,item}_definition.schema.json` (`ResourceValidationTests`); `BlockDefinitionTest` / `ItemDefinitionTest` cover parsing, inheritance, errors and registration.
