# Feature: DWM Field Guide

See also: [Docs Index](./index.md)

## Product Intent

Give players an always-available in-mod playbook for DWM crafting chains and core interactions without relying on EMI/JEI/REI or memorizing the creative tab.

## Player Outcomes

- Open a curated guide from anywhere in a world (keybind, first-join book, or Mod Menu).
- Browse Quick Start, Sonic, Console Circuits, and Console Room Builder chapters immediately — no progression gate.
- Read pages in an open-book layout with chapter index and page-turn navigation.
- See vanilla-style crafting-table, furnace, and stonecutter recipe previews for referenced recipes.

## Access

| Method | Default |
| --- | --- |
| Keybind | **G** (`Controls → Doctor Who Mod → Open Field Guide`) |
| Item | **Field Guide** book given once on first join (use in air). Losing it is expected — G and Mod Menu remain. |
| Mod Menu | **Mods → The Doctor Who Mod → Open Field Guide** (requires a loaded world) |

The screen is client-only; catalog content is loaded from datapacks and synced with the world.

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
| **Console Circuits** | Install ritual, landing kit, planet locator, late circuits (vanilla + Zeiton) |
| **Console Room Builder** | Chronoplasm, wall, roundels (A/B/Big on one page), interior props (white canonical recipes; pattern note for colours) |

Future phases may add Gallifrey building, Azbantium, dimension reference, and chameleon notes.

## Datapack layout

The catalog is three synced dynamic registries. Registry keys use the vanilla `minecraft` namespace so Fabric does not insert an extra registry-namespace folder. File path is the entry id (`data/dwm/guide/page/find_tardis.json` → `dwm:find_tardis`). Nested `id` fields must match the file id.

| Registry | Folder | Built-in entry |
| --- | --- | --- |
| `minecraft:guide/book` | `data/<ns>/guide/book/` | `dwm:field_guide` |
| `minecraft:guide/chapter` | `data/<ns>/guide/chapter/` | `dwm:quick_start`, `dwm:sonic`, `dwm:circuits`, `dwm:console_room` |
| `minecraft:guide/page` | `data/<ns>/guide/page/` | one JSON per page |

**Book**

```json
{
  "guide": { "id": "dwm:field_guide" },
  "chapters": [
    { "id": "dwm:quick_start" },
    { "id": "dwm:sonic" },
    { "id": "dwm:circuits" },
    { "id": "dwm:console_room" }
  ]
}
```

**Chapter**

```json
{
  "chapter": { "id": "dwm:quick_start" },
  "titleKey": "dwm.guide.chapter.quick_start",
  "pages": [
    { "id": "dwm:find_tardis" },
    { "id": "dwm:claim_tardis" }
  ]
}
```

**Page** — exactly one `text` block, plus optional recipe blocks (`crafting`, `smelting`, `stonecutting`). `pattern: true` shows the colour-swap footnote.

```json
{
  "page": { "id": "dwm:chronoplasm" },
  "content": [
    {
      "type": "text",
      "titleKey": "dwm.guide.page.chronoplasm.title",
      "bodyKey": "dwm.guide.page.chronoplasm.body"
    },
    {
      "type": "crafting",
      "recipes": ["dwm:white_chronoplasm_powder"],
      "pattern": true
    }
  ]
}
```

`/reload` rebuilds the registries. Other datapacks replace entries by id (replacing `dwm:field_guide` replaces the chapter list). Lang strings remain in the language provider / `en_us.json`.

## Known Constraints

- Recipe panels resolve against the integrated server's recipe manager; multiplayer or missing datapacks show a graceful fallback message.
- The guide lists curated pages, not every colour variant — pattern pages link white recipes and note dye swaps. Roundel A, B, and Big share one page with a crafting-variant selector.

## Testing

- Unit: `FieldGuideCatalogTest`, `FieldGuideRecipeGridBuilderTest`.
- Screenplay: `fieldGuide.yaml` uses mod-agnostic `pressKey` and widget clicks only; PNGs land in CI artifacts (no committed baselines).

```bash
./gradlew runScreenplay -Pscreenplay=fieldGuide -PscreenplayDisplay=xvfb
```
