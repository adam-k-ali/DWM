# Family colour palettes

Define a small reusable palette **before** drawing ore / gem / powder / tool sprites
for the same material family so inventory icons read as one set.

## Steps

1. **Pick study refs** from the Loom client jar (1–2 files). Examples:
   - Stone-hosted gem family → `textures/block/stone.png` + `textures/block/diamond_ore.png` + `textures/item/diamond.png`
   - Metal tool family → `textures/item/iron_ingot.png` + `textures/item/iron_pickaxe.png`
2. **Extract ~4–8 colours** as discrete RGB hex values (shadow / mid / highlight / accent). Ignore near-duplicates.
3. **Assign roles** (names below). Keep the same role names across the family.
4. **Map template channels** — skill templates use **host greys** + **one accent** (see [templates/README.md](templates/README.md)). Remap greys → host roles; accent → mineral/metal/plant roles.
5. **Record the table** in the PR description (or a short note next to the change) when adding a family.

## Role catalogue

| Role | Typical use |
|------|-------------|
| `host_shadow` | Darkest host stone / deepslate noise |
| `host_mid` | Primary host fill |
| `host_hi` | Host highlight flecks |
| `vein_shadow` | Dark edge of ore clusters |
| `vein_mid` | Main mineral / ore fill |
| `vein_hi` | Specular flecks on ore |
| `gem_shadow` | Gem / crystal dark facet |
| `gem_mid` | Gem body |
| `gem_hi` | Gem highlight |
| `plant_stem` | Stem / wood of cross plants |
| `plant_leaf` | Foliage mid |
| `plant_leaf_hi` | Foliage tip / light |
| `powder_shadow` | Dust pile dark |
| `powder_mid` | Dust body |
| `powder_hi` | Dust highlight grains |
| `handle_shadow` | Tool handle dark wood |
| `handle_mid` | Tool handle mid |
| `metal_shadow` | Tool head / blade dark |
| `metal_mid` | Tool head / blade mid |
| `metal_hi` | Tool head / blade highlight |

Not every family needs every role. Ore+gem families usually need `host_*` + `vein_*` + `gem_*`. Tools need `handle_*` + `metal_*` (metal may alias `vein_*` / `gem_*` if the head uses the same material).

## Table format

```markdown
### Palette — <family_id>

| Role | Hex | Notes |
|------|-----|-------|
| host_shadow | #585858 | from vanilla stone study |
| host_mid | #7F7F7F | |
| host_hi | #8F8F8F | |
| vein_mid | #8DADB1 | family accent |
| vein_hi | #B8E8E0 | |
| gem_mid | #20C5B5 | shared with vein family |
| gem_hi | #A1FBE8 | |
```

## Rules

- Reuse the **same hex** for the same role across ore, gem, powder, and tool head.
- Prefer shifting **value** (darker/lighter) over introducing new hues for “extra” shades.
- Keep the full family within a coherent hue story (one accent hue + neutral host).
