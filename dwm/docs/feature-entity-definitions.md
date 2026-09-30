# Feature: Entity Definitions

See also: [Docs Index](./index.md)

## Product Intent

Author living-mob combat stats and AI goal lists as JSON instead of Java `createAttributes()` / `registerGoals()` bodies, so new fauna can be tuned without recompiling entity classes.

## Player Outcomes

Unchanged from the current Broakir, Flutterwing, Mewing Dog, Time Lord, and Dalek behaviour. This is an authoring API, not a new in-game screen.

## Layout

Definitions are hand-maintained under `data/dwm/entity/definition/<id>.json`. The file stem must match the `entity` id (`dalek.json` → `dwm:dalek`).

v1 loads these files from the mod jar at initialization, **before** `EntityType` registration. Vanilla `DefaultAttributes` is frozen at that point, so `/reload` does not change stats or AI. Restart the game (or dedicated server) after editing JSON.

Entity **classes, renderers, variants, sounds, navigation, and spawn rules** stay in Java.

## JSON shape

```json
{
  "entity": "dwm:broakir",
  "attribute_base": "animal",
  "attributes": {
    "minecraft:max_health": 15.0,
    "minecraft:movement_speed": 0.25
  },
  "goals": [
    { "type": "minecraft:float", "priority": 0 },
    { "type": "minecraft:panic", "priority": 1, "speed": 1.25 }
  ]
}
```

| Field | Required | Meaning |
| --- | --- | --- |
| `entity` | yes | Entity type id; must match the filename |
| `attribute_base` | yes | Vanilla template: `animal`, `mob`, or `monster` |
| `attributes` | yes | Attribute id → base value (overrides the template) |
| `attribute_overrides` | no | Named overlays, e.g. Mewing Dog `"tamed"` max health |
| `goals` | yes | `goalSelector` entries |
| `targets` | no | `targetSelector` entries |

Every goal/target object has `type`, `priority`, and optional `can_use`. Extra fields depend on the type.

## Goal types

`minecraft:` here means “vanilla `Goal` class”, not a vanilla registry.

**Goals:** `minecraft:float`, `panic`, `water_avoiding_random_stroll`, `water_avoiding_random_flying`, `look_at`, `random_look_around`, `open_door`, `ranged_attack`, `melee_attack`, `leap_at_target`, `breed`, `follow_owner`, `sit_when_ordered`, `dwm:dalek_flight`, `dwm:tamable_panic`.

**Targets:** `minecraft:hurt_by_target`, `nearest_attackable_target`, `owner_hurt_by_target`, `owner_hurt_target`, `reset_universal_anger`, `dwm:dalek_share_target`.

**Conditions / filters**

- `can_use`: `{ "type": "dwm:not_flying" }` — Dalek ground stroll while not airborne
- `filter` on nearest-attackable: `{ "type": "dwm:is_angry_at" }` — Mewing Dog persistent anger

`look_at` and `nearest_attackable_target` currently resolve `minecraft:player` only.

Adding a type means a Java codec + factory on `EntityGoal` **and** a matching branch in `src/test/resources/schemas/entity_definition.schema.json`.

## Java API

```java
EntityDefinitions.initialize(); // called from DWMMain and DWMEntityTypes
EntityDefinitions.createAttributes(BroakirEntity.ID);
EntityDefinitions.registerGoals(mob, mob.goalSelector, mob.targetSelector);
EntityDefinitions.applyAttributeOverride(mob, "tamed"); // or EntityDefinitions.DEFAULT_OVERRIDE
```

Missing JSON, id mismatch, unknown attribute ids, and unknown `type` values fail fast at init or apply time.

## Known Constraints

- Restart required after JSON edits (no datapack overlay yet).
- Custom goal classes (`DalekFlightGoal`, inner `TamableAnimalPanicGoal`, …) remain Java; JSON only supplies constructor parameters.
- Seats, lasers, boats, and console hitboxes are not defined here (no mob AI).

## Testing

- Unit: `EntityDefinitionCatalogTest`, `ResourceValidationTests.validateEntityDefinitions`, existing `*EntityTest` DefaultAttributes checks.
- GameTests: existing entity GameTests still cover in-world AI.
