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
- Spawn egg, original mechanical SFX (ambient / hurt / death / shoot). No BBC voice.
- Death loot: **1–2 `dalekanium_ingot`** (+ Looting). Players alloy those ingots with iron (silver) or copper (bronze) for Dalekanium architecture (see [Skaro Dimension](./feature-skaro-dimension.md)).
- Fall-damage immune while flying.
- Flying shows underside exhaust smoke, a whole-chassis hover bob, a takeoff particle burst, and a slight lean into velocity when moving fast.

## How It Works In-Game
1. Use a Dalek Spawn Egg (creative inventory, Spawn Eggs tab), or encounter Daleks once Skaro population ships.
2. Survival players in line of sight are targeted; the Dalek stops and fires.
3. Climbing out of ground reach causes the Dalek to fly up and continue shooting.
4. Defeated Daleks drop Dalekanium ingots for crafting.

## Known Constraints
- Spawn-egg only for natural presence today; Skaro population is owned by later tickets.
- One visual variant; later casings can reuse `DalekVariant` (silver vs bronze alloys already exist as builder materials).
- Does not target Time Lords in this pass.

## Future Opportunities (Planned)
- Additional era variants (skins on the same chassis).
- Natural spawn on Skaro (DWM-070).
- Advancement hooks tied to Dalek encounters.
