# Hand-modeled items

Use when datagen `generateFlatItem` cannot produce the item definition: custom
`display` transforms, a GUI-vs-world model select, or a reused vanilla sprite.
Skip `DWMModelProvider.generateFlatItem` for these ids. Still run recipes, lang,
and tabs through datagen.

Gameplay subclasses (use/tick/components) stay on [custom.md](custom.md) —
open **both** when the item has a class *and* hand JSON (sonic, TARDIS key,
field guide).

## When you need this path

| Pattern | Example | Item JSON |
|---------|---------|-----------|
| Generated sprite + custom `display` | `tardis_key` | `minecraft:model` → `dwm:item/tardis_key` |
| GUI model vs in-world model | `sonic_*_doctor` | `minecraft:select` on `display_context` |
| Reused vanilla sprite | `field_guide` | `minecraft:model` → `dwm:item/field_guide` (layer0 `written_book`) |
| Special in-hand mesh | `radiation_meter` | `minecraft:special` — see [custom.md](custom.md) |

Do not invent a datagen helper for these. Copy the closest sibling JSON.

## Hand assets

Under `dwm/src/client/resources/assets/dwm/`:

1. `items/<id>.json` — item definition consumed by the 26.x item model system.
2. `models/item/<id>.json` — blockbench/`generated` model (`parent`,
   `textures.layer0`, optional `display`).
3. `textures/item/<id>.png` unless the model points at another atlas
   (`field_guide` uses `minecraft:item/written_book`; radiation meter uses an
   entity texture — [custom.md](custom.md)).

Gold files:

- [`items/tardis_key.json`](../../../../dwm/src/client/resources/assets/dwm/items/tardis_key.json)
  + [`models/item/tardis_key.json`](../../../../dwm/src/client/resources/assets/dwm/models/item/tardis_key.json)
- [`items/sonic_second_doctor.json`](../../../../dwm/src/client/resources/assets/dwm/items/sonic_second_doctor.json)
  (`gui` case → `sonic_second_doctor_gui`, fallback world model)
- [`items/field_guide.json`](../../../../dwm/src/client/resources/assets/dwm/items/field_guide.json)

`ResourceValidationTests.validateItemModels` checks hand models against
[`item_model.schema.json`](../../../../dwm/src/test/resources/schemas/item_model.schema.json).
`modelDefinedTexturesExist` still requires every `dwm:` texture path to exist.

## Do not double-own the model

- Do **not** call `generateFlatItem` for a hand-modeled id. Datagen would
  emit a second `items/<id>.json` that `pruneDatagenItemModels` then deletes
  (these ids are not on the allowlist — that is intentional).
- Do **not** add the id to the prune allowlist unless you are *switching* it
  to datagen.
- Hand JSON lives under `src/client/resources`, never under
  `src/main/generated`.

## Sonic casings extra

New Doctor casings must:

1. Register `SonicScrewdriverItem` in `DWMItems` (`.stacksTo(1)`).
2. Hand item + world + GUI models (copy `sonic_second_doctor`).
3. Append the id to the **hand** tag
   [`data/dwm/tags/item/sonic_screwdrivers.json`](../../../../dwm/src/main/resources/data/dwm/tags/item/sonic_screwdrivers.json).
   This tag is **not** emitted by `DWMItemTagProvider`.
4. Recipes / lang / `TOOLS_AND_UTILITIES` tab — copy the existing casing
   recipes in
   [`DWMRecipeProvider`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMRecipeProvider.java)
   (`sonicCasing(...)` transmute + setting shapeless recipes).
5. Docs:
   [`docs/feature-sonic-screwdrivers.md`](../../../../dwm/docs/feature-sonic-screwdrivers.md).

Do not re-derive field modes, pairing, or ping — that is tardis/sonic logic
([custom.md](custom.md) +
[tardis/AGENTS.md](../../../../dwm/src/main/java/com/adamkali/dwm/tardis/AGENTS.md)).

## Field guide extra

`FIELD_GUIDE` is **not** on a creative tab; it is granted
([`FieldGuideGrant`](../../../../dwm/src/main/java/com/adamkali/dwm/guide/FieldGuideGrant.java)).
Copy that grant path only when the task is another grant-only item. Catalog
pages are out of this skill
([`docs/feature-field-guide.md`](../../../../dwm/docs/feature-field-guide.md)).

## Datagen still required

Loot is N/A for items. Still:

- `addItem` in
  [`DWMLanguageProvider`](../../../../dwm/src/main/java/com/adamkali/dwm/datagen/DWMLanguageProvider.java)
- recipes when the sibling crafts
- `content.accept(...)` unless grant-only

After adding hand JSON + PNG, still run `./dwm/gradlew runDatagen` so lang /
recipes regenerate, then `./dwm/gradlew test`.

## What stays generated vs hand-authored

| Asset | Hand (`src/client/resources`) | Datagen (`src/main/generated`) |
|-------|-------------------------------|--------------------------------|
| Custom item/model JSON | yes | no |
| Flat `generateFlatItem` models | no | yes (other items only) |
| Recipes, tags (except sonic tag), lang | no | yes |
| `#dwm:sonic_screwdrivers` | yes (`src/main/resources`) | no |
