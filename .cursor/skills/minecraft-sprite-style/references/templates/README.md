# Sprite templates (original)

These PNGs are **original layout guides** for agents. They are **not** Mojang
vanilla textures and must not be replaced with extracted jar assets.

All files are **16×16** RGBA.

| File | Archetype | Opaque? |
|------|-----------|---------|
| `cube_block.png` | Host / cube face | Fully opaque |
| `ore_stone.png` | Ore-in-stone | Fully opaque |
| `gem_item.png` | Gem / crystal item | Transparent outside silhouette |
| `plant_cross.png` | Plant cross | Transparent outside silhouette |
| `powder_dust.png` | Powder / dust item | Transparent outside pile |
| `tool_pickaxe.png` | Pickaxe | Transparent outside silhouette |
| `tool_sword.png` | Sword | Transparent outside silhouette |

## Colour channels (recolor map)

| Channel | RGB (approx) | Remap to |
|---------|--------------|----------|
| Host greys | `#404040` … `#A0A0A0` | `host_shadow` / `host_mid` / `host_hi` |
| Deep rim / outline | `#303030` | darkest outline or `host_shadow` |
| Accent magenta | `#B428A0`, `#DC46C8`, `#FF8CE6` | `vein_*`, `gem_*`, `plant_leaf*`, `powder_*`, or `metal_*` |
| Handle browns | `#48341C`, `#684C28` | `handle_shadow` / `handle_mid` |

## How to use

1. Copy the matching template to the target path under
   `dwm/src/client/resources/assets/dwm/textures/…`.
2. Remap channels using the family palette table from
   [family-palettes.md](../family-palettes.md).
3. Edit motif lightly (cluster positions, leaf tips) while keeping silhouette
   and value structure.
4. Confirm rules in [minecraft-readable.md](../minecraft-readable.md).
