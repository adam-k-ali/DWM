# Minecraft-readable style sheet

Authority: **current vanilla** textures from the Fabric Loom `minecraft-client.jar`
for `minecraft_version` in `dwm/gradle.properties`. Not older DWM art.

## Canvas

| Asset class (this skill) | Size | Alpha |
|--------------------------|------|-------|
| Block face (cube, ore host) | 16×16 | Fully opaque |
| Item (gem, powder, tool) | 16×16 | Transparent outside silhouette |
| Plant cross | 16×16 | Transparent outside silhouette |

Doors, signs, atlases, and entity skins are out of scope here.

## Palette budget

- Target **≤12 opaque RGB colours** per sprite (vanilla ores/gems/tools often land ~7–11).
- Prefer **discrete value steps** (shadow → mid → highlight) over smooth gradients.
- Indexed / low unique-colour look is desirable; avoid photographic noise.

## Edges and shapes

- **Hard edges** only — no soft anti-alias fringe against transparency or neighbouring colours.
- Prefer **1px** contrast steps for silhouette and facet lines.
- Silhouette must read at Minecraft atlas scale (inventory / hotbar).

## Lighting

- Flat cube/host faces: low-contrast noise; **do not** paint a strong global light gradient across the whole tile.
- Volume (gems, tools): mild **top-left lighter / bottom-right darker** cue is OK; keep it to a few value steps, not soft shading.

## Alpha

- Cubes and ore block faces: every pixel opaque (`A=255`).
- Items and plant crosses: transparent unused pixels (`A=0`); no semi-transparent “glow” rings unless matching a specific vanilla exception (default: avoid).

## Authorship preferences

1. Clone a [template](templates/) or a closely related sibling, then recolor.
2. Study the vanilla jar paths listed in [archetypes.md](archetypes.md).
3. Freehand only when no template/archetype fits — still obey this sheet.

## Explicit non-goals

- Do not start a **new** family by copying style from divergent older DWM textures.
- Do not commit Mojang PNGs into the repository.
