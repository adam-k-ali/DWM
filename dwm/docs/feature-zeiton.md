# Feature: Zeiton

See also: [Docs Index](./index.md), [Gallifrey Dimension](./feature-gallifrey-dimension.md), [Azbantium](./feature-azbantium.md)

## Product Intent
Give Gallifrey a common energy gem—and rare Overworld traces—so later TARDIS fuel and console-circuit crafts have a distinct ingredient loop without requiring a diamond pickaxe.

## Player Outcomes
- Find Zeiton Ore in Gallifrey stone underground, and occasionally in Overworld stone caves.
- Mine crystals with an iron pickaxe (Fortune applies; Silk Touch keeps the ore).
- Smelt or blast silk-touched ore into crystals, then convert crystals to powder (and back).
- Craft Ferrite Powder from iron and redstone as an Overworld-side circuit dust.

## Implemented Now
- **Block:** Zeiton Ore (hardness 3.0 — iron pickaxe required)
- **Items:** Zeiton Crystals, Zeiton Powder, Ferrite Powder
- Ore generation in all Gallifrey biomes replacing `#dwm:gallifrey_ore_replaceables` (Gallifrey stone)
- Rare Overworld traces replacing `#minecraft:stone_ore_replaceables` (no deepslate variant)
- Localization, recipes, tags, and loot via datagen
- **TARDIS fuel:** Zeiton Crystals used on the Panel 5 refueler add 25 artron (tank 0–500). Powder is not fuel — convert 4 powder → 1 crystal first.
- **Late console circuits:** Zeiton powder (2) or crystals (player locator) are cheaper alternatives to the ferrite+vanilla recipes. Zeiton is not required to leave the Overworld — planet locator is ferrite + ender pearl.

## How It Works In-Game
1. Mine Zeiton Ore with an iron (or better) pickaxe. Wood and stone picks drop nothing.
2. Ore drops Zeiton Crystals (Fortune applies; Silk Touch keeps the ore for smelting/blasting).
3. Shapeless: 1 crystal → 4 powder; 4 powder → 1 crystal.
4. Shapeless: 1 iron ingot + 1 redstone → Ferrite Powder.
5. Use Zeiton Crystals on the TARDIS Panel 5 refueler to add 25 artron (clamp 500). Powder is for late circuit crafts (or convert 4 → 1 crystal for the tank).

Overworld traces are a lucky find, not a reliable farm. Gallifrey remains the place to stockpile Zeiton.

## Known Constraints
- No storage block, nugget, or deepslate ore (archive coverage is ore + crystals/powder + ferrite only).
- Ferrite is craft-only; there is no ferrite ore.

## Future Opportunities (Planned)
- Zeiton torch / repeater / rails (out of scope here).
