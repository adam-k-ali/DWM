# Feature: TARDIS Exterior Block

See also: [Docs Index](./index.md), [TARDIS Chameleon System](./feature-chameleon-system.md)

## Product Intent
Make the TARDIS a tangible world object that is expressive, interactive, and persistent per placement — including travel into a generated interior.

## Player Outcomes
- Place and keep a recognizable TARDIS identity in a world.
- Interact with the exterior to trigger visible door-state behavior.
- See a bigger-on-the-inside preview through open exterior doors.
- Walk into an open exterior door to enter a First Doctor–style console room in a dedicated dimension.
- Leave through the interior doors to return outside the exterior TARDIS.

## Implemented Now
- Placeable `tardis_block` with block entity backing data.
- Persistent per-instance identity data (including UUID and variant metadata).
- Interactive door state transitions with sound feedback.
- Custom client rendering for TARDIS model presentation.
- Exterior-only BOTI (bigger on the inside): stencil-masked door aperture draws a baked First Doctor console-room mesh when `doorSwing >= 0.15`.
- Shared empty dimension `dwm:tardis` hosting generated interiors.
- Lazy placement of `first_doctor_console_room` structure template on first entry.
- `TardisBlockEntity` stores `interiorEntrance` / `interiorGenerated`.
- Exterior return coordinates stored on `TardisDataModel` for exit teleports.
- Collision entry when the exterior door is open (`doorSwing >= 0.9`); exit via open interior doors.
- Config toggle `enableBoti` (default on) via Mod Menu / Cloth Config.

## How It Works In-Game
1. Place the TARDIS block.
2. Interact with the block to toggle door-state behavior (server-authoritative).
3. As the door opens, the console room becomes visible through the doorway (client preview).
4. When the door is fully open, walk into the exterior block to teleport to the interior entrance.
5. Walk into the interior door blocks to return just outside the exterior TARDIS.

## BOTI Notes
- Visual illusion: does not stream the live `dwm:tardis` dimension to the exterior client.
- When the interior has been generated, the preview shows a synced BlockState snapshot of the 11×7×11 console-room footprint (near-live on interior edits). Until then (or if no snapshot yet), it falls back to `FirstDoctorConsoleRoomLayout`.
- Block-entity / entity visuals inside BOTI are not included yet (payload format is reserved for a later version).
- Requires a stencil-capable framebuffer (mixin upgrades depth to depth+stencil). If stencil init/render fails, BOTI disables for the session and the exterior still renders.
- May not work with Fabulous graphics or some Sodium / shader setups; disable via config if needed.
- Interior → exterior “smaller on the outside” (SOTO) is not implemented yet.

## Known Constraints
- Interior visuals use existing roundel/wall blocks; console props are simplified.
- Door open/closed for entry is server-authoritative; swing animation still updates locally on both sides.
- Interior door client rendering is a simple block model (not a dedicated BER).
- Shared door aperture for all chameleon variants (not per-model yet).

## Future Opportunities
- Richer First Doctor console props and animated interior doors.
- Looking out from open interior doors (SOTO).
- Per-chameleon BOTI aperture meshes.
- Ownership, multi-room corridors, and dematerialization travel.
