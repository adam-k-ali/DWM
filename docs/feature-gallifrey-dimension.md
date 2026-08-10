# Feature: Gallifrey Dimension

See also: [Docs Index](./index.md), [Gallifrey Building](./feature-gallifrey-building.md), [TARDIS Exterior Block](./feature-tardis-block.md)

## Product Intent
Give TARDIS travel a unique destination world built from Gallifrey builder blocks—orange-tinted plains, forests of Ash woods, and sandy wastes—without waiting on grass or plant content.

## Player Outcomes
- Reach Gallifrey via the First Doctor console planet locator (or debug `/execute in dwm:gallifrey run tp @s ~ 128 ~`).
- Cycle Gallifrey biomes on the biome dial before materialising.
- Explore terrain surfaced with Gallifrey dirt, sand, and stone, with Ash / Dark Ash / Cardinal trees in forested biomes.

## Implemented Now
- Dimension `dwm:gallifrey` (noise overworld-style generator, overworld-like dimension type).
- Biomes: `dwm:gallifrey_plains`, `dwm:gallifrey_forest`, `dwm:gallifrey_wastes` (tag `#dwm:is_gallifrey`).
- Surface rules use Gallifrey dirt / coarse dirt / sand / sandstone / stone (no grass block yet).
- Placed tree features for Ash (sparse plains + denser forest), Dark Ash, and Cardinal in forest.
- Archive colormap and cloud textures imported under `assets/dwm/textures/` for future grass/sky work; look-and-feel today comes from biome effect colors.
- TARDIS `BiomeSelectorLogic` maps `dwm:gallifrey` to `#dwm:is_gallifrey`.

## How It Works In-Game
1. At the console, cycle the planet locator until **Gallifrey** is selected.
2. Cycle the biome dial among plains / forest / wastes.
3. Pull the materialisation lever to dematerialise, then again in flight to land in Gallifrey.
4. Debug without a TARDIS: `/execute in dwm:gallifrey run tp @s ~ 128 ~`.

## Known Constraints
- No Gallifrey grass block or decorative plants yet (see DWM-017 / grass deferred note on the building feature).
- Cloud texture is imported but not wired through custom dimension effects; sky/fog use biome colors and overworld dimension effects.
- Villages and Gallifrey mobs are follow-on tickets (DWM-041–043).

## Future Opportunities (Planned)
- Grass block when archive top texture exists; wire colormaps for grass/foliage tinting.
- Custom dimension effects for Gallifrey clouds/sky.
- Plants, orange sand variants, villages, and fauna.
