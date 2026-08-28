# Feature: TARDIS Exterior Block

See also: [Docs Index](./index.md), [TARDIS Chameleon System](./feature-chameleon-system.md), [Stattenheim Remote](./feature-stattenheim-remote.md)

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
- Interactive door state: exterior and interior share {@code TardisDataModel.doorState}; clicking either side opens/closes both.
- Custom client rendering for TARDIS model presentation.
- Exterior BOTI (bigger on the inside): deferred portal FBO composites a hitch-fixed console-room look-in through the chameleon door aperture when `doorSwing >= 0.15`.
- Interior SOTO (smaller on the outside): deferred portal FBO composites a hitch-fixed exterior look-out through the classic interior door aperture when interior `doorSwing >= 0.15`.
- Shared empty dimension `dwm:tardis` hosting generated interiors.
- Lazy placement of `first_doctor_console_room` structure template on first entry.
- `TardisBlockEntity` stores `interiorEntrance` / `interiorGenerated`.
- Exterior return coordinates stored on `TardisDataModel` for exit teleports.
- First player to enter an unowned TARDIS claims it (`ownerUuid`) if they do not already own one (one TARDIS per player). Overlay: `This TARDIS is yours`.
- **Owner-only piloting:** travel lever, destination controls (biome / planet / waypoint / player locator / telepathic / fast return / coordinate locks), cloak, chameleon, and stabilisers require the TARDIS owner. Visitors see “You do not own this TARDIS”. Unowned ships cannot be piloted until claimed. Panel1 atmosphere readers and the Panel5 refueler stay usable by anyone. Panel4 door lock: owner **or** a player holding a key bound to that TARDIS (main or offhand). C2S waypoint / player-locator / chameleon payloads and the exterior sneak chameleon GUI enforce the same owner gate.
- First-hour advancements (Doctor Who tab): find a TARDIS → claim it, then sibling branches for a same-world hop, binding a key, first refuel, and first circuit install. First other-world materialise hangs off first circuit; first Gallifrey landing is the GOAL under that. Cloak, ping, and Stattenheim remote toasts stay deferred (DWM-061).
- Worldgen (mineshaft) Type 40s start **unfinished**: same-world biome hops only, stabilisers off, **30 artron** (three local hops), and most console circuits (planet locator, waypoints, player locator, telepathic, fast return, cloak, chameleon, coordinate locks, remote summon) are **broken** — empty-hand click shows “This circuit is broken” and a smoke puff. The owner repairs them by using a matching craftable circuit item on that control (remote summon: circuit in one hand, Stattenheim remote in the other). Claim does not auto-fit or grant spare parts. Creative `/give` and player-placed ships stay fully working with a full artron tank (500); existing saves without circuit flags or an artron field stay fully working / full.
- **Craftable circuits** (`stacksTo(16)`, Tools & Utilities): owner-only install on the matching console control; consume unless creative. Coordinate locks accept any of X/Y/Z. Remote summon has no console control — use `circuit_remote_summon` with a Stattenheim remote in the other hand. Wrong control / already fitted / visitor: overlay, do not consume, do not activate the control. Installs are independent (any order). Other-world travel still requires a fitted planet locator. Survival circuits are craft-only (no worldgen/claim loot). Craft-cost ladder (not a gate): landing kit (ferrite + redstone / compass / clock / amethyst) → planet locator (ferrite + ender pearl, no Zeiton) → telepathic / cloak / chameleon / remote summon / player locator last (vanilla+ferrite, or cheaper ferrite+Zeiton).
- `/tardis rebuild` regenerates the owned console room from the current structure template without changing TARDIS UUID or linked data; ops may `/tardis rebuild <uuid>`.
- Ops `/tardis claim` (permission 2) overwrites ownership to the caller if they do not already own a TARDIS: stand inside an interior, or pass `/tardis claim <uuid>`.
- Collision entry when the exterior door is open (`doorSwing >= 0.9`); exit via open interior doors.
- Landing search requires replaceable space in the door-facing column (feet + head), not only the shell cell.
- Automatic surface landings stay below a dimensional bedrock ceiling (e.g. the Nether roof); exact coordinates on it (waypoint, player, summon, fast return) are unchanged.
- Single config toggle `enableDoorPortals` (default on) via Mod Menu / Cloth Config; legacy `enableBoti` / `enableSoto` migrate on load.
- Interior doors use an invisible block + dedicated BER (`TardisClassicInteriorDoorModel`) with swing animation.
- Materialisation lever travel: first pull dematerialises the exterior; after a short hold the TARDIS enters `IN_FLIGHT`; a second pull materialises at the destination resolved from the active `DestinationMode`.
- Destination modes: `BIOME` (default — selected dimension/biome landing search), `WAYPOINT` (exact saved exterior coords), `PLAYER` (live online player position at materialise; fails with overlay if offline), `FAST_RETURN` (exact historically visited exterior from LIFO history), `TELEPATHIC` (using player's bed/respawn, else that dimension's world spawn).
- First Doctor console hex faces have fixed purposes (`FirstDoctorConsoleControls.ConsolePanel`):
  - **Panel1 Environment (0°)** — exterior atmosphere instruments (oxygen / pressure / temperature readers on the middle row; radiation reader on the bottom row).
  - **Panel2 Communications (+60°)** — telepathic circuit on the middle row (distress reserved for DWM-035).
  - **Panel3 Navigation (+120°)** — destination dials (existing) plus a bottom-row coordinate lock.
  - **Panel4 Security (180°)** — cloak lever (middle) and door lock (bottom); shields reserved for DWM-035.
  - **Panel5 Systems (−120°)** — refueler gauge on the middle row (live artron tank; flight/float reserved for DWM-035).
  - **Panel6 Helm (−60°)** — materialisation, chameleon, fast return, stabilisers (existing).
- Each panel deck has three cuboid rows: **top** (toward rotor, `TOP_MOUNT_*`), **middle** (`CONTROL_MOUNT_*`), **bottom** (player-facing, `BOTTOM_MOUNT_*` / stabilisers mount).
- First Doctor console Panel3 hosts four dials: biome selector, waypoint selector, player locator, and planet locator (shared dial mesh). Planet locator cycles loaded worlds except `dwm:tardis` (including `dwm:gallifrey`). Waypoint/player dials open GUIs (no `MenuType`) to save/delete/select waypoints (cap 16; current = linked exterior) or select another online player; cycling biome/planet resets mode to `BIOME`.
- First Doctor console Panel6 hosts the materialisation lever, a basic chameleon circuit dial (cycles shell variant + translucent hologram; advanced chameleon is deferred), a fast-return switch (cycles historically visited exteriors as the next destination; history cap 16), and a bottom-row stabilisers toggle (default on — precise landing; off scatters the materialise landing within ~4–24 blocks then re-validates; unstabilised flight speeds the time rotor and adds light smoke).
- Panel1 readers sample the **linked exterior** (not the interior) about once a second: oxygen (0 if waterlogged/no air; reduced Nether/End), pressure (Y vs sea level / dimension), temperature (biome), radiation (high Nether, medium End, low Overworld with a thunder bump). In flight or with no exterior they show no signal. Needles and HUD use the synced 0–1 reading; click repeats the HUD as an overlay.
- Panel5 refueler is a live artron tank (0–500) on `TardisDataModel.artron`. The needle and HUD show `Artron reserves: N%` (empty overlay at 0). Anyone may read or feed it. Use **Zeiton Crystals** to add 25 (clamp 500); powder hints to convert first; already-full does not consume. Demat and Stattenheim summon spend **10** same-world or **30** on a dimension change; materialise and cloak are free. Insufficient artron refuses with `Not enough artron` (or `Artron reserves: empty` at 0) and does not start a phase. Creative skips spend and crystal consume.
- Panel4 cloak is a perception filter (`TardisDataModel.cloaked`): the exterior BER skips shell, doors, and BOTI while collision and door-click remain. SOTO from inside is unchanged. Overlay: `Cloak engaged` / `Cloak disengaged`.
- Panel4 door lock (`doorsLocked`) blocks **opening** only; closing always works on exterior and interior doors. Lock and unlock only apply when doors are **fully closed**. Overlay: `Doors locked` / `Doors unlocked` / `Doors must be closed`. Clicking a locked closed door shows `Doors are locked`.
- The TARDIS key is crafted from gold nuggets and iron. An owner can bind an unbound key to their TARDIS; it stores that TARDIS UUID (not the owner), so a future ownership transfer will not invalidate it. Anyone holding a bound key can toggle that TARDIS's door lock while the doors are closed.
- Stattenheim remote: sneak-right-click the ground to summon the owner's TARDIS to that cell (precise landing, door facing the player). Parked shells dematerialise then auto-materialise; an in-flight TARDIS materialises at the click. Exterior fade-out/fade-in during demat/mat. Summon slams the doors shut immediately; they stay closed on landing.
- Panel2 telepathic circuit arms `DestinationMode.TELEPATHIC` onto the using player's bed/respawn, or world spawn if none. Overlay: locked onto your home / world spawn.
- Panel3 coordinate lock is not a destination mode: X/Y/Z toggles pin those axes to the current exterior after landing resolve + scatter, then re-validate. Invalid pin fails materialise with the existing invalid-landing overlay. HUD: `X axis locked` / `unlocked` (same for Y/Z).
- First Doctor console time rotor bobbles vertically while the TARDIS is traveling (`DEMATERIALISING` / `IN_FLIGHT` / `MATERIALISING`) and rests when idle; `Time_middle` spins on Y during those same phases (faster with stabilisers off) and is still when idle.
- Demat/mat/in-flight play loopable travel SFX (seamless loops) for code-configured phase lengths (`DEMATERIALISING_DURATION_TICKS` / `MATERIALISING_DURATION_TICKS` in `TardisTravelService`); shell vanishes mid-demat at `DEMATERIALISING_SHELL_REMOVE_AT_TICK`; `IN_FLIGHT` uses a higher-pitched demat/mat-derived loop in the interior; materialisation ends with a landing thud. Travel does not auto-close or auto-open doors.

## How It Works In-Game
1. Place the TARDIS block.
2. Interact with the exterior or interior doors to toggle shared door-state behavior (server-authoritative).
3. As the door opens, the console room becomes visible through the doorway (client preview).
4. When the door is fully open, walk into the exterior block to teleport to the interior entrance.
5. From inside, look through open interior doors to see the exterior world (SOTO preview).
6. Walk into the interior door blocks to return just outside the exterior TARDIS.
7. At the console (owner only for piloting): choose a destination — biome + planet dials, a saved waypoint, an online player, fast return through previous landings, or the telepathic circuit (home bed / world spawn) — then pull the materialisation lever to dematerialise (costs 10 artron same-world, 30 to change dimension), wait for `IN_FLIGHT`, and pull again to materialise and land. Optionally cycle the chameleon circuit on Panel6 to preview/set the exterior shell. Toggle stabilisers on Panel6’s bottom row before materialising if you want a precise vs scattered landing. Panel3 coordinate locks can pin X/Y/Z to the current exterior after scatter. Panel4 cloak hides the exterior shell; the door lock blocks opening and can only be toggled while doors are closed (owner or bound key). Panel1 readers show the linked exterior atmosphere (anyone). Panel5 refueler shows artron and accepts Zeiton Crystals from anyone.
8. Found Type 40s: craft a circuit and use it on the matching control (owner only). Remote summon: Stattenheim remote in the other hand. Planet locator is required before other-world travel; other circuits can be fitted in any order.

## BOTI Notes
- Visual illusion: does not stream the live `dwm:tardis` dimension to the exterior client.
- When the player is near the exterior shell, the server deferred-preloads the console room (ticket chunks, then place on a later tick) and the client warms the BOTI portal stream so real ghost meshes are ready when the door opens.
- Preview shows a synced portal stream (meta + chunk columns + live entities) of the interior plot within Minecraft view/render distance (clipped to the allocated TARDIS plot so neighboring interiors stay hidden) via `SotoGhostMeshCache`. Until meshes arrive the doorway stays blank (no blueprint fallback).
- Shared portal stream format (with SOTO): shell metadata + atmosphere + sparse chunks, compact block/sky-light volumes, and entity spawn/update/remove, keyed by `PortalStreamKind.BOTI`. The client reconstructs synthetic BEs/entities and best-effort renders via `BlockEntityRenderDispatcher` / `EntityRenderDispatcher` (vanilla + mods). Terrain, block entities, and entities use the sampled scene lighting. Entity poses are client-interpolated between sync samples. Players use a dedicated `OtherClientPlayerEntity` path because `EntityType.PLAYER` is not saveable. Interior doors remain excluded from BOTI (no dedicated interior-door BER in the exterior preview yet).
- Uses the shared deferred portal FBO pipeline (`render.portal`) with a hitch-fixed look-in camera at the interior door plane; composite UV crop uses each chameleon's `PortalAperture`. No stencil framebuffer required.
- Applies atmosphere-colored distance fog across the streamed view-distance depth. Newly placed/rebuilt console rooms stamp and propagate their intended full-strength invisible console light before BOTI chunk sync.
- May not work with Fabulous graphics / order-independent transparency; disable via `enableDoorPortals` if needed.

## SOTO Notes
- Uses the same deferred portal FBO pipeline and the same portal stream packet family as BOTI (shared `enableDoorPortals`, default on).
- Visual illusion: portal-composites a streamed exterior scene (shell + atmosphere + view-distance ghost chunks + entities) through the classic interior door aperture (not a live world stream); hitch-fixed look-out at the shell door plane.
- Stream keyed by `PortalStreamKind.SOTO`; meta revision and chunk/entity lifecycle match BOTI's wire format. Sampled sky/block light illuminates terrain and features, while atmosphere-colored distance fog blends the outer stream into its backdrop.

## Known Constraints
- Interior visuals use the shipped `first_doctor_console_room` structure (11×7×17) with decor props; console and interior-door bank are linked to the exterior via `tardisId` on placement.
- Ownership is first-enter or ops `/tardis claim` (no place-time claim); worldgen TARDISes stay unowned until entered or claimed, and start with broken circuits until the owner crafts and installs matching circuit items. Console piloting (travel, destinations, cloak, chameleon, stabilisers) is owner-only; readers are public; door lock accepts the owner or a held bound key. `/tardis rebuild` only targets the caller's owned TARDIS (or any UUID for ops).
- Door open/closed for entry is server-authoritative; swing animation still updates locally on both sides.
- Shared exterior BOTI door aperture table per chameleon variant (`PortalAperture`); interior SOTO uses one classic 3×2 opening aperture.
- Shared single full-window portal FBO: last END_MAIN writer wins when multiple door portals render in one frame (no FBO pooling yet).
- Exterior SOTO footprint is axis-aligned (not rotated with exterior facing). Exit teleport and SOTO look-out follow the chameleon shell door facing (`TardisExteriorFacing`), which is opposite the raw `FACING_ROTATION` skull/banner south=0 convention because of shell BER transforms.

## Future Opportunities
- Per-chameleon BOTI / SOTO aperture meshes.
- Ownership and multi-room corridors.
