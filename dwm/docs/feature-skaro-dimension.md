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
- Dimension `dwm:skaro` (noise overworld-style generator, overworld-like dimension type). Debug: `/execute in dwm:skaro run tp @s ~ 128 ~`.
- Biomes (tag `#dwm:is_skaro`): `dwm:skaro_irradiated_wastes`, `dwm:skaro_petrified_jungle`, `dwm:skaro_drammankin_mire`, `dwm:skaro_drammankin_mountains`, `dwm:skaro_thal_plateau`.
- **Distribution:** Irradiated Wastes dominate multi-noise climate; Petrified Jungle, Drammankin Mire, and Drammankin Mountains occupy wetter/highland niches; Thal Plateau is rare (high climate offset) and reads comparatively safe via a clearer muted sky.
- **Surface rules** compose the vanilla DWM-064 palette only (no custom Skaro stone/sand/dirt, no grass, no petrified logs — tree placement is DWM-068):
  - Irradiated Wastes: sand / red sand / terracotta over sand; sickly yellow-green sky, fog, and water.
  - Petrified Jungle: podzol / rooted dirt / coarse dirt over dirt; ashen grey-brown atmosphere.
  - Drammankin Mire: mud over dirt; olive fog and murky water.
  - Drammankin Mountains: stone / tuff / gravel over tuff; cold ash-grey atmosphere.
  - Thal Plateau: dirt / coarse dirt / terracotta; muted blue-grey sky.
- Baseline caves, lakes, and vanilla ores in every biome. Empty biome spawn tables (no Overworld hostiles, cave fauna, or farm animals). No Gallifrey plants, woods, ores, or fauna. Daleks, Thals, and Skaro fauna remain planned.
- TARDIS planet locator discovers Skaro automatically as a loaded world. `BiomeSelectorLogic` maps `dwm:skaro` to `#dwm:is_skaro`.
- **Petrified wood family** (DWM-064): mineralized Skaro trunks and builder variants.
  - Natural: `petrified_log`, `petrified_wood` (axis pillars).
  - Building: `stripped_petrified_log`, `stripped_petrified_wood`, `petrified_planks`, `petrified_stairs`, `petrified_slab`, `petrified_wall`.
  - Nonflammable and pickaxe-mineable; axe-strippable; survival craftable (log→planks, stairs/slab/wall, stonecutting).
  - Tags: `#dwm:petrified_blocks`, `#dwm:petrified_logs` (not `#minecraft:logs_that_burn`).
- **Vanilla terrain palette (intentional, not placeholder):** stone, tuff, deepslate, gravel, basalt, sand, red sand, terracotta, dirt, coarse dirt, rooted dirt, mud, and podzol. There is no custom Skaro stone, sand, sandstone, dust, or dirt family.

## Planned (not yet in the jar)
- Dalek architecture builder family (DWM-065).
- Radiation, protective suit, and meter (DWM-067).
- Flora features including petrified tree placement (DWM-068).
- Structures, Daleks/Thals/fauna population, Kaalann, and related tickets (DWM-069–074).

## How It Works In-Game
1. Mine petrified logs with a pickaxe (or strip with an axe), then craft planks, stairs, slabs, and walls like other builder sets.
2. At the console, cycle the planet locator until **Skaro** is selected, then cycle the biome dial among the five tagged regions.
3. Pull the materialisation lever to dematerialise, then again in flight to land on Skaro. Radiation readouts are not shipped yet (DWM-067).
4. Debug without a TARDIS: `/execute in dwm:skaro run tp @s ~ 128 ~`.

## Known Constraints
- Skaro is a TARDIS destination, not an alternate travel network or a campaign with bosses, quests, reputation, or a research GUI.
- Terrain identity comes from **vanilla composition**, atmosphere, petrified vegetation, and Dalek architecture — not a full custom stone, sand, and dirt palette. That composition choice is product intent for DWM-066, not temporary art.
- Petrified material is mineralized wood: pickaxe-effective, nonflammable, no saplings or leaves.
- Sky/fog use biome colors and overworld dimension effects; there is no custom Skaro sky renderer.
- Dalek architecture, radiation, flora, entities, and structures remain owned by their child tickets under E-007.

## Future Opportunities
- Custom dimension effects for Skaro sky, fog, and clouds.
- Thal trades or professions if a later settlement loop needs them.
- Additional Dalek ranks, vehicles, or city-state mechanics beyond the baseline patrol and structure presence.
- Protective-suit degradation, medicines, or structure-scale radiation hotspots.
