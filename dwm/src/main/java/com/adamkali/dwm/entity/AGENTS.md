# AGENTS.md

## Scope
This file applies to `com.adamkali.dwm.entity` and `com.adamkali.dwm.entity.definition` — living mobs, projectiles, seats, and the JSON attribute/AI overlay.

## Local Context
Product behaviour for fauna is documented in `docs/feature-gallifrey-dimension.md`, `docs/feature-dalek.md`, and `docs/feature-skaro-dimension.md`. Authoring stats and AI goals is documented in `docs/feature-entity-definitions.md`.

### Attributes and AI
Do **not** hardcode `createAttributes()` values or `registerGoals()` lists on living mobs. Edit `src/main/resources/data/dwm/entity/definition/<id>.json` and the matching JSON Schema branch if you add a goal `type`. Entity classes keep a one-liner:

```java
EntityDefinitions.createAttributes(ID);
EntityDefinitions.registerGoals(this, this.goalSelector, this.targetSelector);
```

JSON is loaded from the jar at init (`EntityDefinitions.initialize()`). `/reload` does not apply. Navigation, variants, sounds, spawn rules, and renderers stay in Java.

### Package layout
| Type | Responsibility |
|------|----------------|
| `*Entity` | Entity class: interactions, NBT, sounds, navigation, variants |
| `definition/` | Codec, loader, apply API (`EntityDefinitions`, `EntityGoal`) |
| `DWMEntityTypes` | `EntityType` registration + spawn placement |
| Client `DWMEntityRenderers` | Renderers only |

## Commands
- Unit tests: `./dwm/gradlew test --tests "com.adamkali.dwm.entity.*"`
- GameTests: `./dwm/gradlew runGametest` (Broakir, Flutterwing, MewingDog, TimeLord, Dalek)
- Schema: `ResourceValidationTests.validateEntityDefinitions`

## Conventions
- New living mobs: register the type in `DWMEntityTypes`, add `data/dwm/entity/definition/<id>.json`, keep Java for class-specific behaviour.
- Custom `Goal` subclasses stay in Java; JSON only supplies constructor parameters and optional `can_use` / `filter`.
- `look_at` / `nearest_attackable_target` currently support `minecraft:player` only.

## Common Pitfalls
- Changing JSON requires a process restart, not `/reload`.
- Goal types in the `goals` array must be `Selector.GOAL`; target types belong in `targets`.
- `dwm:dalek_flight`, `dwm:dalek_share_target`, and `dwm:not_flying` require `DalekEntity`.
- `dwm:tamable_panic`, sit/follow/owner-target goals require `TamableAnimal`.
