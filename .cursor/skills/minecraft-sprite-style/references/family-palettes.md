# Family colour palettes

Define reusable palettes **before** drawing ore / gem / powder / tool sprites
for the same material family so inventory icons read as one set.

Authoring lives under `dwm/docs/palettes/` and the offline palette tool
(`dwm/tools/palette/`). See `dwm/tools/palette/AGENTS.md`.

## Authoring (seed + profile)

A palette JSON is **not** a list of every step hex. It is:

```json
{
  "family_id": "zeiton",
  "display_name": "Zeiton",
  "seed": "#09AF71",
  "profile": "mineral"
}
```

| Field | Meaning |
|-------|---------|
| `seed` | Mid colour (`#RRGGBB`) — the only colour you pick |
| `profile` | Named contrast ramp: `stone` (4 steps) or `mineral` (4 steps: shadow/dark/mid/hi) |

Profiles live in `dwm/tools/palette/dwm_palette/data/profiles.json`. Step hexes
(`shadow` / `dark` / `mid` / `hi`) are **generated** from the seed. Do not
hand-author shadow/hi unless you are changing the shared profile itself.

A palette is just a palette. Usage as **host** or **mineral** is chosen when
making a block (see `dwm/docs/palettes/products.json`), not on the palette file.

```bash
poetry -C dwm/tools/palette run generate-family-palette-docs \
  --palette dwm/docs/palettes/<id>.json \
  --out-dir dwm/docs/palettes
poetry -C dwm/tools/palette run generate-family-palette-docs --gui
```

## Steps when drawing by hand

1. **Pick study refs** from the Loom client jar (1–2 files).
2. **Choose a mid seed** and profile (`stone` for hosts, `mineral` for veins/gems).
3. **Expand** (tool or docs page) to get step hexes.
4. **Map template channels** — greys → host palette steps; accent → mineral palette steps.
5. For ores, use `gallifrey_stone` as host and the mineral palette as veins on the
   emerald-ore template (see `products.json` — `minecraft:block/emerald_ore.png`).

## Usage role names (when drawing)

When talking about pixels on a sprite, prefix the expanded steps by usage:

| Usage name | Typical use |
|------------|-------------|
| `host_shadow` / `host_dark` / `host_mid` / `host_hi` | Host stone noise (from a `stone` palette) |
| `vein_shadow` / `vein_dark` / `vein_mid` / `vein_hi` | Ore mineral clusters (from a `mineral` palette) |
| `gem_shadow` / `gem_dark` / `gem_mid` / `gem_hi` | Gem / crystal / storage (often same mineral palette) |
| `plant_stem` / `plant_leaf` / `plant_leaf_hi` | Cross plants |
| `powder_shadow` / `powder_mid` / `powder_hi` | Dust piles |
| `handle_shadow` / `handle_mid` | Tool handles |
| `metal_shadow` / `metal_mid` / `metal_hi` | Tool heads (often alias mineral) |

These prefixes are **usage labels**, not fields in palette JSON. Ore+gem families
usually need a host palette + a mineral palette. Tools need handle + metal
(metal may reuse the mineral palette).

## Rules

- Reuse the **same palette** (same seed) across ore, gem, powder, and tool head.
- Prefer shifting **value** via the profile over inventing new hues for extra shades.
- Keep the full family within a coherent hue story (one accent hue + neutral host).
- Do not duplicate host hexes inside mineral palette JSON — reference the host
  palette from the product instead.
