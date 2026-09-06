# Block family colour palettes

Canonical hex role tables and PNG swatches for DWM block/item material families.

Generate or refresh a page from its JSON source (from `dwm/`):

```bash
tools/.venv/bin/python tools/generate_family_palette_docs.py \
  --palette docs/palettes/<family_id>.json \
  --out-dir docs/palettes
```

## Families

- [Gallifrey Stone](./gallifrey_stone.md) — canonical terracotta-brown host (shared by Gallifrey vanilla ores)
- [Zeiton](./zeiton.md) — Gallifrey-stone-like host; emerald/teal veins
- [Azbantium](./azbantium.md) — Gallifrey-stone-like host; silver veins; icy cyan storage block
