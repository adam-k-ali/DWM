# Feature: Dalek

See also: [Docs Index](./index.md), [Skaro Dimension](./feature-skaro-dimension.md)

## Product Intent
Give survival and creative worlds a hostile Doctor Who enemy: a 1963 Dalek that glides, fires a laser, lifts off when the player is out of ground reach, and patrols Skaro in small groups. Rarer, larger Paradigm Daleks give the patrols a heavier, more imposing presence. Destroying a casing can release the Kaled mutant inside.

## Player Outcomes
- Spawn a Dalek from the **Dalek Spawn Egg**, or a Paradigm Dalek from the **Paradigm Dalek Spawn Egg** (Spawn Eggs tab), in any dimension.
- Occasionally run into a lone, towering Paradigm Dalek on Skaro, harder to put down than the 1963 casing.
- Encounter occasional small Dalek patrols while exploring Skaro.
- Be targeted and shot at by a cyan laser bolt from the gunstick. Nearby idle patrol members may join the same player target.
- Watch the Dalek take off when standing on a high ledge or otherwise out of ground pathing, then resume gliding when the player is reachable again.
- Sometimes see a Dalek mutant crawl out of a destroyed casing and lunge at whoever destroyed it. A **Dalek Mutant Spawn Egg** is also in the Spawn Eggs tab.

## Implemented Now
- Hostile `dwm:dalek` (`MONSTER`, 0.8 × 1.7 hitbox, a little shorter than the player, as in the show) with three variants on two classic casings. Spawn eggs and natural spawns pick one at random (1963 most common); `/summon dwm:dalek ~ ~ ~ {Variant:"time_war_bronze"}` forces one. Core combat, model, and sounds are owned by this feature (DWM-051).
  - **1963** (`models/entity/dalek.json`): the original Shawcraft casing. Silver with blue hemispheres, four rows of hemispheres all round a skirt that slopes back more steeply behind than in front, plain shoulders (no slats), silver neck rings over black mesh, a "toffee-apple" eyeball and small unlit dome lights.
  - **Bronze Time War** (`time_war_bronze`) and a darker, battle-scorched **Bronze Time War Dark** (`time_war_bronze_dark`) (`models/entity/time_war_dalek.json`): the 2005 casing. Bronze with gold hemispheres and collar, bronze shoulder slats over dark mesh, a ribbed gunstick, a two-step fender, and a blue lens. The lens and dome lights glow in the dark (full-bright overlay `time_war_glow.png`).
  - Both classic skins are 128×64 and painted per cube (shaded tiers, dome-shaded hemispheres, corner bevels), using the Silver and Bronze Dalekanium palettes.
- **Paradigm Daleks** (`dwm:paradigm_dalek`): a separate, larger casing (1.0 × 2.4 hitbox against 0.8 × 1.7, about 1.4× the classic height) with its own JSON mesh (`models/entity/paradigm_dalek.json`, 128×64 skins). Five castes on one chassis: **Drone** (red, most common), **Strategist** (blue), **Scientist** (orange), **Eternal** (yellow) and **Supreme** (white, rarest). Spawn eggs and natural spawns pick a caste at random; `/summon dwm:paradigm_dalek ~ ~ ~ {Variant:"paradigm_supreme"}` forces one. Tougher than the classic Daleks (50 health, 8 armour, full knockback resistance, 20-block laser range, slightly slower) with a higher gunstick and a deeper voice on the shared Dalek SFX. The eyestalk ends in a black socket with an orange eye; the eye and dome lights glow in the dark (full-bright overlay `paradigm_glow.png`, shared by every caste and drawn by the same `DalekGlowLayer` as the Time War glow). Same flight, laser and target sharing as the classic Daleks; all casings share targets and never hit each other.
- **Dalek mutant** (`dwm:dalek_mutant`, `MONSTER`): the Kaled creature inside the casing. Small (0.7 × 0.7), slow (0.2 speed), 10 health and 3 melee damage. It hunts players on sight, leaps at them and lashes with its tentacles; it retaliates when hurt. Its JSON mesh (`models/entity/dalek_mutant.json`, 64×64 skin `dalek_mutant.png`) has a fleshy dome with a brain on top, one eye that follows its gaze, and six tentacles that wave idly and swing as it crawls. Sounds are DWM events aliased to vanilla squid and small-slime sounds. No natural spawns: when any Dalek (1963, Time War or Paradigm) dies, there is a 25% chance (`DalekMutantEmergence.EMERGE_CHANCE`) that a mutant crawls out as the casing disappears, aimed at the survival player who destroyed it. Only the end of the death animation counts: Daleks that despawn, unload or are discarded (even mid-animation) never release one. Dalek lasers pass through mutants. Empty loot table.
- Each variant is painted for one casing mesh (`DalekChassis`: 1963, Time War, Paradigm), and each entity type spawns from one family (`DalekFamily`: classic or Paradigm). The variant picks the mesh and the gunstick muzzle; a `dwm:dalek` given a Paradigm `Variant` (or the reverse) falls back to its family default rather than drawing the wrong skin.
- Ground pathfinding by default; flying navigation + no-gravity when the target is more than 2.5 blocks above or has no ground path beyond 4 blocks.
- Ranged laser projectile `dwm:dalek_laser` spawned from the gunstick (~2 second cadence, 16-block range, 4 damage).
- Spawn egg, empty loot table, and a robotic voice. Every Dalek (all casings; Paradigm is just pitched lower) shouts "Exterminate!" (`entity.dalek.exterminate`, three takes) when it first locks onto a target, at most once per 10 seconds per Dalek (`DalekVoice`). Idle chatter (`ambient`, four lines such as "Seek, locate, destroy"), pain cries (`hurt`, short "Emergency!" / "Damage!" lines) and a winding-down death line (`death`, "My vision is impaired!") are spoken the same way. The laser (`shoot`) stays a wordless zap, since it fires every couple of seconds.
- **Voice provenance.** The speech is plain text-to-speech from [CMU Flite](https://github.com/festvox/flite) (voice `kal16`) flattened to a monotone and run through a Dalek effects chain (band-limit, ~30 Hz ring modulation, soft clipping, a short metallic comb), generated by `dwm/tools/sfx` (`generate-dalek-sfx`) and committed as OGGs. No BBC audio is sampled. Flite's licence (a CMU BSD-style licence, free for commercial use) requires retaining its copyright notice and authors' names, marking modifications and not using the names to endorse; the installed package states no separate per-voice licence, so the voice data fall under that collection licence. This is a record, not legal advice. macOS `say` voices were ruled out because Apple's licence limits them to personal, non-commercial use and forbids redistributing recordings.
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
- Dalek outposts (checkpoints) generate on Skaro without embedded Daleks (see [Skaro landmarks](./feature-skaro-dimension.md#landmarks)); outpost, bunker, and Kaalann population is not shipped yet.
- Variants and castes are visual only; Paradigm castes do not have rank-specific behaviour (e.g. a Supreme commanding Drones) yet.
- Does not target Time Lords in this pass. Daleks hunt Thals (`dalek.json` targets `dwm:thal`); unarmed Thals flee on sight (`#dwm:thal_fears`) and armed Thals shoot back — see [Thal](./feature-thal.md) and [Dalek/Thal Battles](./feature-dalek-thal-battles.md).
- No unique drops yet.
- The Dalek mutant has no natural spawns and does not climb back into a casing.

## Future Opportunities (Planned)
- More era variants (skins on an existing chassis, or a new `DalekChassis` with its own mesh in an existing family).
- Caste-specific Paradigm behaviour (Supreme leadership, Scientist/Strategist roles).
- Structure-scoped patrols at checkpoints, the Kaled bunker, and Kaalann.
- Unique loot / advancement hooks.

See also the player-usable [Dalek Laser Gun](./feature-dalek-laser-gun.md).
