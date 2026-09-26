# Gameplay items (subclass, components, special renderer)

Use when `Item::new` is not enough: use/tick behaviour, persistent or synced
components, a special in-hand mesh, or a client screen/HUD. Keep this
checklist short and copy a live sibling. TARDIS travel, interiors, portals,
and chameleon belong in
[tardis/AGENTS.md](../../../../dwm/src/main/java/com/adamkali/dwm/tardis/AGENTS.md)
— do not re-derive them here.

Hand JSON / GUI select still follows [hand-modeled.md](hand-modeled.md).
Ingredients and tools stay on [flat-datagen.md](flat-datagen.md) /
[equipment.md](equipment.md).

## When you need this path

| Need | Sibling |
|------|---------|
| Use-on-block / bind / lock | `TardisKeyItem` |
| Use-in-air client screen | `FieldGuideItem` (`openGuide` callback) |
| Handheld tick + synced reading | `RadiationMeterItem` + `RADIATION_LEVEL` |
| Console circuit install | `ConsoleCircuitItem` + `registerCircuit` |
| Multi-mode tool + HUD | `SonicScrewdriverItem` + `SONIC_STATE` |
| Remote summon gadget | `StattenheimRemoteItem` |

## Common layers

1. **Item class** in `dwm/src/main/java/com/adamkali/dwm/item/` — stays on the
   **common** source set. Register from
   [`DWMItems`](../../../../dwm/src/main/java/com/adamkali/dwm/item/DWMItems.java)
   with a fresh `Properties` (uniques: `.stacksTo(1)`).
2. **Extract rules** into a `*Logic` class with JUnit under
   `dwm/src/test/java/.../item/` (copy
   [`RadiationMeterReadoutTest`](../../../../dwm/src/test/java/com/adamkali/dwm/item/RadiationMeterReadoutTest.java),
   sonic `*LogicTest`s). Keep the `Item` subclass thin.
3. **Client UI** — do not import client classes from common. Expose a
   `public static @Nullable Consumer<...>` on the item and assign it from
   [`DWMClient`](../../../../dwm/src/client/java/com/adamkali/dwm/DWMClient.java)
   (`FieldGuideItem.openGuide`, `SonicScrewdriverItem.openFieldModeSelector`).
4. **Lang + tab + recipes** still go through datagen
   ([flat-datagen.md](flat-datagen.md)). Models are either `generateFlatItem`
   (circuits, Stattenheim, sonic settings) or hand JSON
   ([hand-modeled.md](hand-modeled.md)).

## Data components

Persistent or synced stack state lives on
[`DWMDataComponents`](../../../../dwm/src/main/java/com/adamkali/dwm/item/DWMDataComponents.java).
[`DWMMain`](../../../../dwm/src/main/java/com/adamkali/dwm/DWMMain.java) already
calls `DWMDataComponents.initialize()` before `DWMItems.initialize()`.

- Persistent + networked — `BOUND_TARDIS_ID`, `SONIC_STATE` (codec +
  stream codec)
- Ephemeral networked only — `RADIATION_LEVEL` (no `.persistent(...)`)

Do not stuff gameplay into NBT helpers; add a typed component.

## Special in-hand mesh

Gold example: radiation meter. JSON models cannot draw the chunky handheld
body, so the item uses `minecraft:special`:

1. Common item:
   [`RadiationMeterItem`](../../../../dwm/src/main/java/com/adamkali/dwm/item/RadiationMeterItem.java)
   (`inventoryTick` writes `RADIATION_LEVEL`).
2. Client model:
   [`RadiationMeterModel`](../../../../dwm/src/client/java/com/adamkali/dwm/model/item/RadiationMeterModel.java)
   — `ModelLayerLocation` + entity atlas
   `textures/entity/radiation_meter.png`.
3. Layer:
   [`DWMRenderLayerManager`](../../../../dwm/src/client/java/com/adamkali/dwm/DWMRenderLayerManager.java)
   (`ModelLayerRegistry.registerModelLayer`).
4. Renderer:
   [`RadiationMeterSpecialRenderer`](../../../../dwm/src/client/java/com/adamkali/dwm/render/RadiationMeterSpecialRenderer.java)
   (`SpecialModelRenderer` with a `Display` argument — not
   `NoDataSpecialModelRenderer`, which is the TARDIS-block pattern).
5. Register the unbaked codec **before** resource reload in `DWMClient`:

```java
SpecialModelRenderers.ID_MAPPER.put(
        Identifier.fromNamespaceAndPath(DWMReference.MOD_ID, "radiation_meter"),
        RadiationMeterSpecialRenderer.Unbaked.MAP_CODEC);
```

6. Hand item JSON
   [`items/radiation_meter.json`](../../../../dwm/src/client/resources/assets/dwm/items/radiation_meter.json):
   `"type": "minecraft:special"` + `"model": { "type": "dwm:radiation_meter" }`.
   The `type` must match the `ID_MAPPER` id.
7. Skip `generateFlatItem`. Still `addItem`, recipe, and
   `TOOLS_AND_UTILITIES` tab.

Block-entity special renderers (`tardis_globe`, console) belong to
[add-new-block complex.md](../../add-new-block/references/complex.md), not
this skill.

## Circuits extra

New console circuits:

1. Add the enum constant on
   [`TardisCircuit`](../../../../dwm/src/main/java/com/adamkali/dwm/tardis/data/model/TardisCircuit.java)
   and wire flags in `CircuitFittedLogic` / console look-targets — that is
   tardis package work, not a second install path.
2. `registerCircuit(TardisCircuit.EXAMPLE, "circuit_example")` in `DWMItems`
   (already `.stacksTo(16)` + `ConsoleCircuitItem`).
3. `generateFlatItem(..., FLAT_ITEM)`, prune substring `circuit`,
   `TOOLS_AND_UTILITIES`, `addItem`.
4. Copy sibling circuit recipes and
   [`CircuitInstallLogic`](../../../../dwm/src/main/java/com/adamkali/dwm/tardis/logic/CircuitInstallLogic.java)
   tests / [`CircuitFittedGameTests`](../../../../dwm/src/main/java/com/adamkali/dwm/gametest/CircuitFittedGameTests.java).

Remote-summon is the only circuit that `use()`s in air (other hand must hold
the Stattenheim remote). Do not special-case others.

## Networking

Gameplay state is server-authoritative. New C2S payloads: register type/codec
before handlers, validate on the server. See [AGENTS.md](../../../../AGENTS.md)
Networking & Side Safety.

## Tests and docs

- Unit: `*Logic` JUnit next to the item package.
- GameTest: in-world use —
  [`TardisDoorGameTests`](../../../../dwm/src/main/java/com/adamkali/dwm/gametest/TardisDoorGameTests.java)
  (key),
  [`SonicInteractionGameTests`](../../../../dwm/src/main/java/com/adamkali/dwm/gametest/SonicInteractionGameTests.java),
  [`FieldGuideGrantGameTests`](../../../../dwm/src/main/java/com/adamkali/dwm/gametest/FieldGuideGrantGameTests.java).
  Run `./dwm/gradlew runGametest` when GameTest code changes.
- Docs: [`docs/feature-sonic-screwdrivers.md`](../../../../dwm/docs/feature-sonic-screwdrivers.md),
  [`docs/feature-stattenheim-remote.md`](../../../../dwm/docs/feature-stattenheim-remote.md),
  [`docs/feature-field-guide.md`](../../../../dwm/docs/feature-field-guide.md),
  [`docs/feature-tardis-block.md`](../../../../dwm/docs/feature-tardis-block.md)
  (key / doors).
