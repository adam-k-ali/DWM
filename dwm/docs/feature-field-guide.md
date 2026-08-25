# Feature: DWM Field Guide

See also: [Docs Index](./index.md)

## Product Intent

Give players an always-available in-mod playbook for DWM crafting chains and core interactions without relying on EMI/JEI/REI or memorizing the creative tab.

## Player Outcomes

- Open a curated guide from anywhere in a world (keybind or pause menu).
- Browse Quick Start, Sonic, and Console Room Builder chapters immediately — no progression gate.
- See live recipe lookups for referenced items when playing singleplayer.

## Access

| Method | Default |
| --- | --- |
| Keybind | **G** (`Controls → Doctor Who Mod → Open Field Guide`) |
| Pause menu | **Field Guide** button on the in-game pause screen |

There is no physical guide item and no server packet — the screen is client-only.

## Content (v1)

| Chapter | Topics |
| --- | --- |
| **Quick Start** | Find TARDIS, claim, first hop, bind key |
| **Sonic Toolkit** | Craft sonic, doctor variants, basic use |
| **Console Room Builder** | Chronoplasm, wall, roundels, interior props (white canonical recipes; pattern note for colours) |

Future phases may add Gallifrey building, Azbantium, dimension reference, and chameleon notes.

## Known Constraints

- Recipe panels resolve against the integrated server's recipe manager; multiplayer or missing datapacks show a graceful fallback message.
- The guide lists curated pages, not every colour variant — pattern pages link white recipes and note dye swaps.

## Testing

- Unit: `FieldGuideCatalogTest` validates page ids and recipe JSON references.
- Screenplay: `fieldGuide.yaml` captures PNGs to CI artifacts only (no committed baselines). Review diffs across CI runs when UI changes.

```bash
./gradlew runScreenplay -Pscreenplay=fieldGuide -PscreenplayDisplay=xvfb
```
