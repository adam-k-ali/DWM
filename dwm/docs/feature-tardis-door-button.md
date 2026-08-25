# Feature: TARDIS Door Button

See also: [Docs Index](./index.md), [TARDIS Exterior Block](./feature-tardis-block.md)

## Product Intent
Provide a compact, thematic interaction block that feels mechanically readable and architecturally useful near TARDIS builds.

## Player Outcomes
- Add a recognizable control element to TARDIS-themed doors/walls.
- Trigger short-lived state changes with immediate audio feedback.
- Integrate button behavior into decorative or redstone-adjacent builds.

## Implemented Now
- Custom `tardis_door_button` block with directional placement behavior.
- Pressed/unpressed state handling with timed reset.
- Click/interaction audio cues.
- Dedicated blockstate/model assets for active and inactive visuals.

## How It Works In-Game
1. Place the button on a supported face.
2. Interact to press it.
3. Button enters active state briefly, then returns to default state.

## Known Constraints
- Purpose is focused on concise interaction feedback, not complex control logic.
- Shape and orientation follow defined facing-based rules.

## Future Opportunities
- Add explicit links to nearby TARDIS state interactions.
- Expand redstone utility while keeping clear visual language.
