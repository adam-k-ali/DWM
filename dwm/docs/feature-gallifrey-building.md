# Feature: Gallifrey Building

See also: [Docs Index](./index.md), [Building Content System](./feature-building-content.md), [Gallifrey Dimension](./feature-gallifrey-dimension.md)

## Product Intent
Give players a Gallifrey-themed terrain and builder kit—stone, woods, and Citadel surfaces—so builds and the Gallifrey destination dimension share a coherent material language without custom texturing.

## Player Outcomes
- Build Gallifrey-inspired structures from stone, sandstone, and themed woods.
- Mix Citadel wall/panel/tile/glass with stone and wood families for civic or interior layouts.
- Craft survival-friendly variants (bricks, mossy blocks, stairs, slabs, doors) from base materials.

## Implemented Now
- **Gallifrey stone family**
  - Building set: stone, stone bricks (including chiseled, cracked, and mossy, plus stairs/slab/wall), cobblestone and mossy cobblestone (stairs/slab/wall), smooth stone (slab), sandstone, cut sandstone, chiseled sandstone
  - Terrain blocks: Gallifrey grass (deep-red top), dirt, coarse dirt, and sand
- **Orange sand family** (distinct from Gallifrey sand; vanilla red-sandstone parity)
  - Terrain: orange sand
  - Building: orange sandstone (stairs, slab, wall), cut orange sandstone (slab), chiseled orange sandstone, smooth orange sandstone (stairs, slab)
- **Wood families:** Ash, Dark Ash, and Cardinal
  - Shared set: planks, logs/wood (and stripped), leaves, sapling (and potted), stairs, slab, fence, fence gate, button, pressure plate, signs, hanging signs, boats
  - Doors and trapdoors on all three families; Cardinal uses a three-block-tall door
- **Citadel family:** Citadel wall, panel, tile, and glass
- **Azbantium:** storage block (ore is mined in Gallifrey; gem crafts tools/armor — see [Azbantium](./feature-azbantium.md))
- **Gallifrey vanilla ores:** coal, iron, gold, and diamond ores textured for Gallifrey stone (drop vanilla items; mined in the destination dimension)
- **Gallifrey decorative plants**
  - Cross flowers: Flower of Remembrance, Moonlight Bloom (with potted variants)
  - Saccharine Cane (stackable decorative cane; no water requirement, no growth)
  - Also decorate Gallifrey biomes (flowers on plains/forest; cane on wastes/badlands); still placeable anywhere valid as builder blocks
- Localization, creative-tab placement, and recipe/tag coverage via datagen for these families

## How It Works In-Game
1. Gather Gallifrey stone, sand, orange sand, or wood logs (creative or survival).
2. Craft bricks, sandstone variants, planks, and wood furnishings from the provided recipes.
3. Combine stone, wood, and Citadel blocks for exteriors, interiors, and set dressing.
4. Place flowers on dirt/grass (including Gallifrey terrain) and stack saccharine cane on dirt or sand for set dressing — or gather them from Gallifrey biomes.

## Known Constraints
- Some wood accessory textures (for example signs or GUI icons) may still be provisional.
- Roundels, TARDIS walls, and Chronoplasm remain under the separate Building Content System feature.
- Krubella and plutarch archive textures are opaque cubes (not plant sprites) and are not registered yet.

## Future Opportunities (Planned)
- Soul wood family (fourth Gallifrey wood set).
- Krubella / plutarch plants once proper cross (or intentional cube) sprites exist.
- Wire archive colormaps for tinted grass/foliage if plants need biome tinting (grass block uses pre-colored deep-red textures).
- Richer Gallifrey destination content (mobs, villages) on top of the shipped dimension.
