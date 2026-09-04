# Feature: Dalek

See also: [Docs Index](./index.md)

## Product Intent
Give survival and creative worlds a hostile Doctor Who enemy that can be spawned on demand: a 1963 Dalek that glides, fires a laser, and lifts off when the player is out of ground reach.

## Player Outcomes
- Spawn a Dalek from the **Dalek Spawn Egg** (Spawn Eggs tab).
- Be targeted and shot at by a cyan laser bolt from the gunstick.
- Watch the Dalek take off when standing on a high ledge or otherwise out of ground pathing, then resume gliding when the player is reachable again.

## Implemented Now
- Hostile `dwm:dalek` (`MONSTER`) with a single **1963** variant (silver / black / cyan atlas).
- Ground pathfinding by default; flying navigation + no-gravity when the target is more than 2.5 blocks above or has no ground path beyond 4 blocks.
- Ranged laser projectile `dwm:dalek_laser` spawned from the gunstick (~2 second cadence, 16-block range, 4 damage).
- Spawn egg, empty loot table, original mechanical SFX (ambient / hurt / death / shoot). No BBC voice.
- Fall-damage immune while flying.
- Flying shows underside exhaust smoke, a whole-chassis hover bob, a takeoff particle burst, and a slight lean into velocity when moving fast.

## How It Works In-Game
1. Use a Dalek Spawn Egg (creative inventory, Spawn Eggs tab).
2. Survival players in line of sight are targeted; the Dalek stops and fires.
3. Climbing out of ground reach causes the Dalek to fly up and continue shooting.

## Known Constraints
- Spawn-egg only. There is no Skaro dimension, and Daleks do not naturally spawn on Gallifrey.
- One visual variant; later casings can reuse `DalekVariant`.
- Does not target Time Lords in this pass.
- No unique drops yet.

## Future Opportunities (Planned)
- Additional era variants (skins on the same chassis).
- Natural spawn on a future Skaro dimension.
- Unique loot / advancement hooks.
