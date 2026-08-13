# Feature: Azbantium

See also: [Docs Index](./index.md), [Gallifrey Building](./feature-gallifrey-building.md)

## Product Intent
Give Gallifrey a signature hard gem material—minable only with diamond-tier tools—so survival progression on the destination world has a clear late-game craft and equip goal.

## Player Outcomes
- Obtain Azbantium Ore (creative / follow-up worldgen) and process it into gems.
- Smelt or blast silk-touched ore into Azbantium gems.
- Craft diamond-tier tools, armor, and storage blocks from Azbantium.

## Implemented Now
- **Blocks:** Azbantium Ore, Azbantium Block (hardness 55 — harder than obsidian; requires diamond pickaxe)
- **Item:** Azbantium gem
- **Tools:** sword, pickaxe, axe, shovel, hoe (diamond-equivalent stats)
- **Armor:** helmet, chestplate, leggings, boots (diamond-equivalent defense/toughness)
- Localization, recipes, tags, and loot via datagen

## How It Works In-Game
1. Mine Azbantium Ore with a diamond (or better) pickaxe.
2. Ore drops Azbantium gems (Fortune applies; Silk Touch keeps the ore for smelting/blasting).
3. Craft a storage block from 9 gems, or craft tools and armor with sticks / shaped patterns like diamond gear.

## Known Constraints
- No nugget or raw-ore form (archive coverage is gem + tools/armor only).
- Natural ore placement in Gallifrey is a follow-up (see Gallifrey Dimension docs once shipped).

## Future Opportunities (Planned)
- Dark Star Alloy (DWM-021) as a higher netherite-style tier reusing this material pattern.
