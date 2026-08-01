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

## How It Works In-Game
1. Place the TARDIS block.
2. Interact with the block to toggle door-state behavior (server-authoritative).
3. As the door opens, the console room becomes visible through the doorway (client preview).
4. When the door is fully open, walk into the exterior block to teleport to the interior entrance.
5. From inside, look through open interior doors to see the exterior world (SOTO preview).
6. Walk into the interior door blocks to return just outside the exterior TARDIS.

## BOTI Notes
- Visual illusion: does not stream the live `dwm:tardis` dimension to the exterior client.
- When the interior has been generated, the preview shows a synced BlockState + block-entity + entity snapshot of the 11×7×11 console-room footprint (near-live on interior edits; continuously refreshed while entities occupy the footprint). Until then (or if no snapshot yet), it falls back to `FirstDoctorConsoleRoomLayout` (blocks only).
- Format version 3 includes chunk-sync block-entity NBT and live entity samples; the client reconstructs synthetic BEs/entities and best-effort renders via `BlockEntityRenderDispatcher` / `EntityRenderDispatcher` (vanilla + mods). Entity poses are client-interpolated between sync samples (with local limb advance) so motion stays smooth at the exterior doorway. Players use a dedicated `OtherClientPlayerEntity` path because `EntityType.PLAYER` is not saveable. Interior doors remain excluded from BOTI (no dedicated interior-door BER in the exterior preview yet).
- Requires a stencil-capable framebuffer (mixin upgrades depth to depth+stencil). If stencil init/render fails, BOTI disables for the session and the exterior still renders.
- May not work with Fabulous graphics or some Sodium / shader setups; disable via config if needed.

## SOTO Notes
- Visual illusion: does not inject exterior chunks into the player's `dwm:tardis` dimension.
- **Snapshot path (blocks / BEs / shell / atmosphere):** syncs an 11×7×11 exterior footprint centered on the TARDIS block plus shell metadata (`variant`, `doorSwing`, `isOpen`, `exteriorRotation`) and atmosphere (`dimensionEffectsId`, `timeOfDay`, rain/thunder gradients, biome sky/fog colors). The exterior `tardis_block` is excluded from the block sample and drawn as a synthetic chameleon shell.
- **Phase 1 ghost stream (live entities):** while interior players track the door origin (or request `request_soto_ghost`), the server tickets a fixed **2-chunk** Chebyshev radius around the exterior (`STREAM_RADIUS_CHUNKS`), streams sparse chunk columns into a client `SotoGhostExterior` store, and pushes live entity spawn/update/remove packets (~10 Hz). Entity motion and walk cycles come from this ghost path, not snapshot lerp.
- Snapshot flush stays on a 3-tick cadence for terrain/shell dirtying; entity occupancy no longer forces a 1-tick full snapshot resample. Mob despawn counters are reset over the stream box (ticket-only keep-alive, no per-tick force-load of the whole cube).
- Client draws a mini skybox (overworld sun/moon/stars, End sky, or Nether fog backdrop) and short-range terrain fog inside the stencil mask before the footprint mesh so gaps do not show the interior dimension sky.
- Snapshots push to players tracking the interior door origin; clients may also request on cache miss (`request_soto_exterior` + `request_soto_ghost`).
- Shares the same stencil framebuffer support as BOTI; session disable covers both if stencil fails during either path.
- May not work with Fabulous graphics or some Sodium / shader setups; disable via `enableSoto` if needed.

## Known Constraints
- Interior visuals use existing roundel/wall blocks; console props are simplified.
- Door open/closed for entry is server-authoritative; swing animation still updates locally on both sides.
- Shared exterior BOTI door aperture table per chameleon variant; interior SOTO uses one classic 3×2 opening aperture.
- Exterior SOTO footprint is axis-aligned (not rotated with exterior facing).
- Phase 1 still draws **blocks** from the snapshot mesh path; ghost chunk data is stored for Phase 2 terrain meshes.

## Future Opportunities
- Richer First Doctor console props.
- Per-chameleon BOTI / SOTO aperture meshes.
- Ownership, multi-room corridors, and dematerialization travel.
- Phase 2: draw SOTO terrain from ghost-world chunk meshes; optional view-distance scaling beyond the fixed 2-chunk stream radius.
