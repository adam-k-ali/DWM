# Feature: Dalek/Thal Battles

See also: [Docs Index](./index.md), [Thal](./feature-thal.md), [Dalek](./feature-dalek.md), [Dalek Laser Gun](./feature-dalek-laser-gun.md)

## Product Intent
Give Skaro's Thal settlements occasional, self-contained skirmishes: a Dalek squad attacks and the Thals fight back with Dalek Laser Guns.

## Player Outcomes
- Near a Thal settlement on Skaro a skirmish may start at random (checked every 10 seconds, 10% chance, 10-minute cooldown after one ends, one at a time).
- Resident Thals within 48 blocks arm themselves, 2-3 armed reinforcement Thals appear 6-12 blocks from the player, and 2-4 Daleks (Daleks and Paradigm Daleks) arrive from 40-60 blocks away. Nearby players get an action-bar message.
- The skirmish ends when one side is wiped out or after 3 minutes. Remaining Daleks and reinforcement Thals are removed, resident Thals are disarmed.

## Commands
- `/dwm battle start` — start a skirmish at your position (op level 2, works in any dimension).
- `/dwm battle stop` — end every running skirmish.

## Known Constraints
- State is in memory only; a restart forgets the battle (leftover persistent Daleks/Thals are not cleaned up).
- No persistent war state, loot or reputation. Possible follow-up: `SavedData`-backed war progress.

## Testing
- Unit: `BattleEventLogicTest` (trigger gating, squad sizing, spawn ring, outcomes).
- GameTests: `DalekThalBattleGameTests` (armed Thal bolt damages a Dalek but not a Thal; start spawns both squads and arms Thals; stop cleans up).
