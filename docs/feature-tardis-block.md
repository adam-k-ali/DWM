# Feature: TARDIS Exterior Block

See also: [Docs Index](./index.md), [TARDIS Chameleon System](./feature-chameleon-system.md)

## Product Intent
Make the TARDIS a tangible world object that is expressive, interactive, and persistent per placement.

## Player Outcomes
- Place and keep a recognizable TARDIS identity in a world.
- Interact with the exterior to trigger visible door-state behavior.
- Use the TARDIS as a themed anchor for builds and roleplay spaces.

## Implemented Now
- Placeable `tardis_block` with block entity backing data.
- Persistent per-instance identity data (including UUID and variant metadata).
- Interactive door state transitions with sound feedback.
- Custom client rendering for TARDIS model presentation.

## How It Works In-Game
1. Place the TARDIS block.
2. Interact with the block to toggle door-state behavior.
3. Door swing/state persists through block entity data and render updates.

## Known Constraints
- This is currently an exterior interaction feature, not full interior travel gameplay.
- Visual fidelity depends on available client-side model/texture assets.

## Future Opportunities
- Add clearer in-game affordances for TARDIS state.
- Extend interactions into deeper progression systems around ownership and travel.
