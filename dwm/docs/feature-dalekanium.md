# Feature: Dalekanium

See also: [Docs Index](./index.md), [Skaro Dimension](./feature-skaro-dimension.md), [Steel](./feature-steel.md), [Colour palettes](./palettes/silver_dalekanium.md)

## Product Intent
Give Skaro a signature metal that players mine, smelt, and alloy: silver Dalekanium from ore, then bronze Dalekanium with copper, both slightly stronger than steel tools without reaching diamond.

## Player Outcomes
- Find Dalekanium Ore underground on Skaro.
- Smelt or blast the ore into silver Dalekanium ingots.
- Alloy silver Dalekanium with copper to make bronze Dalekanium ingots.
- Craft silver and bronze Dalekanium tool sets (sword, pickaxe, axe, shovel, hoe).
- Craft silver and bronze Dalekanium panels from matching ingots, then stonecut riveted walls.
- Repair each set with its matching ingot.

## Implemented Now
- **Blocks:** Dalekanium Ore (`dwm:dalekanium_ore`) — hardness 3.0, iron pickaxe required, drops itself
- **Architecture (DWM-065 slice):** Silver/Bronze Dalekanium Panel and Riveted Wall — metal cubes, hardness 4.0, iron pickaxe required, drop themselves
- **Items:** Silver Dalekanium Ingot, Bronze Dalekanium Ingot
- **Tools:** silver and bronze sword, pickaxe, axe, shovel, hoe
- **Tier:** iron harvest level (`INCORRECT_FOR_IRON_TOOL`)
  - Silver: durability 550, mining speed 7.2, attack bonus 2.6, enchantability 13
  - Bronze: durability 625, mining speed 7.5, attack bonus 2.75, enchantability 14
- Skaro-only worldgen replacing `#minecraft:stone_ore_replaceables` (vein size 9, count 10, triangle Y −24…56)
- Localization, recipes, tags, loot, and models via datagen

## How It Works In-Game
1. Travel to Skaro and mine Dalekanium Ore with an iron (or better) pickaxe.
2. Smelt or blast the ore into a silver Dalekanium ingot.
3. Combine one silver Dalekanium ingot with one copper ingot for a bronze Dalekanium ingot.
4. Craft tools with the matching ingot and sticks using the same shaped patterns as iron/diamond tools.
5. Craft a 2×2 of matching ingots into 8 panels; stonecut panels into riveted walls (and back).
6. Both tool sets mine the same blocks as iron and steel, but last longer and mine/hit slightly harder; bronze is a small step above silver.

## Known Constraints
- No Dalekanium armor, storage block, or raw ore in this feature. Remaining architecture (wall, floor, light, door, damaged variants) stays DWM-065.
- No deepslate variant; ore does not generate on Gallifrey or in the Overworld.
- Charcoal-style substitutes do not apply: bronze specifically requires `minecraft:copper_ingot`.

## Future Opportunities (Planned)
- Remaining Dalek architecture family (DWM-065) may reuse silver and bronze Dalekanium ingots and the existing panel/riveted-wall stonecutting set.
