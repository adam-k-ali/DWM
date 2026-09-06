# AGENTS.md

## Scope
This file applies to `dwm/tools/palette/` — offline family colour-palette docs and recolour GUI. Not invoked by Gradle or CI.

## Local Context
Writes Markdown + PNG swatches under `dwm/docs/palettes/`. The GUI remaps a stone template onto `host_*` roles for interactive preview.

## Commands (from repo root)

```bash
poetry -C dwm/tools/palette install
poetry -C dwm/tools/palette run generate-family-palette-docs \
  --palette dwm/docs/palettes/<family>.json \
  --out-dir dwm/docs/palettes
poetry -C dwm/tools/palette run generate-family-palette-docs --gui
(cd dwm/tools/palette && poetry run python -m unittest discover -s tests)
```

## Conventions
- Install with Poetry (`virtualenvs.in-project = true` → `palette/.venv/`).
- Package import root is `dwm_palette`.
- Do not commit `palette/.venv/`.

## Common Pitfalls
- The palette GUI needs Tk (`import tkinter`). On Homebrew Python 3.14 install `brew install python-tk@3.14` if `_tkinter` is missing.
- Python version may differ from Java 25; the venv is independent of the Gradle toolchain.
