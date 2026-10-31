# Feature: K9

See also: [Docs Index](./index.md), [Entity Definitions](./feature-entity-definitions.md), [Entity Model JSON](./entity-model-json.md)

## Product Intent
Add K-9, the Doctor's robot dog, as a loyal companion the player builds rather than finds.

## Player Outcomes
- Craft a **K-9 Unit** (steel ingots, ferrite powder, redstone, bone): `. B .` / `S R S` / `S F S`.
- Use the unit on a block: a K9 appears next to it, already tamed to you, and the unit is consumed.
- K9 follows you, defends you (attacks what hurts you or what you hit), and never attacks creepers or tamed animals.
- Interact with your K9 to order it to stay put; interact again to let it follow. Other players cannot command it.
- Creative players can also use the **K9 Spawn Egg** (Spawn Eggs tab) or `/summon dwm:k9` (untamed).

## Behaviour
| Aspect | Value |
| --- | --- |
| Entity | `dwm:k9`, `MobCategory.CREATURE`, 0.6 x 0.7 hitbox |
| Stats | 30 max health, 0.35 movement speed, 4 attack damage (`data/dwm/entity/definition/k9.json`) |
| Follow | Starts at 8 blocks, stops at 2 |
| Targets | Owner-hurt-by, owner-hurt, hurt-by (alerts others) |
| Breeding / food | None |
| Spawning | Never spawns naturally |
| Model | Box chassis on hidden wheels (no legs), nodding head, swivelling dish ears, tail antenna |
| Sounds | Iron-golem-style hurt / death (custom sounds are a follow-up) |

## Not Included
Custom sounds, healing/repair mechanics, a dedicated K-9 inventory or commands, and extra laser attack.

## Testing
`K9EntityTest`, `EntityDefinitionCatalogTest.k9MatchesShippedStats`, `ResourceValidationTests.k9AssetsAndGeneratedFilesExist`, `K9GameTests` (unit spawns tamed K9, definition match, sit toggle), and the `entities_misc` client GameTest screenshot.
