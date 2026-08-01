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
- Visual illusion: does not stream the live exterior dimension to the interior client as a full world.
- **Phase 0 snapshot:** syncs an 11×7×11 exterior footprint centered on the TARDIS block (blocks + BE NBT + entities) plus shell metadata (`variant`, `doorSwing`, `isOpen`, `exteriorRotation`) and atmosphere. The exterior `tardis_block` is excluded from the block sample and drawn as a synthetic chameleon shell via `SotoShellModels`. Snapshots push to players tracking the interior door origin; clients may also request on cache miss (`request_soto_exterior`).
- **Phase 1 ghost stream:** streams nearby exterior chunk columns + live entities into `SotoGhostExterior` (footprint-relative coords). Ghost entities are drawn preferentially over snapshot synthetics.
- **Phase 2 mesh draw:** on chunk apply, client bakes per-chunk GPU meshes (`SotoGhostMeshCache`) and draws them through the existing stencil aperture when ghost chunks and meshes are present; otherwise falls back to snapshot `renderBlockAsEntity`. Ghost block entities render from streamed NBT when on the mesh path.
- Preview hitch sits one block in front of the exterior shell door face (`PREVIEW_FORWARD_OFFSET`) so the look-out clears the chameleon body rather than starting at the shell center.
- Lookout stable view (`applyLookoutStableView`) freezes the exterior eye at that hitch looking outward at a fixed view depth (`LOOKOUT_VIEW_DEPTH`) so walking/strafing does not dolly the preview; the synthetic shell is not drawn on the SOTO path.
- Shares the same stencil framebuffer support as BOTI; session disable covers both if stencil fails during either path.
- May not work with Fabulous graphics or some Sodium / shader setups; disable via `enableSoto` if needed.

## Known Constraints
- Interior visuals use existing roundel/wall blocks; console props are simplified.
- Door open/closed for entry is server-authoritative; swing animation still updates locally on both sides.
- Shared exterior BOTI door aperture table per chameleon variant; interior SOTO uses one classic 3×2 opening aperture.
- Exterior SOTO footprint is axis-aligned (not rotated with exterior facing). Exit teleport and SOTO look-out follow the chameleon shell door facing (`TardisExteriorFacing`), which is opposite the raw `FACING_ROTATION` skull/banner south=0 convention because of shell BER transforms.

## Future Opportunities
- Richer First Doctor console props.
- Per-chameleon BOTI / SOTO aperture meshes.
- Ownership, multi-room corridors, and dematerialization travel.
