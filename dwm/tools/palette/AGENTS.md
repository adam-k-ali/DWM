# AGENTS.md

## Scope
This file applies to `dwm/tools/palette/` — offline family colour-palette docs and recolour GUI. Not invoked by Gradle or CI.

## Local Context
Writes Markdown + PNG swatches under `dwm/docs/palettes/`. The GUI remaps:
- **Stone** mode — a stone cube template onto `host_*` roles.
- **Ore** mode — `gallifrey_coal_ore.png` (or another ore template) using a **host** palette (`host_*`) plus a **mineral** palette (`vein_*`).

## Commands (from repo root)

```bash
poetry -C dwm/tools/palette install
poetry -C dwm/tools/palette run generate-family-palette-docs \
  --palette dwm/docs/palettes/<family>.json \
  --out-dir dwm/docs/palettes
poetry -C dwm/tools/palette run generate-family-palette-docs --gui
# Optional ore defaults override:
#   --mineral-palette dwm/docs/palettes/azbantium.json
#   --ore-template dwm/src/client/resources/assets/dwm/textures/block/gallifrey_coal_ore.png
(cd dwm/tools/palette && poetry run python -m unittest discover -s tests)
```

## Conventions
- Install with Poetry (`virtualenvs.in-project = true` → `palette/.venv/`).
- Package import root is `dwm_palette`.
- Do not commit `palette/.venv/`.
- Ore mode classifies host vs mineral pixels against the **stone template** unique colours (frozen at load), not the live edited host hexes.
- Mineral UI shows only `vein_*` roles; mineral JSON `host_*` / `gem_*` are ignored for ore remapping.
- **Save PNG…** writes the unscaled 16×16 preview (suggests `{mineral_family_id}_ore.png` in Ore mode).

## Common Pitfalls
- The palette GUI needs Tk (`import tkinter`). On Homebrew Python 3.14 install `brew install python-tk@3.14` if `_tkinter` is missing.
- Python version may differ from Java 25; the venv is independent of the Gradle toolchain.
- Nearest-luminance vein mapping would crush dark coal greys onto `vein_shadow` for bright minerals — ore remapping uses **rank** mapping instead.
