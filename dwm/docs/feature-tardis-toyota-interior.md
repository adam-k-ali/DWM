# Feature: TARDIS Toyota Interior (Series 7)

See also: [Docs Index](./index.md), [TARDIS Exterior Block](./feature-tardis-block.md), [Building Content System](./feature-building-content.md), [Block and Item Definitions](./feature-block-item-definitions.md)

> **Status: planned design only.** Nothing in this document is implemented. There is no Toyota room, no Series 7 console block and none of the new blocks below. It is a blueprint for later tickets.

## Product Intent
Give the TARDIS a second, visually distinct interior: the Series 7 console room ("Toyota" is the working name). A player should be able to tell the two rooms apart at a glance, while the console controls and gameplay stay identical to the First Doctor room.

> **Assumption.** The repo has no reference material for this room. The visual brief below comes from recollection of the Series 7 set and is meant to be retuned. The Palette section and the roundel/trim rules in Geometry Spec are isolated so they can change without touching the rest of the design.

## Visual Brief
- A darker, more industrial room than the First Doctor's white roundel hall.
- Octagonal footprint, so the room reads as round rather than as a box.
- A raised central dais carrying the console.
- A glass time-rotor shaft rising above the console to the ceiling.
- Two raised side galleries with railings, reached by short staircases.
- Warm copper trim against grey walls.
- Roundels as the wall pattern, with a ring of big roundels in the ceiling.

## Geometry Spec
All coordinates are **plot-local**, relative to the structure origin. The room must fit inside `TardisPlotAllocator.PLOT_SPACING` (64) on both horizontal axes. Conventions match `FirstDoctorConsoleRoomLayout`: the door bank is 3×2 and faces SOUTH, and the entrance is the standing position (feet) just in front of the door.

| Item | Value |
|---|---|
| Size X / Y / Z | 23 / 12 / 23 (`y=0` floor, `y=1..10` volume, `y=11` ceiling) |
| Door bank origin | (10, 1, 1): lower cell, slot 0; slots run east when facing south; centred on the north flat |
| Entrance (feet) | (11, 1, 2) |
| Console | (11, 2, 11), on the dais |
| Console light | `LOCAL_CONSOLE.above(3)`, i.e. (11, 5, 11) |
| Reserved structure id | `dwm:toyota_console_room` (no `.nbt` ships yet) |

Let `dx = x − 11` and `dz = z − 11`.

- **Footprint.** Cell `(x, z)` is inside when `|dx| ≤ 11`, `|dz| ≤ 11` and `|dx| + |dz| ≤ 17`. Cells outside are structure void. The north flat is `x = 5..17`, which contains the door bank (`x = 10..12`).
- **Walls.** Footprint cells with at least one 4-neighbour outside the footprint.
- **Dais.** Cells with `dx² + dz² ≤ 20` are raised one block at `y = 1`, so the console stands at `y = 2`.
- **Side galleries** (west `x = 1..5`, east `x = 17..21`, both `z = 6..13`):
  - Floor blocks at `y = 3`, so the walking surface is `y = 4`.
  - `iron_bars` rail on the inner edge (`x = 6` west, `x = 16` east) at `y = 4`.
  - A three-step `quartz_stairs` run at `z = 16, 15, 14` (`y = 1, 2, 3`), ascending north into the gallery, with solid fill under each step.
  - The east side mirrors the west: `x → 22 − x`.
- **Rotor shaft.** The 3×3 column `x = 10..12`, `z = 10..12`, `y = 6..10` is kept clear for the time rotor. A `glass_pane` ring surrounds it and `WHITE_BIG_ROUNDEL_A` cells sit around it in the ceiling (`y = 11`).
- **Roundels and trim.** `WHITE_ROUNDEL_A` on wall cells at `y ∈ {3, 7}` where `(x + z) % 3 == 0`. Copper trim runs along the wall at `y = 1` and `y = 9`. These rules are deterministic and meant to be tuned in review.
- **Ceiling.** Every footprint cell has a ceiling block at `y = 11`.

## Palette
Block fields below are in `DWMBlocks` unless marked vanilla. `WHITE_ROUNDEL_A`, `WHITE_BIG_ROUNDEL_A`, `WHITE_TARDIS_WALL` and `LIGHT_GRAY_TARDIS_WALL` are already used by the First Doctor room. `BLACK_TARDIS_WALL`, `GRAY_TARDIS_WALL` and `TEAL_ROUNDEL_A` also exist (each has a block definition JSON).

| Role | Block |
|---|---|
| Floor | `BLACK_TARDIS_WALL` |
| Dais | `LIGHT_GRAY_TARDIS_WALL` |
| Walls | `GRAY_TARDIS_WALL` |
| Wall roundels | `WHITE_ROUNDEL_A` |
| Ceiling ring | `WHITE_BIG_ROUNDEL_A` |
| Trim | vanilla `waxed_cut_copper` (until the trim block below exists) |
| Stairs | vanilla `quartz_stairs` (as in the First Doctor room), later TARDIS stairs |
| Rails | vanilla `iron_bars`, later the TARDIS railing |
| Rotor shaft | vanilla `glass_pane`, later the time-rotor column |
| Light | `minecraft:light` at the console light position |

The only brass-like family in the mod today is Dalekanium, which is Skaro-themed, so vanilla copper is the stand-in for trim.

## Series 7 Console
### Today
- There is no console abstraction. The First Doctor console is First-Doctor-specific end to end:
  - `FirstDoctorConsoleBlock`: block and the `activateControl` dispatch over every control.
  - `FirstDoctorConsoleControls`: the six hex `ConsolePanel`s, the 19-control `LookTarget` enum and per-control hit boxes (via `ConsoleControlSpec`).
  - `FirstDoctorConsoleBlockEntity`: tardis link, synced display state, spawning one `ConsoleControlInteractionEntity` per control.
  - Client: `FirstDoctorConsoleBlockEntityRenderer`, `FirstDoctorConsoleSpecialRenderer`, `FirstDoctorConsoleModel`, the JSON entity model with its 40×40 texture, and 19 widget meshes.
- Already generic and reusable: `ConsoleDisplayState`, `ConsolePilotLogic`, `CircuitInstallLogic`, the `dwm.console.*` lang keys and the widget meshes.
- A copy-paste console would duplicate roughly 1.4k lines of Java plus a model, so the base must be extracted first.

### Design
- **Block.** `dwm:series_7_console` with a `FACING` property, an INVISIBLE render shape (the mesh comes from the block entity renderer), a pedestal collision box, strength −1 and light 15, matching the First Doctor console. It is creative-only and cannot be broken in survival, like the First Doctor console.
- **Refactor first.** Extract an abstract console base covering the shared behaviour:
  - the `activateControl` dispatch, owner and broken-circuit gates;
  - `tardisId` and `ConsoleDisplayState` sync;
  - control-entity maintenance.

  Detach `ConsoleControlSpec` and `LookTarget` from `FirstDoctorConsoleControls`, and replace the hard-wired `instanceof FirstDoctorConsoleBlock` checks in `ConsoleControlInteractionEntity`, `ConsoleControlHud`, `SonicTardisLogic` and `CircuitFittedLogic`.
- **Layout.** A hexagonal deck around a translucent time-rotor column, six panels like the First Doctor console. The control set and behaviour are unchanged, so players keep their muscle memory. Panel assignments:

  | Panel | Purpose | Controls |
  |---|---|---|
  | 1 Environment | exterior atmosphere | oxygen, pressure, temperature, radiation readers |
  | 2 Communications | comms | telepathic circuit |
  | 3 Navigation | destination | biome, waypoint, player and planet dials; X/Y/Z coordinate locks |
  | 4 Security | access | cloak lever, door lock |
  | 5 Systems | power | refueler (artron gauge) |
  | 6 Helm | flight | materialisation lever, chameleon circuit, fast return, stabilisers |

  That is all 19 interactive `LookTarget` values. Hit boxes live in a Series 7 `ConsoleControlSpec` catalog, with mounts taken from the new mesh.
- **Assets.** A JSON entity model and texture under `assets/dwm/models/entity/`, a model class and render layer registration (`DWMRenderLayerManager`), a block entity renderer and item special renderer, plus blockstate, item model and lang entries. The widget meshes are reused.
- **Rotor animation.** Reuse the `TardisControlVariables` inputs (`rotor_bob`, `rotor_spin`) so the rotor bobs while travelling and spins faster with stabilisers off, as on the First Doctor console.
- **Room link.** `FirstDoctorConsoleSync.findConsole` and the placer's `stampInteriorEntities` currently look for the First Doctor console at a fixed position. They must resolve the console by interior type.
- **Tests this needs.** Hit-box geometry (model on `FirstDoctorConsoleControlsTest`), model animation (`FirstDoctorConsoleModelTest`), a client GameTest screenshot of the console, and a server GameTest for control activation.

## New Blocks
Every block below is absent today. Each lists its behaviour and the implementation route.

- **Time-rotor column** (`dwm:time_rotor_column`). A glass-and-metal shaft that spans the room height and animates with the travel phase. The rotor only exists inside the console mesh today, so this is a block entity plus renderer, driven by the same travel-phase inputs as the console rotor. Java registration; not JSON-definable.
- **Emissive lamp and glowing roundel.** A full-bright lamp, plus a roundel variant with an emissive overlay. Roundels emit light level 10 today but their textures are not emissive. Precedents: the globe's glow texture (`tardis_globe_glow.png`) and `DalekGlowLayer`. A plain JSON block cannot carry an emissive texture, so this needs a model-level or render-layer solution; decide that before ticketing.
- **TARDIS stairs, slabs and railing.** Stairs and slabs for the wall families, plus an iron-grating railing. `stairs` and `slab` are supported block definition types with a `base`, so these are cheap JSON files plus models. The railing needs a fence-like block (Java, or a `wall` type if the shape fits).
- **Copper trim.** A plain JSON block. Generate its palette with `dwm/tools/palette/`; the existing `bronze_dalekanium` palette is the nearest seed.
- **Pipes and struts.** A decor prop in the `TardisDecorBlock` family for the ceiling and wall detail.

Not planned: organic or coral textures. A metal-and-glass Series 7 look does not need them.

### Wiring checklist per block
1. Block definition JSON (`data/dwm/block/definition/`) or Java registration.
2. Creative tab entry in `DWMBlocks`.
3. `DWMLanguageProvider` line.
4. `DWMLootTableProvider` drop.
5. Blockstate and model JSON under `src/client/resources` (hand-maintained for wall and roundel families).
6. Recipe, if craftable.
7. An allowlist entry in `pruneDatagenItemModels` (`build.gradle`) for datagen-owned item models, otherwise the item model is silently deleted.
8. Run `./dwm/gradlew runDatagen` and commit intentional output.

## Interior Selection Gap
There is no notion of interior type today. `TardisDimensions.CONSOLE_ROOM_STRUCTURE_ID` is a single global id, and `TardisDataModel` has no room field. Supporting a second room needs, in order:
- A nullable `TardisDataModel.interiorType`, defaulting to First Doctor when null, so existing saves load unchanged.
- A room descriptor (structure id, size, entrance, door origin and facing, console and light positions) replacing the `FirstDoctorConsoleRoomLayout` statics.
- A selection rule or UI. That is a product decision and is not made here.

These call sites hard-code First Doctor room constants and must move to the descriptor:

| Area | Where |
|---|---|
| Placement | `FirstDoctorConsoleRoomPlacer` |
| Rebuild box and preload tickets | `TardisInteriorService`, `TardisInteriorPreloadService` |
| Monitor lookup by plot | `TardisDataLoader.findAtInteriorPos` |
| Console position, in-room check | `TardisTravelAudio`, `FirstDoctorConsoleSync` |
| Door origin | `TardisLogic`, `PortalStreamSyncService` |
| Light position and chunk bounds | `BotiInteriorSampler` |
| BOTI client door geometry | `BotiPortalContent`, `TardisBotiRenderer` (hard-coded door centre and `Direction.SOUTH`) |
| GameTests | `TardisInteriorGameTests`, `TardisMonitorGameTests` |

## Follow-up Work
Suggested split, as backlog tickets (IDs not yet allocated):
1. `TardisDataModel.interiorType` and the room descriptor.
2. Layout class `ToyotaConsoleRoomLayout` plus unit tests (size, anchors, symmetry, door bank, BOTI visibility, console on dais), then the `dwm:toyota_console_room` structure. Authoring options: build it in-game, or write it as a `TemplateLayout` and register it in `StructureTemplateGeneratorTest.allTemplates()` so it gets a drift check.
3. Console base-class extraction.
4. Series 7 console block, block entity, controls and model.
5. New blocks, one ticket per family: stairs, slabs and railing; rotor column; emissive lamp; copper trim; pipes and struts.
6. Per-room BOTI/SOTO door geometry on the client.
7. Interior selection UI or command.

## Constraints and Open Questions
- The reference look is unverified; confirm it before texturing.
- Room size is capped by `PLOT_SPACING` (64). 23×12×23 leaves wide margin.
- `BotiRelativePosCodec` packs 5 bits per axis (0..31), and 23 fits. Production sync uses relative VAR_INT coordinates instead.
- BOTI sampling cost grows with room volume. Re-check `PortalStreamGameTests.botiStream_FullSampleStaysWithinBudget` with the new room, since 23×12×23 (6348 cells before the octagon cut) is nearly 5× the First Doctor's 11×7×17 (1309 cells).
- The door-bank facing is hard-coded to SOUTH in several places. Keep the Toyota door SOUTH-facing until the client geometry is generalised.
