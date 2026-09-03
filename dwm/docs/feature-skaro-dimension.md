# Feature: Skaro Dimension

See also: [Docs Index](./index.md), [Gallifrey Dimension](./feature-gallifrey-dimension.md), [TARDIS Exterior Block](./feature-tardis-block.md), [Building Content System](./feature-building-content.md), [Differentiation Strategy](./differentiation-strategy.md)

## Product Intent
Give TARDIS travel a hostile destination world that is not another recolored Overworld: post–Thousand Year War Skaro, where preparation matters, instruments warn before the doors open, and every new material family is useful to builders.

## Player Outcomes
- Reach Skaro from the First Doctor console by cycling the planet locator, then land in a chosen region with the biome dial.
- Read radiation on the console, sonic scan, and a handheld meter before stepping outside; survive ambient exposure with a full protective suit.
- Explore five distinct regions — irradiated wastes, a petrified jungle, the Drammankin mire and mountains, and a rare Thal plateau — built from vanilla terrain composition, petrified wood, and Dalek architecture.
- Encounter Dalek patrols, mutant fauna, Thal settlements, war landmarks, a Kaled bunker, and the Dalek city of Kaalann.

## Implemented Now
- **Petrified wood family** (DWM-064): mineralized Skaro trunks and builder variants.
  - Natural: `petrified_log`, `petrified_wood` (axis pillars).
  - Building: `stripped_petrified_log`, `stripped_petrified_wood`, `petrified_planks`, `petrified_stairs`, `petrified_slab`, `petrified_wall`.
  - Nonflammable and pickaxe-mineable; axe-strippable; survival craftable (log→planks, stairs/slab/wall, stonecutting).
  - Tags: `#dwm:petrified_blocks`, `#dwm:petrified_logs` (not `#minecraft:logs_that_burn`).
- **Vanilla terrain palette (intentional, not placeholder):** later Skaro surface rules compose vanilla blocks only — stone, tuff, deepslate, gravel, basalt, sand, red sand, terracotta, dirt, coarse dirt, rooted dirt, mud, and podzol. There is no custom Skaro stone, sand, sandstone, dust, or dirt family.

## Planned (not yet in the jar)
- Dimension `dwm:skaro`, biome tag `#dwm:is_skaro`, and five biomes: Irradiated Wastes, Petrified Jungle, Drammankin Mire, Drammankin Mountains, Thal Plateau (DWM-066).
- Dalek architecture builder family (DWM-065).
- Radiation, protective suit, and meter (DWM-067).
- Flora features including petrified tree placement (DWM-068).
- Structures, Daleks/Thals/fauna population, Kaalann, and related tickets (DWM-069–074).
- TARDIS planet locator / biome dial travel to Skaro.

## How It Works In-Game
1. Mine petrified logs with a pickaxe (or strip with an axe), then craft planks, stairs, slabs, and walls like other builder sets.
2. Once the dimension ships: at the console, cycle the planet locator to **Skaro**, pick a biome, check radiation, then materialise.
3. Debug (when the dimension exists): `/execute in dwm:skaro run tp @s ~ 128 ~`.

## Known Constraints
- Skaro is a TARDIS destination, not an alternate travel network or a campaign with bosses, quests, reputation, or a research GUI.
- Terrain identity comes from **vanilla composition**, atmosphere, petrified vegetation, and Dalek architecture — not a full custom stone, sand, and dirt palette. That composition choice is product intent for DWM-066, not temporary art.
- Petrified material is mineralized wood: pickaxe-effective, nonflammable, no saplings or leaves.
- Dalek architecture, radiation, entities, and worldgen remain owned by their child tickets under E-007.

## Future Opportunities
- Custom dimension effects for Skaro sky, fog, and clouds.
- Thal trades or professions if a later settlement loop needs them.
- Additional Dalek ranks, vehicles, or city-state mechanics beyond the baseline patrol and structure presence.
- Protective-suit degradation, medicines, or structure-scale radiation hotspots.
