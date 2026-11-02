# Feature: Cybermen

See also: [Docs Index](./index.md), [Entity Definitions](./feature-entity-definitions.md), [JSON entity models](./entity-model-json.md), [Dalek](./feature-dalek.md)

## Product Intent
Add the Cybermen as DWM's second hostile antagonist family. There are four types, drawn from different eras, and each has its own look and one signature ability. This pass ships the mobs only. They have no worldgen, structures or natural spawning yet.

## Player Outcomes
- Spawn any of the four types from its spawn egg (Spawn Eggs tab, after the Dalek Mutant) or with `/summon dwm:<id>`.
- All four are hostile. They hunt players they can see, fight back when hurt, and rally nearby Cybermen of the same type.
- Cybermen never hurt each other, whether by melee or by Cybermaster laser.

## Types
| Entity id | Name | Look | Signature ability |
| --- | --- | --- | --- |
| `dwm:cybermondan` | CyberMondan | Original Mondasian design: cloth-wrapped face with eye and mouth holes and a raised nose, steel crown band with a headlamp, cloth suit, chest unit with knobs and tubes, clamped pipes along the arms, bare hands, moon boots | Slow and relentless. Lowest armour, but very hard to knock back |
| `dwm:cybus_cyberman` | Cybus Cyberman | Bulky Cybus Industries steel armour: framed faceplate with teardrop eyes and a mouth slot, chest unit with a raised "C", ribbed abdomen, belt, back pack, two-tier shoulder pads, pistons, black rubber joints, armoured boots | **Electric grip**: every landed melee hit applies Slowness II for 2 seconds, with sparks and a zap sound |
| `dwm:cybermaster` | Cybermaster | Gunmetal and gold Cyberman built from Time Lords: gold-rimmed faceplate and crest, flared Gallifreyan collar framing the head, raised Seal-of-Rassilon boss, gold shoulder caps and sash, wrist-laser gauntlet | **Wrist laser**: ranged attack. **Regeneration**: survives its first killing blow once (see below) |
| `dwm:weapons_grade_cyberman` | Weapons-Grade Cyberman | *Nightmare in Silver* design in satin silver: smooth egg helmet with rounded teardrop eye holes and a low mouth slot, large round ear discs, handlebars meeting a flat top bar with a centre post, layered pauldrons, sternum disc with chevron bands, stacked abdominal plates on a narrow waist, dark ribbed neck, elbows and knees, forearm gun | **Adaptive upgrade** and **super-speed** dashes (see below) |

## Behaviour
| Aspect | Value |
| --- | --- |
| Category | `MobCategory.MONSTER`, 0.6 × 1.95 hitbox, eye height 1.62 |
| Stats | Set in `data/dwm/entity/definition/<id>.json`; see the table below |
| AI (melee types) | Float; melee attack (speed 1.0, keeps following when out of sight); water-avoiding stroll (0.8); look at players within 8 blocks; random look-around |
| AI (Cybermaster) | As above, but with a ranged attack instead of melee: one bolt every 40 ticks, range 16 |
| Targets | Hurt-by-target, alerting others of the same type; nearest visible player |
| Navigation | Ground only. Does not open doors. Floats in water |
| Loot | Empty table |
| Sounds | Shared `entity.cyberman.*` ambient, hurt, death and step sounds (iron-golem events), plus a zap, laser shot, regeneration and upgrade sound, each with a subtitle |

| Type | Health | Speed | Attack | Armour | Knockback resistance | Follow range |
| --- | --- | --- | --- | --- | --- | --- |
| CyberMondan | 24 | 0.20 | 5 | 4 | 0.8 | 24 |
| Cybus Cyberman | 30 | 0.23 | 6 | 8 | 0.6 | 24 |
| Cybermaster | 30 | 0.25 | 5 (laser) | 6 | 0.6 | 32 |
| Weapons-Grade Cyberman | 32 | 0.28 | 6 | 6 | 0.5 | 32 |

CyberMondan and Weapons-Grade Cyberman inherit the Cybus Cyberman AI through `"parent": "dwm:cybus_cyberman"` and override only their attributes.

## Abilities
### Cybus electric grip (`CybusElectricGrip`)
When a Cybus Cyberman's melee hit lands on a living target, the target gets Slowness II for 40 ticks (`SLOWNESS_TICKS`, `SLOWNESS_AMPLIFIER`).

### Cybermaster wrist laser
The Cybermaster fires the same tracked bolt as the Dalek (`DalekLaserEntity`). The bolt deals the Cybermaster's attack damage. It passes through anything in `#dwm:cybermen` when fired by a Cyberman.

### Cybermaster regeneration (`CybermasterRegeneration`)
The first time a hit would kill a Cybermaster, it instead returns to full health. It gains Resistance III for 3 seconds, gives off totem particles and plays the totem sound. Only the hurt sound plays, and the Cybermaster does not die or drop loot.

- It regenerates once. The flag is saved as the NBT boolean `Regenerated`, so a reload does not give it another life.
- Damage that bypasses invulnerability (`/kill`, the void) is never survived.

To spawn a Cybermaster that has already used its regeneration: `/summon dwm:cybermaster ~ ~ ~ {Regenerated:1b}`.

### Weapons-grade adaptive upgrade (`CyberAdaptation`)
- Repeated hits of the same damage type do less damage: −20 % for each previous hit of that type (`STEP`), up to −60 % (`MAX_REDUCTION`).
- The first hit of any type always lands in full.
- A type it hasn't been hit by for 200 ticks (`DECAY_TICKS`) is forgotten.
- Damage types are tracked separately, so switching weapons (say, from sword to arrows) gets past the adaptation.
- Each new upgrade step plays the upgrade sound.
- Damage that bypasses invulnerability is never reduced.

### Weapons-grade super-speed (`CyberSpeedBurst`)
When its target is between 6 and 24 blocks away, it dashes for 30 ticks with +150 % movement speed (a transient `dwm:cyberman_speed_burst` attribute modifier), kicking up a puff of cloud particles. After each dash there is a 120-tick cooldown.

## Spawning
- **No natural or biome spawning.** No biome lists a Cyberman spawner (guarded by `ResourceValidationTests.noBiomeSpawnsCybermen`).
- The spawn placement predicate is vanilla `Monster.checkMonsterSpawnRules`. It only applies to mob spawners; spawn eggs and `/summon` do not use it.

## Entity tag: `#dwm:cybermen`
Generated by `DWMEntityTagProvider` from `DWMEntityTypeTags.CYBERMEN`, it holds all four types. Membership decides Cyberman kinship: Cybermen ignore damage from other members, and a laser fired by a member passes through other members. A datapack can add entity types to the tag.

## Not Included
- Cyber-conversion, Cybermats and Cybermites, Cyber-controllers, flight, or block breaking.
- Fighting Daleks or other factions. Thals do not flee Cybermen.
- Drops or progression materials.
- Natural spawning, structures, or Cyberman worlds (e.g. Mondas, Telos).

## Known Constraints
- Stats and AI are JSON-authored; edits need a game restart, not `/reload` (see [Entity Definitions](./feature-entity-definitions.md)).
- Weapons-grade adaptation and dash state are not saved: they reset when the entity reloads. The Cybermaster `Regenerated` flag is saved.
- The models and skins are original DWM art built on the JSON entity model format (`models/entity/cyberman.json` plus one child per type, all on 128×128 skins). Faces are sculpted from geometry: eyes, teardrops and mouths are real holes in the faceplate over a dark recess, not painted features. They are not converted from the production asset archive; DWM-050's `1966 Cybermen.bbmodel` is still unused.

## Testing
- Unit:
  - `CybermanEntityTest`: registration, hitbox, attributes, spawn eggs, grip constants.
  - `CyberAdaptationTest`, `CyberSpeedBurstTest`, `CybermasterRegenerationTest`.
  - `EntityDefinitionCatalogTest`: shipped stats, AI and inheritance.
  - `EntityModelTexturesTest`: JSON layers.
  - Resource checks in `ResourceValidationTests`: skins (and that each matches its model's atlas size), spawn egg textures, generated spawn egg models, the `cybermen` tag, no biome spawns.
- GameTests (`CybermanGameTests`):
  - Every type matches its definition and is in `#dwm:cybermen`.
  - The Cybus grip slows its target.
  - Cybermen do not hurt each other.
  - The Cybermaster laser hurts a pig but not a Cyberman.
  - The Cybermaster regenerates once and then dies, cannot regenerate from `/kill`, and keeps `Regenerated` through a save and reload.
  - The weapons-grade Cyberman adapts to repeated damage and dashes at a distant target.
- Not covered by a GameTest: acquiring a survival player as a target. The GameTest mock server player is always creative; the target selectors are covered by the definition match.
- Client GameTest: close-up `entities_cybermen` (facing the camera) and `entities_cybermen_angled` (turned 35°, to show the face depth) screenshots of the four types.
