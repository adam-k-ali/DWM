---
name: minecraft-sprite-style
description: >-
  Create Minecraft-readable 16×16 block and item sprites for DWM using vanilla
  (current minecraft_version) style, not older DWM art. Use when asked to draw,
  generate, recolor, or style textures/sprites for blocks, ores, gems, plants,
  powders, or tools: family palettes, archetype recipes, and PNG templates.
---

# Minecraft Sprite Style (DWM)

## Keywords

sprite, texture, pixel art, block texture, item texture, ore, gem, plant cross,
powder, dust, pickaxe, sword, palette, Minecraft-readable, vanilla style

## Overview

Author **16×16** block/item PNGs that read clearly in Minecraft’s atlas, matching
**current vanilla** readability (see `minecraft_version` in
[`dwm/gradle.properties`](../../../dwm/gradle.properties)). Existing DWM textures
often reflect an older Minecraft era — do **not** treat them as style authority
when starting a **new** family.

**Output:** PNG(s) under
`dwm/src/client/resources/assets/dwm/textures/{block,item}/` (and related paths
only when the task explicitly needs them). This skill covers **style and
drawing**, not registry/datagen wiring.

**Read references on demand:**

| Reference | When |
|-----------|------|
| [minecraft-readable.md](references/minecraft-readable.md) | Style sheet (size, palette budget, edges, alpha, lighting) |
| [family-palettes.md](references/family-palettes.md) | Build a reusable colour roles table for a new family |
| [archetypes.md](references/archetypes.md) | Ore, gem, plant, powder, tool recipes + vanilla study paths |
| [templates/](references/templates/) | Original layout guides to clone and recolor |

## Hard constraints

| Allowed | Forbidden |
|---------|-----------|
| Study vanilla PNGs from the Loom client jar (local extract) | Commit Mojang/vanilla PNGs into the repo |
| Clone/recolor skill templates or sibling DWM textures **after** applying vanilla-readable rules | Soft blur, anti-alias gradients, photographic shading |
| ≤12 opaque colours per sprite; hard 1px edges | Freehand invention when a template/archetype fits |
| Transparent corners on items/plants | Opaque cube/ore faces with holes or soft fringe |
| Neutral greys + accent remap on templates | Copying pixels from divergent older DWM art as the style source for new families |

**Legal:** Templates under `references/templates/` are **original** guides, not
Mojang assets. Vanilla files are for **study only** (Mojang EULA).

## Study vanilla (runtime)

Resolve the jar from `minecraft_version` in `dwm/gradle.properties` (e.g. `26.2`):

```bash
MC_VER=$(grep -E '^minecraft_version=' dwm/gradle.properties | cut -d= -f2)
JAR="$HOME/.gradle/caches/fabric-loom/${MC_VER}/minecraft-client.jar"
jar tf "$JAR" | grep 'assets/minecraft/textures/block/diamond_ore.png'
mkdir -p /tmp/mc-vanilla-study && cd /tmp/mc-vanilla-study
jar xf "$JAR" assets/minecraft/textures/block/diamond_ore.png
```

Open extracted PNGs for structure and value steps. **Do not** copy them into
`dwm/` or this skill.

## Workflow

1. **Identify archetype** — cube host, ore-in-stone, gem/item, plant cross, powder/dust, pickaxe, or sword ([archetypes.md](references/archetypes.md)).
2. **Build a family palette** — named roles + hex table ([family-palettes.md](references/family-palettes.md)) before drawing multiple related sprites.
3. **Read the style sheet** — [minecraft-readable.md](references/minecraft-readable.md).
4. **Start from a template** — copy the matching file from [templates/](references/templates/); remap greys → host roles, accent → mineral/metal roles; edit motif lightly.
5. **Study 1–2 vanilla refs** from the jar paths listed for that archetype.
6. **Write PNG(s)** to the correct client texture path (`snake_case` id = filename).
7. **Self-check** — 16×16; opaque cubes/ores fully opaque; items/plants use alpha; unique opaque RGB count ≤12; silhouette readable at atlas scale.

## Out of scope

- Datagen / blockstate / model JSON wiring
- Doors, signs, UV atlases, entity skins, GUI widgets (non-16×16 or multi-face layouts)
- Blockbench meshes
- Restyling the entire existing DWM texture set
