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
- Interior SOTO (smaller on the outside): stencil-masked interior door aperture draws a synced exterior world sample (view-distance capped, visibility-culled) when interior `doorSwing >= 0.15`.
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
- Visual illusion: does not stream the live exterior dimension to the interior client.
- Syncs a per-player exterior sample sized from the client's vanilla view distance (capped at 8 chunks): a Chebyshev cube centered on the TARDIS block, visibility-culled by flood-fill from the exterior door (blocks + BE NBT + entities in the visible volume) plus shell metadata (`variant`, `doorSwing`, `isOpen`, `exteriorRotation`). The exterior `tardis_block` is excluded from the block sample and drawn as a synthetic chameleon shell.
- Client bakes terrain block models into a static GPU mesh when a snapshot arrives and redraws that mesh each frame (block entities / entities still use their dispatchers).
- Relative block coords are TARDIS-centered (signed); format version 4. Clients send `viewDistanceChunks` on `request_soto_exterior` and re-request when render distance changes.
- Snapshots push to players tracking the interior door origin (each player gets a sample at their last known / default view distance).
- While interior trackers are present, exterior chunks stay ticketed at the viewer's RD (no per-tick force-load). Mob despawn is reset in that radius for empty exterior worlds (singleplayer-safe). Entity sample refreshes run on a slow cadence without re-flooding terrain; full samples run on block/shell dirty.
- Flood-fill visits are hard-capped so open sky cannot expand to the full RD volume in one sample.
- Shares the same stencil framebuffer support as BOTI; session disable covers both if stencil fails during either path.
- May not work with Fabulous graphics or some Sodium / shader setups; disable via `enableSoto` if needed.

## Known Constraints
- Interior visuals use existing roundel/wall blocks; console props are simplified.
- Door open/closed for entry is server-authoritative; swing animation still updates locally on both sides.
- Shared exterior BOTI door aperture table per chameleon variant; interior SOTO uses one classic 3×2 opening aperture.
- Exterior SOTO sample cube is axis-aligned; door-seeded visibility uses exterior facing. Hard cap of 8 chunks regardless of higher client view distance.

## Future Opportunities
- Richer First Doctor console props.
- Per-chameleon BOTI / SOTO aperture meshes.
- Ownership, multi-room corridors, and dematerialization travel.
