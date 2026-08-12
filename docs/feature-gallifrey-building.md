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
  - Building set: stone, stone bricks (including chiseled, cracked, and mossy), cobblestone and mossy cobblestone, smooth stone, sandstone, cut sandstone, chiseled sandstone
  - Terrain blocks: Gallifrey grass (silver top), dirt, coarse dirt, and sand
- **Wood families:** Ash, Dark Ash, and Cardinal
  - Shared set: planks, logs/wood (and stripped), leaves, sapling (and potted), stairs, slab, fence, fence gate, button, pressure plate, signs, hanging signs, boats
  - Doors and trapdoors on all three families; Cardinal uses a three-block-tall door
- **Citadel family:** Citadel wall, panel, tile, and glass
- Localization, creative-tab placement, and recipe/tag coverage via datagen for these families

## How It Works In-Game
1. Gather Gallifrey stone, sand, or wood logs (creative or survival).
2. Craft bricks, sandstone variants, planks, and wood furnishings from the provided recipes.
3. Combine stone, wood, and Citadel blocks for exteriors, interiors, and set dressing.

## Known Constraints
- Some wood accessory textures (for example signs, boats, or GUI icons) may still be provisional.
- Roundels, TARDIS walls, and Chronoplasm remain under the separate Building Content System feature.

## Future Opportunities (Planned)
- Soul wood family (fourth Gallifrey wood set).
- Gallifrey decorative plants.
- Orange sand and sandstone variants (distinct from Gallifrey sand).
- Wire archive colormaps for tinted grass/foliage if plants need biome tinting (grass block uses pre-colored silver textures).
- Richer Gallifrey destination content (mobs, villages) on top of the shipped dimension.
