# Sprite archetypes

Jar paths are under `assets/minecraft/` inside the Loom `minecraft-client.jar`
for the current `minecraft_version`. Extract for study only — do not commit them.

For each archetype: clone the listed template, apply the family palette, then
nudge motif while keeping silhouette and value structure.

---

## Cube / host stone

| | |
|--|--|
| **Template** | [templates/cube_block.png](templates/cube_block.png) |
| **Vanilla study** | `textures/block/stone.png`, `textures/block/deepslate.png` |
| **Size / alpha** | 16×16, fully opaque |
| **Palette roles** | `host_shadow`, `host_mid`, `host_hi` |

**Do:** Low-contrast noise with 3–5 values; tileable-ish face; host remains readable alone.

**Don’t:** Strong diagonal light wash; high-contrast “busy” speckles that fight ore overlays; transparency.

**Recolor:** Map template greys → `host_*` only (no accent on this template).

---

## Ore-in-stone

| | |
|--|--|
| **Template** | [templates/ore_stone.png](templates/ore_stone.png) |
| **Vanilla study** | `textures/block/coal_ore.png`, `iron_ore.png`, `gold_ore.png`, `diamond_ore.png` |
| **Size / alpha** | 16×16, fully opaque |
| **Palette roles** | `host_*` + `vein_shadow`, `vein_mid`, `vein_hi` |

**Do:** Sparse mineral **clusters** on a readable host; keep most pixels as host; vein blobs have a dark rim + mid fill + rare hi flecks.

**Don’t:** Cover the face in mineral; soft glow around veins; transparent pixels.

**Recolor:** Greys → `host_*`; accent pixels → `vein_*`.

---

## Gem / crystal item

| | |
|--|--|
| **Template** | [templates/gem_item.png](templates/gem_item.png) |
| **Vanilla study** | `textures/item/diamond.png`, `textures/item/emerald.png` |
| **Size / alpha** | 16×16, transparent outside silhouette |
| **Palette roles** | `gem_shadow`, `gem_mid`, `gem_hi` (optionally share hue with `vein_*`) |

**Do:** Faceted silhouette; strong center / upper highlight; clear outline; shared family hue with the ore.

**Don’t:** Round soft-shaded spheres; opaque full-canvas square; rainbow noise.

**Recolor:** Accent → `gem_*`; leave transparent pixels untouched.

---

## Plant cross

| | |
|--|--|
| **Template** | [templates/plant_cross.png](templates/plant_cross.png) |
| **Vanilla study** | `textures/block/short_grass.png`, `oak_sapling.png`, `poppy.png` |
| **Size / alpha** | 16×16, transparent background |
| **Palette roles** | `plant_stem`, `plant_leaf`, `plant_leaf_hi` |

**Do:** Centered upright silhouette suitable for the cross model; readable outline; limited greens/browns.

**Don’t:** Opaque cube plants; ground contact shadow blobs; dense fill that reads as a block face.

**Recolor:** Stem greys → `plant_stem`; accent foliage → `plant_leaf` / `plant_leaf_hi`.

---

## Powder / dust

| | |
|--|--|
| **Template** | [templates/powder_dust.png](templates/powder_dust.png) |
| **Vanilla study** | `textures/item/gunpowder.png`, `sugar.png`, `redstone.png` |
| **Size / alpha** | 16×16, transparent outside pile |
| **Palette roles** | `powder_shadow`, `powder_mid`, `powder_hi` |

**Do:** Mass weighted to the **lower half**; clumpy scatter; same family hue as ore/gem when it is ground material.

**Don’t:** Faceted gem shapes; centered “orb”; soft airbrush clouds.

**Recolor:** Greys/accent pile → `powder_*`.

---

## Tool — pickaxe

| | |
|--|--|
| **Template** | [templates/tool_pickaxe.png](templates/tool_pickaxe.png) |
| **Vanilla study** | `textures/item/iron_pickaxe.png` |
| **Size / alpha** | 16×16, transparent outside silhouette |
| **Palette roles** | `handle_shadow`, `handle_mid`, `metal_shadow`, `metal_mid`, `metal_hi` |

**Do:** Diagonal handle + distinct head; clear wood vs metal separation; readable hotbar silhouette.

**Don’t:** Symmetric centered icons; merging handle into head colour; thick AA outlines.

**Recolor:** Brown/grey handle pixels → `handle_*`; accent head → `metal_*` (often same as `vein_*` / `gem_*`).

---

## Tool — sword

| | |
|--|--|
| **Template** | [templates/tool_sword.png](templates/tool_sword.png) |
| **Vanilla study** | `textures/item/diamond_sword.png` |
| **Size / alpha** | 16×16, transparent outside silhouette |
| **Palette roles** | `handle_*` + `metal_*` (blade) |

**Do:** Blade + guard + handle; sharp silhouette; blade uses family metal/gem colours.

**Don’t:** Soft glow blade; oversized guard; opaque canvas.

**Recolor:** Handle → `handle_*`; blade/guard accent → `metal_*`.
