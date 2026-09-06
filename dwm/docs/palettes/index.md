# Block family colour palettes

Canonical hex role tables and PNG swatches for DWM block/item material families.

Generate or refresh a page from its JSON source (from repo root):

```bash
poetry -C dwm/tools/palette run generate-family-palette-docs \
  --palette dwm/docs/palettes/<family_id>.json \
  --out-dir dwm/docs/palettes
```

Preview stone recolour interactively (defaults to Gallifrey stone palette + texture):

```bash
poetry -C dwm/tools/palette run generate-family-palette-docs --gui
```

## Families

- [Gallifrey Stone](./gallifrey_stone.md) — canonical terracotta-brown host (shared by Gallifrey vanilla ores)
- [Zeiton](./zeiton.md) — Gallifrey-stone-like host; emerald/teal veins
- [Azbantium](./azbantium.md) — Gallifrey-stone-like host; silver veins; icy cyan storage block
