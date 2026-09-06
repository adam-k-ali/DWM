# AGENTS.md

## Scope
This file applies to `dwm/tools/palette/` — offline family colour-palette docs and recolour GUI. Not invoked by Gradle or CI.

## Local Context
Writes Markdown + PNG swatches under `dwm/docs/palettes/`. Palette JSON is **seed + named contrast profile** (`stone` / `mineral`); step hexes are expanded, not hand-authored.

The GUI remaps:
- **Stone** mode — a stone cube template using the **host** palette slot.
- **Ore** mode — coal-ore template (`products.json`) using **host** + **mineral** palette slots.

Products (`dwm/docs/palettes/products.json`) choose which palette is host vs mineral for each block; a palette file itself is not typed as host or family.

## Commands (from repo root)

```bash
poetry -C dwm/tools/palette install
poetry -C dwm/tools/palette run generate-family-palette-docs \
  --palette dwm/docs/palettes/<family>.json \
  --out-dir dwm/docs/palettes
poetry -C dwm/tools/palette run generate-family-palette-docs --gui
# Optional ore defaults override:
#   --mineral-palette dwm/docs/palettes/zeiton.json
#   --ore-template dwm/src/client/resources/assets/dwm/textures/block/gallifrey_coal_ore.png
(cd dwm/tools/palette && poetry run python -m unittest discover -s tests)
```

## Conventions
- Install with Poetry (`virtualenvs.in-project = true` → `palette/.venv/`).
- Package import root is `dwm_palette`.
- Do not commit `palette/.venv/`.
- Author a **mid seed** and a **profile**; do not list every step hex in JSON.
- Contrast profiles live in `dwm_palette/data/profiles.json` (`stone` = 4 steps, `mineral` = 3).
- Ore mode classifies host vs mineral pixels against the **stone template** unique colours (frozen at load), not the live edited host hexes.
- GUI edits mids only; derived steps are read-only. **Save host/mineral JSON…** writes seed+profile. **Save PNG…** writes the unscaled 16×16 preview.

## Common Pitfalls
- The palette GUI needs Tk (`import tkinter`). On Homebrew Python 3.14 install `brew install python-tk@3.14` if `_tkinter` is missing.
- Python version may differ from Java 25; the venv is independent of the Gradle toolchain.
- Nearest-luminance vein mapping would crush dark coal greys onto mineral shadow for bright minerals — ore remapping uses **rank** mapping instead.
- Zeiton/Azbantium must not duplicate Gallifrey host hexes; ores reference `gallifrey_stone` via `products.json`.
