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

- **Catalog shell:** a wide, two-page parchment spread with clear hierarchy and generous margins.
- **Left page:** persistent chapter catalog plus the active chapter's topic index.
- **Right page:** chapter context, topic title, concise guidance, and a readable station recipe diagram. Pages with multiple crafting variants show selectable result icons.
- **Navigation:** book page-turn arrows move within a chapter; direct topic links support catalog lookup; **Done** closes the guide.

## Content (v1)

| Chapter | Topics |
| --- | --- |
| **Quick Start** | Find TARDIS, claim, first hop, bind key |
| **Sonic Toolkit** | Craft sonic, doctor variants, basic use |
| **Console Room Builder** | Chronoplasm, wall, roundels (A/B/Big on one page), interior props (white canonical recipes; pattern note for colours) |

Future phases may add Gallifrey building, Azbantium, dimension reference, and chameleon notes.

## Known Constraints

- Recipe panels resolve against the integrated server's recipe manager; multiplayer or missing datapacks show a graceful fallback message.
- The guide lists curated pages, not every colour variant — pattern pages link white recipes and note dye swaps. Roundel A, B, and Big share one page with a crafting-variant selector.

## Testing

- Unit: `FieldGuideCatalogTest`, `FieldGuideRecipeGridBuilderTest`.
- Screenplay: `fieldGuide.yaml` uses mod-agnostic `pressKey` and widget clicks only; PNGs land in CI artifacts (no committed baselines).

```bash
./gradlew runScreenplay -Pscreenplay=fieldGuide -PscreenplayDisplay=xvfb
```
