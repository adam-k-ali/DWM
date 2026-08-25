# Feature: DWM Field Guide

See also: [Docs Index](./index.md)

## Product Intent

Give players an always-available in-mod playbook for DWM crafting chains and core interactions without relying on EMI/JEI/REI or memorizing the creative tab.

## Player Outcomes

- Open a curated guide from anywhere in a world (keybind or pause menu).
- Browse Quick Start, Sonic, and Console Room Builder chapters immediately — no progression gate.
- Read pages in an open-book layout with chapter index and page-turn navigation.
- See vanilla-style crafting-table, furnace, and stonecutter recipe previews for referenced recipes.

## Access

| Method | Default |
| --- | --- |
| Keybind | **G** (`Controls → Doctor Who Mod → Open Field Guide`) |
| Pause menu | **Field Guide** button on the in-game pause screen |

There is no physical guide item and no server packet — the screen is client-only.

## Presentation

- **Book shell:** vanilla written-book texture (`textures/gui/book.png`) as an open spread.
- **Left page:** chapter list plus page index for the active chapter.
- **Right page:** page title, body text, page indicator, and recipe preview.
- **Navigation:** Vanilla book page-turn arrows at the spread corners; **Done** sits below the book like a written book.

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

- Unit: `FieldGuideCatalogTest`, `FieldGuideRecipeGridBuilderTest`.
- Screenplay: `fieldGuide.yaml` uses mod-agnostic `pressKey` and widget clicks only; PNGs land in CI artifacts (no committed baselines).

```bash
./gradlew runScreenplay -Pscreenplay=fieldGuide -PscreenplayDisplay=xvfb
```
