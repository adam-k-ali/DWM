# AGENTS.md

## Scope
This file applies to `dwm/tools/palette/` — offline family colour-palette docs and recolour GUI. Not invoked by Gradle or CI.

## Local Context
Writes Markdown + PNG swatches under `dwm/docs/palettes/`. Palette JSON is **seed + named contrast profile** (`stone` / `mineral`); step hexes are expanded, not hand-authored.

The GUI remaps:
- **Stone** mode — a stone cube template using the **host** palette slot.
- **Ore** mode — emerald-ore layout (`products.json` → `minecraft:block/emerald_ore.png` from the Loom client jar) using **host** + **mineral** palette slots.
- **Gem** mode — diamond item layout (`products.json` → `minecraft:item/diamond.png`) using the **mineral** slot only; alpha outside the silhouette is preserved.
- **Crystal** mode — quartz item layout (`products.json` → `minecraft:item/quartz.png`) using the **mineral** slot only; alpha outside the silhouette is preserved.
- **Ingot** mode — iron-ingot layout (`products.json` → `minecraft:item/iron_ingot.png`) using the **mineral** slot only; alpha preserved (same remapper as gem/crystal).
- **Pickaxe** / **Sword** / **Shovel** / **Axe** / **Hoe** modes — iron tool layouts using **handle** + **metal** (mineral) slots; handle pixels are classified against vanilla `stick.png` colours (frozen at load), metal pixels take the mineral ramp; alpha preserved.

Products (`dwm/docs/palettes/products.json`) choose archetype, template, and which palette is host / handle / mineral; a palette file itself is not typed as host or family. Gem/crystal/ingot products omit `host` and `handle`. Tool products (pickaxe/sword/shovel/axe/hoe) use `handle` + `mineral` (no `host`).

## Commands (from repo root)

```bash
poetry -C dwm/tools/palette install
poetry -C dwm/tools/palette run generate-family-palette-docs \
  --palette dwm/docs/palettes/<family>.json \
  --out-dir dwm/docs/palettes
poetry -C dwm/tools/palette run generate-family-palette-docs --gui
# Headless product export (gem/crystal/ingot/ore/pickaxe/sword/shovel/axe/hoe):
#   --export-product azbantium --out dwm/src/client/resources/assets/dwm/textures/item/azbantium.png
#   --export-product zeiton_crystals --out dwm/src/client/resources/assets/dwm/textures/item/zeiton_crystals.png
#   --export-product steel_ingot --out dwm/src/client/resources/assets/dwm/textures/item/steel_ingot.png
#   --export-product steel_pickaxe --out dwm/src/client/resources/assets/dwm/textures/item/steel_pickaxe.png
#   --export-product steel_shovel --out dwm/src/client/resources/assets/dwm/textures/item/steel_shovel.png
# Optional ore defaults override:
#   --mineral-palette dwm/docs/palettes/zeiton.json
#   --ore-template dwm/src/client/resources/assets/dwm/textures/block/gallifrey_coal_ore.png
# Optional tool handle override:
#   --handle-palette dwm/docs/palettes/tool_handle.json
(cd dwm/tools/palette && poetry run python -m unittest discover -s tests)
```

## Conventions
- Install with Poetry (`virtualenvs.in-project = true` → `palette/.venv/`).
- Package import root is `dwm_palette`.
- Do not commit `palette/.venv/`.
- Author a **mid seed** and a **profile**; do not list every step hex in JSON.
- Contrast profiles live in `dwm_palette/data/profiles.json` (`stone` = 4 steps, `mineral` = 4 steps: shadow/dark/mid/hi).
- Ore product templates may be DWM paths (`block/….png`) or vanilla study ids (`minecraft:block/emerald_ore.png`). Vanilla ids load from `~/.gradle/caches/fabric-loom/{minecraft_version}/minecraft-client.jar` — **never** commit Mojang PNGs.
- Ore mode classifies host vs mineral pixels against **vanilla `stone.png`** when the template is a `minecraft:…` id (frozen at load), not the live edited host hexes. Filesystem overrides that are already on DWM host hexes classify against the stone cube template.
- Tool modes classify handle vs metal pixels against **vanilla `stick.png`** (frozen at load).
- GUI edits mids only; derived steps are read-only. **Save host/mineral JSON…** writes seed+profile. **Save PNG…** writes the unscaled 16×16 preview. In Pickaxe/Sword modes the host panel is relabelled **handle** and the mineral panel **metal**.

## Common Pitfalls
- The palette GUI needs Tk (`import tkinter`). On Homebrew Python 3.14 install `brew install python-tk@3.14` if `_tkinter` is missing.
- Python version may differ from Java 25; the venv is independent of the Gradle toolchain.
- Nearest-luminance vein mapping would crush dark source greys onto mineral shadow for bright minerals — ore remapping uses **rank** mapping instead.
- Zeiton/Azbantium must not duplicate Gallifrey host hexes; ores reference `gallifrey_stone` via `products.json`.
- Coal-ore layouts have low vein contrast; prefer emerald ore as the mineral product template.
- Steel tool products need both `tool_handle` and `steel` palette JSON files present under `dwm/docs/palettes/`.
