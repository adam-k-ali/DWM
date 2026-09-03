# Feature: Skaro Dimension

See also: [Docs Index](./index.md), [Gallifrey Dimension](./feature-gallifrey-dimension.md), [TARDIS Exterior Block](./feature-tardis-block.md), [Differentiation Strategy](./differentiation-strategy.md)

## Product Intent
Give TARDIS travel a hostile destination world that is not another recolored Overworld: post–Thousand Year War Skaro, where preparation matters, instruments warn before the doors open, and every new material family is useful to builders.

## Player Outcomes
- Reach Skaro from the First Doctor console by cycling the planet locator, then land in a chosen region with the biome dial.
- Read radiation on the console, sonic scan, and a handheld meter before stepping outside; survive ambient exposure with a full protective suit.
- Explore five distinct regions — irradiated wastes, a petrified jungle, the Drammankin mire and mountains, and a rare Thal plateau — built from vanilla terrain composition, petrified wood, and Dalek architecture.
- Encounter Dalek patrols, mutant fauna, Thal settlements, war landmarks, a Kaled bunker, and the Dalek city of Kaalann.

## Implemented Now
- Dimension `dwm:skaro` (noise overworld-style generator, overworld-like dimension type). Travel is TARDIS-only; there is no custom portal.
- Biome tag `#dwm:is_skaro`. The biome dial cycles exactly these five regions:
  - **Irradiated Wastes** (`dwm:skaro_irradiated_wastes`) — the dominant landscape; barren, high radiation, war wreckage.
  - **Petrified Jungle** (`dwm:skaro_petrified_jungle`) — dense dead trunks and branches; no living canopy.
  - **Drammankin Mire** (`dwm:skaro_drammankin_mire`) — wet, high radiation; shoreline reeds and aquatic mutants.
  - **Drammankin Mountains** (`dwm:skaro_drammankin_mountains`) — dry uplands and observation ruins.
  - **Thal Plateau** (`dwm:skaro_thal_plateau`) — rare, comparatively safe, and the only arable region.
- Terrain uses vanilla blocks (stone, tuff, deepslate, gravel, basalt, sand, red sand, terracotta, dirt, coarse dirt, rooted dirt, mud, podzol) composed per biome. Custom identity is petrified wood (nonflammable, pickaxe-mineable mineralized trunks and processed builder variants) plus a Dalek architecture family (walls, riveted walls, floors, panels, grilles, glass, lights, doors or shutters, and damaged variants). Gallifrey plants and Gallifrey terrain do not generate here.
- Baseline caves and ordinary ores generate; Overworld farm animals do not. Passive recovery lives on the Thal plateau, not in the wastes.
- Radiation is local to the current Skaro biome and recomputed from that biome plus equipped protection. Thal Plateau is lowest; Irradiated Wastes and Drammankin Mire are highest. Leaving Skaro ends exposure immediately — there is no lingering hidden dose and no extra HUD. A complete protective suit prevents ambient Skaro radiation damage. The handheld meter reports a percentage in the action bar; console Panel1 and sonic scan show the same environmental reading for a linked Skaro exterior.
- Flora is sparse and biome-specific: petrified tree features (no saplings or leaves), hazardous Varga plants, mutated reeds on mire shores, low-light radiation fungus, ash scrub on the wastes, and Thal crops only in settlement farms.
- **Daleks** occupy hostile Skaro regions and military sites in small patrols; they do not spawn in Thal settlement safe zones or outside Skaro. **Thals** (`dwm:thal`) live only in plateau settlements (four cosmetic variants; they wander, look at players, and flee threats — no trades, quests, or wild biome spawns). **Slythers** (`dwm:slyther`) are solitary land predators on eligible dry Skaro biomes. **Lake mutants** (`dwm:lake_mutant`) stay in Drammankin Mire water and fare badly on land.
- Places: small war landmarks (trenches, wreckage, craters, checkpoints, petrified copses, mutation nests, mountain observation posts) with limited military loot; rare plateau settlements (huts, shelter, fields, watch post, connected paths); a repeatable Kaled bunker with a walkable surface entrance, connected underground rooms, encounters, and one vault; and **Kaalann**, a rare repeatable Dalek city with a walkable gate-to-command route — not a single unique coordinate.

## How It Works In-Game
1. At the console, cycle the planet locator until **Skaro** is selected.
2. Cycle the biome dial among Irradiated Wastes, Petrified Jungle, Drammankin Mire, Drammankin Mountains, and Thal Plateau.
3. Check Panel1 radiation (or sonic-scan the linked exterior). Suit up if the reading is high.
4. Pull the materialisation lever to dematerialise, then again in flight to land in Skaro.
5. On the surface, the handheld meter reports local radiation. A full suit stops ambient damage; leaving Skaro clears exposure.
6. Debug without a TARDIS: `/execute in dwm:skaro run tp @s ~ 128 ~`.

## Known Constraints
- Skaro is a TARDIS destination, not an alternate travel network or a campaign with bosses, quests, reputation, or a research GUI.
- Radiation is biome-and-equipment driven on the logical server. It does not persist after leaving, does not use a new HUD, and is separate from plant contact effects such as Varga.
- Terrain identity comes from composition, atmosphere, petrified vegetation, and Dalek architecture — not a full custom stone, sand, and dirt palette.
- Dalek architecture is a builder family reused by structures. Dalek ranks, saucers, raids, and city-conquest persistence are out of scope.
- Kaalann and the Kaled bunker are rare and repeatable. They are not globally unique sites and do not introduce new progression currencies.
- Thals have no professions, dialogue UI, or breeding. Their farms use the Skaro crop; they are not a villager economy.

## Future Opportunities
- Custom dimension effects for Skaro sky, fog, and clouds.
- Thal trades or professions if a later settlement loop needs them.
- Additional Dalek ranks, vehicles, or city-state mechanics beyond the baseline patrol and structure presence.
- Protective-suit degradation, medicines, or structure-scale radiation hotspots.
