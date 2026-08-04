# Feature: TARDIS Exterior Block

See also: [Docs Index](./index.md), [TARDIS Chameleon System](./feature-chameleon-system.md)

## Product Intent
Make the TARDIS a tangible world object that is expressive, interactive, and persistent per placement — including travel into a generated interior.

## Player Outcomes
- Place and keep a recognizable TARDIS identity in a world.
- Interact with the exterior to trigger visible door-state behavior.
- See a bigger-on-the-inside preview through open exterior doors.
- Walk into an open exterior door to enter a First Doctor–style console room in a dedicated dimension.
- See a smaller-on-the-outside preview through open interior doors.
- Leave through the interior doors to return outside the exterior TARDIS.

## Implemented Now
- Placeable `tardis_block` with block entity backing data.
- Persistent per-instance identity data (including UUID and variant metadata).
- Interactive door state transitions with sound feedback.
- Custom client rendering for TARDIS model presentation.
- Exterior BOTI (bigger on the inside): stencil-masked door aperture draws a synced console-room preview when `doorSwing >= 0.15`.
- Interior SOTO (smaller on the outside): stencil-masked interior door aperture draws a synced exterior world footprint when interior `doorSwing >= 0.15`.
- Shared empty dimension `dwm:tardis` hosting generated interiors.
- Lazy placement of `first_doctor_console_room` structure template on first entry.
- `TardisBlockEntity` stores `interiorEntrance` / `interiorGenerated`.
- Exterior return coordinates stored on `TardisDataModel` for exit teleports.
- Collision entry when the exterior door is open (`doorSwing >= 0.9`); exit via open interior doors.
- Config toggles `enableBoti` and `enableSoto` (default on) via Mod Menu / Cloth Config.
- Interior doors use an invisible block + dedicated BER (`TardisClassicInteriorDoorModel`) with swing animation.
- Materialisation lever travel: first pull dematerialises the exterior; after a short hold the TARDIS enters `IN_FLIGHT`; a second pull materialises at the selected dimension/biome landing site.
- First Doctor console Panel3 hosts a biome selector and planet locator (shared dial mesh); planet locator cycles loaded worlds except `dwm:tardis` and drives cross-dimension travel.
- First Doctor console time rotor bobbles vertically while the TARDIS is traveling (`DEMATERIALISING` / `IN_FLIGHT` / `MATERIALISING`) and rests when idle.
- Demat/mat/in-flight play loopable travel SFX (seamless loops) for code-configured phase lengths (`DEMATERIALISING_DURATION_TICKS` / `MATERIALISING_DURATION_TICKS` in `TardisTravelService`); shell vanishes mid-demat at `DEMATERIALISING_SHELL_REMOVE_AT_TICK`; `IN_FLIGHT` uses a higher-pitched demat/mat-derived loop in the interior; materialisation ends with a landing thud.

## How It Works In-Game
1. Place the TARDIS block.
2. Interact with the block to toggle door-state behavior (server-authoritative).
3. As the door opens, the console room becomes visible through the doorway (client preview).
4. When the door is fully open, walk into the exterior block to teleport to the interior entrance.
5. From inside, look through open interior doors to see the exterior world (SOTO preview).
6. Walk into the interior door blocks to return just outside the exterior TARDIS.
7. At the console: select a destination dimension (planet locator) and biome, pull the materialisation lever to dematerialise, wait for `IN_FLIGHT`, then pull again to materialise and land.

## BOTI Notes
- Visual illusion: does not stream the live `dwm:tardis` dimension to the exterior client.
- When the interior has been generated, the preview shows a synced BlockState + block-entity + entity snapshot of the 11×7×11 console-room footprint (near-live on interior edits; continuously refreshed while entities occupy the footprint). Until then (or if no snapshot yet), it falls back to `FirstDoctorConsoleRoomLayout` (blocks only).
- Format version 3 includes chunk-sync block-entity NBT and live entity samples; the client reconstructs synthetic BEs/entities and best-effort renders via `BlockEntityRenderDispatcher` / `EntityRenderDispatcher` (vanilla + mods). Entity poses are client-interpolated between sync samples (with local limb advance) so motion stays smooth at the exterior doorway. Players use a dedicated `OtherClientPlayerEntity` path because `EntityType.PLAYER` is not saveable. Interior doors remain excluded from BOTI (no dedicated interior-door BER in the exterior preview yet).
- Requires a stencil-capable framebuffer (mixin upgrades depth to depth+stencil). If stencil init/render fails, BOTI disables for the session and the exterior still renders.
- May not work with Fabulous graphics or some Sodium / shader setups; disable via config if needed.

## SOTO Notes
- Visual illusion: portal-composites a synced exterior footprint through the interior door aperture (not a live world stream); requires stencil + vanilla Fast/Fancy, disable via `enableSoto` if needed.

## Known Constraints
- Interior visuals use existing roundel/wall blocks; console props are simplified.
- Door open/closed for entry is server-authoritative; swing animation still updates locally on both sides.
- Shared exterior BOTI door aperture table per chameleon variant; interior SOTO uses one classic 3×2 opening aperture.
- Exterior SOTO footprint is axis-aligned (not rotated with exterior facing). Exit teleport and SOTO look-out follow the chameleon shell door facing (`TardisExteriorFacing`), which is opposite the raw `FACING_ROTATION` skull/banner south=0 convention because of shell BER transforms.

## Future Opportunities
- Richer First Doctor console props.
- Per-chameleon BOTI / SOTO aperture meshes.
- Ownership and multi-room corridors.
