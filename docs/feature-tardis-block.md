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
- Exterior BOTI (bigger on the inside): deferred portal FBO composites a hitch-fixed console-room look-in through the chameleon door aperture when `doorSwing >= 0.15`.
- Interior SOTO (smaller on the outside): deferred portal FBO composites a hitch-fixed exterior look-out through the classic interior door aperture when interior `doorSwing >= 0.15`.
- Shared empty dimension `dwm:tardis` hosting generated interiors.
- Lazy placement of `first_doctor_console_room` structure template on first entry.
- `TardisBlockEntity` stores `interiorEntrance` / `interiorGenerated`.
- Exterior return coordinates stored on `TardisDataModel` for exit teleports.
- Collision entry when the exterior door is open (`doorSwing >= 0.9`); exit via open interior doors.
- Landing search requires replaceable space in the door-facing column (feet + head), not only the shell cell.
- Single config toggle `enableDoorPortals` (default on) via Mod Menu / Cloth Config; legacy `enableBoti` / `enableSoto` migrate on load.
- Interior doors use an invisible block + dedicated BER (`TardisClassicInteriorDoorModel`) with swing animation.
- Materialisation lever travel: first pull dematerialises the exterior; after a short hold the TARDIS enters `IN_FLIGHT`; a second pull materialises at the destination resolved from the active `DestinationMode`.
- Destination modes: `BIOME` (default — selected dimension/biome landing search), `WAYPOINT` (exact saved exterior coords), `PLAYER` (live online player position at materialise; fails with overlay if offline), `FAST_RETURN` (exact historically visited exterior from LIFO history).
- First Doctor console Panel3 hosts four dials: biome selector, waypoint selector, player locator, and planet locator (shared dial mesh). Planet locator cycles loaded worlds except `dwm:tardis` (including `dwm:gallifrey`). Waypoint/player dials open GUIs (no `MenuType`) to save/delete/select waypoints (cap 16; current = linked exterior) or select another online player; cycling biome/planet resets mode to `BIOME`.
- First Doctor console Panel6 hosts the materialisation lever, a basic chameleon circuit dial (cycles shell variant + translucent hologram; advanced chameleon is deferred), and a fast-return switch (cycles historically visited exteriors as the next destination; history cap 16).
- First Doctor console time rotor bobbles vertically while the TARDIS is traveling (`DEMATERIALISING` / `IN_FLIGHT` / `MATERIALISING`) and rests when idle.
- Demat/mat/in-flight play loopable travel SFX (seamless loops) for code-configured phase lengths (`DEMATERIALISING_DURATION_TICKS` / `MATERIALISING_DURATION_TICKS` in `TardisTravelService`); shell vanishes mid-demat at `DEMATERIALISING_SHELL_REMOVE_AT_TICK`; `IN_FLIGHT` uses a higher-pitched demat/mat-derived loop in the interior; materialisation ends with a landing thud.

## How It Works In-Game
1. Place the TARDIS block.
2. Interact with the block to toggle door-state behavior (server-authoritative).
3. As the door opens, the console room becomes visible through the doorway (client preview).
4. When the door is fully open, walk into the exterior block to teleport to the interior entrance.
5. From inside, look through open interior doors to see the exterior world (SOTO preview).
6. Walk into the interior door blocks to return just outside the exterior TARDIS.
7. At the console: choose a destination — biome + planet dials, a saved waypoint, an online player, or fast return through previous landings — then pull the materialisation lever to dematerialise, wait for `IN_FLIGHT`, and pull again to materialise and land. Optionally cycle the chameleon circuit on Panel6 to preview/set the exterior shell.

## BOTI Notes
- Visual illusion: does not stream the live `dwm:tardis` dimension to the exterior client.
- When the interior has been generated, the preview shows a synced portal stream (meta + chunk columns + live entities) of the 11×7×17 console-room footprint. Until then (or if no chunks yet), it falls back to `FirstDoctorConsoleRoomLayout` (blocks only).
- Shared portal stream format (with SOTO): shell metadata + atmosphere + sparse chunks + entity spawn/update/remove, keyed by `PortalStreamKind.BOTI`. The client reconstructs synthetic BEs/entities and best-effort renders via `BlockEntityRenderDispatcher` / `EntityRenderDispatcher` (vanilla + mods). Entity poses are client-interpolated between sync samples. Players use a dedicated `OtherClientPlayerEntity` path because `EntityType.PLAYER` is not saveable. Interior doors remain excluded from BOTI (no dedicated interior-door BER in the exterior preview yet).
- Uses the shared deferred portal FBO pipeline (`render.portal`) with a hitch-fixed look-in camera at the interior door plane; composite UV crop uses each chameleon's `PortalAperture`. No stencil framebuffer required.
- May not work with Fabulous graphics / order-independent transparency; disable via `enableDoorPortals` if needed.

## SOTO Notes
- Uses the same deferred portal FBO pipeline and the same portal stream packet family as BOTI (shared `enableDoorPortals`, default on).
- Visual illusion: portal-composites a streamed exterior scene (shell + atmosphere + Chebyshev-2 ghost chunks + entities) through the classic interior door aperture (not a live world stream); hitch-fixed look-out at the shell door plane.
- Stream keyed by `PortalStreamKind.SOTO`; meta revision and chunk/entity lifecycle match BOTI's wire format.

## Known Constraints
- Interior visuals use the shipped `first_doctor_console_room` structure (11×7×17) with decor props; console and interior-door bank are linked to the exterior via `tardisId` on placement.
- Door open/closed for entry is server-authoritative; swing animation still updates locally on both sides.
- Shared exterior BOTI door aperture table per chameleon variant (`PortalAperture`); interior SOTO uses one classic 3×2 opening aperture.
- Shared single full-window portal FBO: last END_MAIN writer wins when multiple door portals render in one frame (no FBO pooling yet).
- Exterior SOTO footprint is axis-aligned (not rotated with exterior facing). Exit teleport and SOTO look-out follow the chameleon shell door facing (`TardisExteriorFacing`), which is opposite the raw `FACING_ROTATION` skull/banner south=0 convention because of shell BER transforms.

## Future Opportunities
- Per-chameleon BOTI / SOTO aperture meshes.
- Ownership and multi-room corridors.
