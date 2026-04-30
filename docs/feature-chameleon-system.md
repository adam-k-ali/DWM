# Feature: TARDIS Chameleon System (Experimental)

See also: [Docs Index](./index.md), [TARDIS Exterior Block](./feature-tardis-block.md)

## Product Intent
Allow players to personalize TARDIS exterior identity through selectable visual variants while keeping the interaction flow simple.

## Player Outcomes
- Select a preferred TARDIS exterior style.
- Keep visual expression aligned with roleplay/build themes.
- Understand that this is an optional experimental path.

## Implemented Now
- Multiple chameleon variants mapped to different TARDIS exterior styles.
- Client GUI for selecting variants.
- Networking payloads to update variant state server-side.
- Config gate for enabling/disabling chameleon GUI.

## How It Works In-Game
1. Enable the experimental chameleon setting in config.
2. Sneak-use the TARDIS interaction path that opens the variant selector.
3. Pick a variant; client sends update payload; server applies new variant data.

## Known Constraints
- This feature is experimental and disabled by default.
- Stability and UX polish are still evolving.
- Multiplayer behavior depends on correct payload registration and synced state.

## Future Opportunities
- Promote from experimental to stable after UX hardening.
- Add better in-game onboarding for first-time variant selection.
