# Block family colour palettes

Canonical hex role tables and PNG swatches for DWM block/item material families.

Each palette JSON is a **mid seed** plus a named contrast **profile** (`stone` or `mineral`). Step hexes in the docs are generated.

Generate or refresh a page from its JSON source (from repo root):

```bash
poetry -C dwm/tools/palette run generate-family-palette-docs \
  --palette dwm/docs/palettes/<family_id>.json \
  --out-dir dwm/docs/palettes
```

Preview stone, ore, gem, or crystal recolour interactively (defaults: Gallifrey stone host + Azbantium mineral on the vanilla emerald-ore layout from [`products.json`](./products.json)):

```bash
poetry -C dwm/tools/palette run generate-family-palette-docs --gui
```

In the GUI, switch **Stone** / **Ore** / **Gem** / **Crystal**. Ore mode takes any palette in the **host** slot and any palette in the **mineral** slot and remaps `minecraft:block/emerald_ore.png`. Gem mode remaps `minecraft:item/diamond.png`; Crystal mode remaps `minecraft:item/quartz.png` (both from the Loom client jar; not committed) using the mineral slot only, preserving transparency. Edit **mids** only; contrast comes from the profile. Use **Save PNG…** to export the 16×16 result.

## Palettes

- [Gallifrey Stone](./gallifrey_stone.md) — terracotta-brown host (`profile: stone`)
- [Zeiton](./zeiton.md) — emerald/teal mineral (`profile: mineral`)
- [Azbantium](./azbantium.md) — icy cyan mineral (`profile: mineral`)

## Products

Blocks and items pick palettes in [`products.json`](./products.json):

| Product | Archetype | Host | Mineral | Template |
|---------|-----------|------|---------|----------|
| `zeiton_ore` | ore | `gallifrey_stone` | `zeiton` | `minecraft:block/emerald_ore.png` |
| `azbantium_ore` | ore | `gallifrey_stone` | `azbantium` | `minecraft:block/emerald_ore.png` |
| `azbantium` | gem | — | `azbantium` | `minecraft:item/diamond.png` |
| `zeiton_crystals` | crystal | — | `zeiton` | `minecraft:item/quartz.png` |
