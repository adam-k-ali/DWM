# Feature: TARDIS Chameleon System (Experimental)

See also: [Docs Index](./index.md), [TARDIS Exterior Block](./feature-tardis-block.md)

## Product Intent
Allow players to personalize TARDIS exterior identity through selectable visual variants while keeping the interaction flow simple.

## Player Outcomes
- Select a preferred TARDIS exterior style.
- Keep visual expression aligned with roleplay/build themes.
- Understand that this is an optional experimental path.
- Cycle the shell from the First Doctor console without opening the exterior GUI.

## Implemented Now
- Multiple chameleon variants mapped to different TARDIS exterior styles.
- **Basic console chameleon circuit** (Panel6): look-hit cycles `TardisChameleonVariant` with wrap-around; overlay names the new variant; a translucent shell hologram above the control mirrors the synced current variant. **Owner-only** (same gate as other piloting controls).
- Client GUI for selecting variants (sneak-use on exterior; config-gated). **Owner-only**; non-owners get the not-owner overlay and the GUI does not open.
- Networking payloads to update variant state server-side (owner-checked).
- Config gate for enabling/disabling chameleon GUI.

## How It Works In-Game
1. Enable the experimental chameleon setting in config for the exterior GUI path.
2. Sneak-use the TARDIS interaction path that opens the variant selector, or use the Panel6 chameleon circuit dial on the First Doctor console to cycle variants in-place.
3. Exterior GUI: pick a variant; client sends update payload; server applies new variant data.
4. Console cycle: server advances the variant, syncs it onto the console block entity, and the BER hologram updates.

## Known Constraints
- This feature is experimental; the exterior chameleon GUI remains disabled by default.
- Console basic cycle is owner-only on a linked First Doctor console (no advanced on/off toggle in this release); exterior GUI is also owner-only when enabled.
- Advanced chameleon circuit textures/toggle and environmental disguises are deferred (see DWM-032).
- Stability and UX polish are still evolving.
- Multiplayer behavior depends on correct payload registration and synced state.

## Future Opportunities
- Promote from experimental to stable after UX hardening.
- Add better in-game onboarding for first-time variant selection.
- Advanced chameleon toggle and disguise models.
