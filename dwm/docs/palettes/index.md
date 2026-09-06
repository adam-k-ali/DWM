# Block family colour palettes

Canonical hex role tables and PNG swatches for DWM block/item material families.

Generate or refresh a page from its JSON source (from repo root):

```bash
poetry -C dwm/tools/palette run generate-family-palette-docs \
  --palette dwm/docs/palettes/<family_id>.json \
  --out-dir dwm/docs/palettes
```

Preview stone or ore recolour interactively (defaults: Gallifrey stone host + Azbantium mineral veins on the Gallifrey coal-ore layout):

```bash
poetry -C dwm/tools/palette run generate-family-palette-docs --gui
```

In the GUI, switch **Stone** / **Ore**. Ore mode takes a host palette JSON (`host_*`) and a mineral palette JSON (`vein_*`) and remaps `gallifrey_coal_ore.png`. Use **Save PNG…** to export the 16×16 result.

## Families

- [Gallifrey Stone](./gallifrey_stone.md) — canonical terracotta-brown host (shared by Gallifrey vanilla ores)
- [Zeiton](./zeiton.md) — Gallifrey-stone-like host; emerald/teal veins
- [Azbantium](./azbantium.md) — Gallifrey-stone-like host; silver veins; icy cyan storage block
