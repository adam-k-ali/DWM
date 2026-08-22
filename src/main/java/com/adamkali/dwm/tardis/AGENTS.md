# AGENTS.md

## Scope
This file applies to `com.adamkali.dwm.tardis` and its subpackages — the TARDIS exterior, interior dimension, console, travel, and portal systems.

## Local Context
TARDIS gameplay spans persistence, server-authoritative logic, world placement, and heavy client rendering. Product behaviour is documented in `docs/feature-tardis-block.md`, `docs/feature-tardis-door-button.md`, and `docs/feature-chameleon-system.md` (experimental, config-gated).

### Package layout
| Package | Responsibility |
|---------|----------------|
| `tardis.logic` | Server-side rules and orchestration (`TardisLogic`, travel, locks, circuits, landing). Prefer extracting testable pure logic here. |
| `tardis.data` / `data.model` | Persistence (`TardisDataLoader`, JSON on disk) and immutable-ish state models (`TardisDataModel`, waypoints, door/travel phase). |
| `tardis.interior` | Interior dimension layout, plot allocation, room placement (`TardisInteriorService`, `FirstDoctorConsoleRoomLayout`). |
| `tardis.portal` | Portal stream sampling/sync between exterior aperture and interior view (`PortalStreamSyncService`). |
| `tardis.boti` | Bigger-on-the-inside interior mesh sampling and relative position codec (shared data for client BOTI renderer). |
| `tardis.soto` | Scanning-the-outside exterior sampling and atmosphere (chameleon/SOTO path). |
| `block` / `block.entities` (outside package) | Block types and BEs that TARDIS logic drives — keep block interaction thin, delegate to logic. |
| `src/client/java/.../render/` | Client renderers (`TardisBlockEntityRenderer`, `PortalDoorRenderer`, `TardisBotiRenderer`, console HUD). No gameplay state changes. |

Entry orchestration for doors/travel generally flows: block/BE interaction → `TardisLogic` / feature `*Logic` → `TardisDataLoader` persist → optional `PortalStreamSyncService` or interior services.

## Commands
- Unit tests: `./gradlew test --tests "com.adamkali.dwm.tardis.*"`
- GameTests (door/interior/landing/console): `./gradlew runGametest`
- YAML client flows (UI/world creation): `./gradlew runScenarioTest -Pscenario=placeAndOpenTardis` (see `src/scenarioTest/AGENTS.md`)

## Conventions
- **Server authoritative** — mutate `TardisDataModel` on the server; sync to clients via existing payloads/render state, not client-side persistence.
- **Logic extraction** — new rules belong in focused `*Logic` classes with JUnit tests under `src/test/java/.../tardis/logic/`.
- **GameTests** — redirect TARDIS saves in tests: `TardisDataLoader.tardisSaveDirectory = context.getWorld().getServer().getSavePath(WorldSavePath.ROOT).resolve("gametest_tardis_data");`
- **Rendering** — portal/BOTI changes stay in `src/client/java`; keep mesh/cache logic testable where possible (`BotiInteriorMeshCacheTest`, etc.).
- Blocks register in `DWMBlocks`; TARDIS-specific blocks often pair with dedicated BEs and datagen in `com.adamkali.dwm.datagen`.

## Common Pitfalls
- Do not reference client render classes from `tardis.logic` or `tardis.data`.
- Door swing/travel phase timing — use `waitTicks` in scenario tests; unit-test phase transitions in logic tests.
- Chameleon/SOTO paths are config-gated and off by default — do not assume enabled in tests without setting config.
- Interior rebuild/plot allocation is order-sensitive — check `TardisInteriorGameTests` before changing placement rules.
- Travel audio assets may be regenerated via `tools/` scripts — commit resulting OGGs under client resources, not fixture WAVs.
