# Feature: Steel

See also: [Docs Index](./index.md), [Colour palette](./palettes/steel.md), [Azbantium](./feature-azbantium.md)

## Product Intent
Give survival players a mid-tier metal between iron and diamond: craftable from common ingredients, useful for tools, without introducing a custom furnace or ore.

## Player Outcomes
- Craft steel ingots from iron and coal.
- Craft a full steel tool set (sword, pickaxe, axe, shovel, hoe).
- Repair steel tools with steel ingots.

## Implemented Now
- **Item:** Steel Ingot (`dwm:steel_ingot`) — shapeless `minecraft:iron_ingot` + `minecraft:coal` (charcoal does not substitute)
- **Tools:** sword, pickaxe, axe, shovel, hoe
- **Tier:** iron harvest level (`INCORRECT_FOR_IRON_TOOL`); durability 500, mining speed 7.0, attack bonus 2.5, enchantability 12 (strictly between iron and diamond)
- Localization, recipes, tags, and models via datagen

## How It Works In-Game
1. Combine one iron ingot and one coal in a crafting grid to make a steel ingot.
2. Craft tools with steel ingots and sticks using the same shaped patterns as iron/diamond tools.
3. Steel tools mine the same blocks as iron tools, but last longer and mine/hit slightly harder.

## Known Constraints
- No steel armor, storage block, or grille in this feature (architecture grille material is separate Skaro work).
- No ore or smelting path — steel is craft-only.

## Future Opportunities (Planned)
- Storage block packing and Dalek architecture grille family may reuse `steel_ingot`.
