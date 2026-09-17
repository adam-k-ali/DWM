# Feature: Dalek

See also: [Docs Index](./index.md), [Skaro Dimension](./feature-skaro-dimension.md)

## Product Intent
Give survival and creative worlds a hostile Doctor Who enemy: a 1963 Dalek that glides, fires a laser, lifts off when the player is out of ground reach, and patrols Skaro in small groups.

## Player Outcomes
- Spawn a Dalek from the **Dalek Spawn Egg** (Spawn Eggs tab) in any dimension.
- Encounter occasional small Dalek patrols while exploring Skaro.
- Be targeted and shot at by a cyan laser bolt from the gunstick. Nearby idle patrol members may join the same player target.
- Watch the Dalek take off when standing on a high ledge or otherwise out of ground pathing, then resume gliding when the player is reachable again.

## Implemented Now
- Hostile `dwm:dalek` (`MONSTER`) with a single **1963** variant (silver / black / cyan atlas). Core combat, model, and sounds are owned by this feature (DWM-051).
- Ground pathfinding by default; flying navigation + no-gravity when the target is more than 2.5 blocks above or has no ground path beyond 4 blocks.
- Ranged laser projectile `dwm:dalek_laser` spawned from the gunstick (~2 second cadence, 16-block range, 4 damage).
- Spawn egg, empty loot table, original mechanical SFX (ambient / hurt / death / shoot). No BBC voice.
- Fall-damage immune while flying.
- Flying shows underside exhaust smoke, a whole-chassis hover bob, a takeoff particle burst, and a slight lean into velocity when moving fast.
- **Skaro patrols (DWM-070 biome slice):** natural `MONSTER` spawn table entries on all five Skaro biomes. Groups are 1–3 in petrified jungle, Drammankin mire, and Drammankin mountains, and 1–2 with lower weight in irradiated wastes and the Thal plateau. Vanilla monster caps are unchanged. Spawn eggs and commands still work outside Skaro; natural/chunk generation does not.
- Idle Daleks may copy a nearby ally's living player target within 16 blocks in the same dimension. Targeting and firing stay server-authoritative with vanilla entity tracking.

## How It Works In-Game
1. Travel to Skaro, or use a Dalek Spawn Egg (creative inventory, Spawn Eggs tab).
2. Survival players in line of sight are targeted; the Dalek stops and fires. Nearby idle patrol members may acquire the same player.
3. Climbing out of ground reach causes the Dalek to fly up and continue shooting.

## Known Constraints
- Daleks do not naturally spawn on Gallifrey or other non-Skaro worlds.
- Checkpoint, bunker, and Kaalann structure population is not shipped yet (DWM-069).
- One visual variant; later casings can reuse `DalekVariant`.
- Does not target Time Lords or Thals in this pass.
- No unique drops yet.

## Future Opportunities (Planned)
- Additional era variants (skins on the same chassis).
- Structure-scoped patrols at checkpoints, the Kaled bunker, and Kaalann.
- Unique loot / advancement hooks.
