# Block family colour palettes

Canonical hex role tables and PNG swatches for DWM block/item material families.

Each palette JSON is a **mid seed** plus a named contrast **profile** (`stone` or `mineral`). Step hexes in the docs are generated.

Generate or refresh a page from its JSON source (from repo root):

```bash
poetry -C dwm/tools/palette run generate-family-palette-docs \
  --palette dwm/docs/palettes/<family_id>.json \
  --out-dir dwm/docs/palettes
```

Preview stone, ore, gem, crystal, ingot, pickaxe, or sword recolour interactively (defaults: Gallifrey stone host + Azbantium mineral on the vanilla emerald-ore layout from [`products.json`](./products.json); tools use `tool_handle` + stick classifier):

```bash
poetry -C dwm/tools/palette run generate-family-palette-docs --gui
```

In the GUI, switch **Stone** / **Ore** / **Gem** / **Crystal** / **Ingot** / **Pickaxe** / **Sword**. Ore mode takes any palette in the **host** slot and any palette in the **mineral** slot and remaps `minecraft:block/emerald_ore.png`. Gem mode remaps `minecraft:item/diamond.png`; Crystal mode remaps `minecraft:item/quartz.png`; Ingot mode remaps `minecraft:item/iron_ingot.png` (mineral only). Pickaxe and Sword modes remap `minecraft:item/iron_pickaxe.png` / `iron_sword.png` with **handle** + **metal** slots (handle pixels classified against vanilla `stick.png`). Edit **mids** only; contrast comes from the profile. Use **Save PNG…** to export the 16×16 result.

## Palettes

- [Gallifrey Stone](./gallifrey_stone.md) — terracotta-brown host (`profile: stone`)
- [Zeiton](./zeiton.md) — emerald/teal mineral (`profile: mineral`)
- [Azbantium](./azbantium.md) — icy cyan mineral (`profile: mineral`)
- [Steel](./steel.md) — dark silvery-grey metal (`profile: mineral`)
- [Tool Handle](./tool_handle.md) — oak-stick wood browns for tool handles (`profile: stone`)

## Products

Blocks and items pick palettes in [`products.json`](./products.json):

| Product | Archetype | Host / Handle | Mineral | Template |
|---------|-----------|---------------|---------|----------|
| `zeiton_ore` | ore | host: `gallifrey_stone` | `zeiton` | `minecraft:block/emerald_ore.png` |
| `azbantium_ore` | ore | host: `gallifrey_stone` | `azbantium` | `minecraft:block/emerald_ore.png` |
| `azbantium` | gem | — | `azbantium` | `minecraft:item/diamond.png` |
| `zeiton_crystals` | crystal | — | `zeiton` | `minecraft:item/quartz.png` |
| `steel_ingot` | ingot | — | `steel` | `minecraft:item/iron_ingot.png` |
| `steel_pickaxe` | pickaxe | handle: `tool_handle` | `steel` | `minecraft:item/iron_pickaxe.png` |
| `steel_sword` | sword | handle: `tool_handle` | `steel` | `minecraft:item/iron_sword.png` |
| `steel_shovel` | shovel | handle: `tool_handle` | `steel` | `minecraft:item/iron_shovel.png` |
| `steel_axe` | axe | handle: `tool_handle` | `steel` | `minecraft:item/iron_axe.png` |
| `steel_hoe` | hoe | handle: `tool_handle` | `steel` | `minecraft:item/iron_hoe.png` |
