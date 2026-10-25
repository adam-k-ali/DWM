# Feature: Dalek

See also: [Docs Index](./index.md), [Skaro Dimension](./feature-skaro-dimension.md)

## Product Intent
Give survival and creative worlds a hostile Doctor Who enemy: a 1963 Dalek that glides, fires a laser, lifts off when the player is out of ground reach, and patrols Skaro in small groups. Rarer, larger Paradigm Daleks give the patrols a heavier, more imposing presence.

## Player Outcomes
- Spawn a Dalek from the **Dalek Spawn Egg**, or a Paradigm Dalek from the **Paradigm Dalek Spawn Egg** (Spawn Eggs tab), in any dimension.
- Occasionally run into a lone, towering Paradigm Dalek on Skaro, harder to put down than the 1963 casing.
- Encounter occasional small Dalek patrols while exploring Skaro.
- Be targeted and shot at by a cyan laser bolt from the gunstick. Nearby idle patrol members may join the same player target.
- Watch the Dalek take off when standing on a high ledge or otherwise out of ground pathing, then resume gliding when the player is reachable again.

## Implemented Now
- Hostile `dwm:dalek` (`MONSTER`) with three variants on one chassis: **1963** (silver / black / cyan), **Bronze Time War** (`time_war_bronze`) and a darker weathered **Bronze Time War Dark** (`time_war_bronze_dark`). Spawn eggs and natural spawns pick one at random (1963 most common); `/summon dwm:dalek ~ ~ ~ {Variant:"time_war_bronze"}` forces one. Core combat, model, and sounds are owned by this feature (DWM-051).
- **Paradigm Daleks** (`dwm:paradigm_dalek`): a separate, larger casing (1.0 × 2.4 hitbox against 0.8 × 1.95) with its own JSON mesh (`models/entity/paradigm_dalek.json`, 128×64 skins). Five castes on one chassis: **Drone** (red, most common), **Strategist** (blue), **Scientist** (orange), **Eternal** (yellow) and **Supreme** (white, rarest). Spawn eggs and natural spawns pick a caste at random; `/summon dwm:paradigm_dalek ~ ~ ~ {Variant:"paradigm_supreme"}` forces one. Tougher than the 1963 Dalek (50 health, 8 armour, full knockback resistance, 20-block laser range, slightly slower) with a higher gunstick and a deeper voice on the shared Dalek SFX. The eyestalk ends in a black socket with an orange eye; the eye and dome lights glow in the dark (full-bright overlay `paradigm_glow.png`, shared by every caste). Same flight, laser and target sharing as the 1963 Dalek; both casings share targets and never hit each other.
- Variants belong to a chassis (`DalekChassis`): a 1963 Dalek given a Paradigm `Variant` (or the reverse) falls back to its chassis default rather than drawing the wrong skin on its mesh.
- Ground pathfinding by default; flying navigation + no-gravity when the target is more than 2.5 blocks above or has no ground path beyond 4 blocks.
- Ranged laser projectile `dwm:dalek_laser` spawned from the gunstick (~2 second cadence, 16-block range, 4 damage).
- Spawn egg, empty loot table, original mechanical SFX (ambient / hurt / death / shoot). No BBC voice.
- Fall-damage immune while flying.
- Flying shows underside exhaust smoke, a whole-chassis hover bob, a takeoff particle burst, and a slight lean into velocity when moving fast.
- **Skaro patrols (DWM-070 biome slice):** natural `MONSTER` spawn table entries on all five Skaro biomes. Groups are 1–3 in petrified jungle, Drammankin mire, and Drammankin mountains, and 1–2 with lower weight in irradiated wastes and the Thal plateau. Paradigm Daleks spawn alone with a much lower weight (8 against 100, or 3 against 25 in the sparse biomes) and a higher spawn charge. Vanilla monster caps are unchanged. Spawn eggs and commands still work outside Skaro; natural/chunk generation does not.
- Idle Daleks may copy a nearby ally's living player target within 16 blocks in the same dimension. Targeting and firing stay server-authoritative with vanilla entity tracking.

## How It Works In-Game
1. Travel to Skaro, or use a Dalek Spawn Egg (creative inventory, Spawn Eggs tab).
2. Survival players in line of sight are targeted; the Dalek stops and fires. Nearby idle patrol members may acquire the same player.
3. Climbing out of ground reach causes the Dalek to fly up and continue shooting.

## Known Constraints
- Daleks do not naturally spawn on Gallifrey or other non-Skaro worlds.
- Checkpoint, bunker, and Kaalann structure population is not shipped yet (DWM-069).
- Variants and castes are visual only; Paradigm castes do not have rank-specific behaviour (e.g. a Supreme commanding Drones) yet.
- Does not target Time Lords or Thals in this pass.
- No unique drops yet.

## Future Opportunities (Planned)
- More era variants (skins on an existing chassis, or a new `DalekChassis` with its own mesh).
- Caste-specific Paradigm behaviour (Supreme leadership, Scientist/Strategist roles).
- Structure-scoped patrols at checkpoints, the Kaled bunker, and Kaalann.
- Unique loot / advancement hooks.
