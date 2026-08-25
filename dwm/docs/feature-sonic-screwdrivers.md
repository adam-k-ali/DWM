# Feature: Sonic Screwdrivers

See also: [Docs Index](./index.md)

## Product Intent
Give players a recognizable Doctor Who utility tool that feels versatile, tactile, and immediately useful in normal survival play.

## Player Outcomes
- Solve small interaction problems quickly (doors, trapdoors, TNT prep).
- Gain a themed utility option for mobs and utility interactions.
- Progress toward Doctor-themed loadouts through craftable variants.

## Implemented Now
- Craftable sonic variants: Second, Third, Fourth, and Fifth Doctor.
- Shared sonic interaction logic with context-sensitive outcomes.
- Custom sonic use sounds and usage stat tracking.

## How It Works In-Game
1. Craft a sonic screwdriver variant.
2. Use it on supported blocks/entities.
3. The action is resolved based on target context.

Current interactions include:
- Toggle iron doors and iron trapdoors.
- Prime TNT safely.
- Break glass/stained glass without drops.
- Damage slimes.
- Shear sheep.

## Known Constraints
- Interactions are intentionally scoped to a curated set of targets.
- Not every Doctor variant in TARDIS visuals exists as a sonic item yet.
- Behavior is utility-first, not combat-focused.

## Future Opportunities
- Add stronger player feedback for each sonic action type.
- Expand supported interactions while preserving clarity and balance.
