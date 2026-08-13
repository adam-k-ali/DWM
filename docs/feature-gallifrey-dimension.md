# Feature: Gallifrey Dimension

See also: [Docs Index](./index.md), [Gallifrey Building](./feature-gallifrey-building.md), [TARDIS Exterior Block](./feature-tardis-block.md)

## Product Intent
Give TARDIS travel a unique destination world built from Gallifrey builder blocks—burnt-orange skies over deep-red grass plains, forests of Ash woods, sandy wastes, and orange-sand badlands.

## Player Outcomes
- Reach Gallifrey via the First Doctor console planet locator (or debug `/execute in dwm:gallifrey run tp @s ~ 128 ~`).
- Cycle Gallifrey biomes on the biome dial before materialising.
- Explore terrain surfaced with Gallifrey grass, dirt, sand, orange sand, and stone, with Ash / Dark Ash / Cardinal trees in forested biomes and Gallifrey decorative plants in the wild.

## Implemented Now
- Dimension `dwm:gallifrey` (noise overworld-style generator, overworld-like dimension type).
- Biomes: `dwm:gallifrey_plains`, `dwm:gallifrey_forest`, `dwm:gallifrey_wastes`, `dwm:gallifrey_badlands` (tag `#dwm:is_gallifrey`).
- Surface rules use Gallifrey grass / dirt / coarse dirt / sand / sandstone / orange sand / orange sandstone / stone (wastes stay Gallifrey-sand; badlands use orange sand).
- Placed tree features for Ash (sparse plains + denser forest), Dark Ash, and Cardinal in forest.
- Plant decoration: Flower of Remembrance / Moonlight Bloom mix on plains (denser) and forest (sparser); Saccharine Cane columns on wastes and badlands (no water requirement).
- Azbantium ore veins in Gallifrey stone (all biomes; diamond pickaxe required — see [Azbantium](./feature-azbantium.md)).
- Gallifrey-textured coal, iron, gold, and diamond ores in Gallifrey stone (all biomes; drop vanilla items; Overworld-like vein distribution).
- Jigsaw villages (`dwm:gallifrey_village`) in plains and forest: vanilla plains layouts retextured to Ash wood and Gallifrey stone via pool aliases (no copied NBT). Wastes/badlands have no villages yet.
- Archive colormap and cloud textures imported under `assets/dwm/textures/` for future tinting/sky work; grass block uses pre-colored deep-red textures.
- TARDIS `BiomeSelectorLogic` maps `dwm:gallifrey` to `#dwm:is_gallifrey`.

## How It Works In-Game
1. At the console, cycle the planet locator until **Gallifrey** is selected.
2. Cycle the biome dial among plains / forest / wastes / badlands.
3. Pull the materialisation lever to dematerialise, then again in flight to land in Gallifrey.
4. Debug without a TARDIS: `/execute in dwm:gallifrey run tp @s ~ 128 ~`.
5. Find a village: `/execute in dwm:gallifrey run locate structure dwm:gallifrey_village` (plains/forest only).

## Known Constraints
- Cloud texture is imported but not wired through custom dimension effects; sky/fog use biome colors and overworld dimension effects.
- Gallifrey mobs are follow-on tickets (DWM-041–042). After datagen/build, smoke villages with `/execute in dwm:gallifrey run locate structure dwm:gallifrey_village`.

## Future Opportunities (Planned)
- Wire colormaps for tinted grass/foliage if decorative plants need biome tinting.
- Custom dimension effects for Gallifrey clouds/sky.
- Desert-style villages for wastes/badlands.
- Remaining Gallifrey fauna.
