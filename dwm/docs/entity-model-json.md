# JSON entity models

See also: [Docs Index](./index.md)

DWM entity and block-entity meshes can be authored as JSON under `assets/<namespace>/models/entity/` instead of `CubeListBuilder` trees in Java. The client compiles each file to a vanilla `LayerDefinition`.

This is **not** vanilla block/item JSON (no bone tree, per-face UV) and **not** Bedrock `geo.json`. Cubes use Minecraft Java `texOffs` unwrap.

## Resource path

| Layer id | File |
|----------|------|
| `dwm:dalek_laser` | `assets/dwm/models/entity/dalek_laser.json` |
| `dwm:first_doctor_box` … `dwm:seventh_doctor_box`, `dwm:tt_capsule`, `dwm:tardis_classic_interior_door` (TARDIS exteriors and interior door) | `assets/dwm/models/entity/<id>.json` |
| `dwm:entity/console_selector` (parent form) | `assets/dwm/models/entity/console_selector.json` |

The layer id (`ModelLayerLocation`) is defined in Java. New meshes still need one Java registration line; resource packs can override the JSON of registered ids. A missing or malformed file fails the model reload with an error naming the model and resource; there is no built-in fallback.

## File shape

```json
{
  "parent": "dwm:entity/console_selector",
  "texture_width": 16,
  "texture_height": 16,
  "texture": "dwm:textures/entity/tardis_globe.png",
  "parts": [
    {
      "name": "arrow",
      "pivot": [0, 24.5, 0.5],
      "rotation": [0, 0, -45],
      "cubes": [
        { "uv": [12, 8], "origin": [-1.914, -7.086, -0.5], "size": [1, 4, 0.01], "inflate": 0, "mirror": false }
      ],
      "children": []
    }
  ]
}
```

| Field | Maps to |
|-------|---------|
| `texture_width` / `texture_height` | `LayerDefinition.create(mesh, w, h)` |
| `texture` | Atlas (`Identifier` form, including `textures/` and `.png`). Authoritative: required on the resolved (parent-merged) model, recorded in `EntityModelTextures` when the layer loads, and read by renderers via `EntityModelTextures.get(LAYER_LOCATION)`. A resource pack that overrides the JSON retargets the texture. |
| `parts[].name` | `addOrReplaceChild` (keep names used by animation and tests); must be unique among siblings, otherwise the file fails to parse |
| `pivot` | `PartPose` translation |
| `rotation` | Rest pose in **degrees**; converted to radians at bake |
| `cubes[].uv` | `texOffs(u, v)` |
| `origin` + `size` | `addBox(x, y, z, w, h, d)` |
| `inflate` | `CubeDeformation` (negative allowed) |
| `mirror` | `CubeListBuilder.mirror()` |
| `parent` | Copy parent, then overlay `texture` / texture size; child `parts` replace same-named parent parts and append new names (an empty or absent `parts` inherits the parent tree) |

| `animation.bindings` | Per-frame pose changes, see [Animation](#animation). Merged through `parent` by part + channel |

A resolved model must have at least one part. A file must have either `parent` or `texture_width` + `texture_height` + `parts`. Schema: `src/test/resources/schemas/entity_model.schema.json`.

## Animation

```json
"animation": {
  "bindings": [
    { "part": "neck/head", "channel": "rot_x", "value": "head_pitch" },
    { "part": "leg1", "channel": "rot_x", "value": "cos(walk_pos * 0.6662) * 1.4 * walk_speed * deg" },
    { "part": "tail", "channel": "rot_x", "value": "cos(age * 0.05) * 0.1 * deg" }
  ]
}
```

Each frame vanilla resets every part to its rest pose, then each binding **adds** its value to one channel. Several bindings on the same part and channel sum.

| Field | Meaning |
|-------|---------|
| `part` | Slash path from the root (`neck/head`). Omit to drive the root part. An unknown path fails when the model is built. |
| `channel` | `rot_x` `rot_y` `rot_z` (**degrees**, like rest `rotation`) or `pos_x` `pos_y` `pos_z` (model units) |
| `value` | Expression over the model's variables |

Expressions support `+ - * /`, unary `-`, parentheses, numbers, variables, the constants `pi` and `deg` (`180/pi`, to turn a radian result into degrees), and `sin cos` (radians, Minecraft's `Mth` tables, so ports from Java match), `abs`, `min`, `max`, `clamp(x, lo, hi)`, `lerp(a, b, t)`. Evaluation is in `float`.

Variables come from the Java model type (`AnimationVariables`). Living entities get `age`, `walk_pos`, `walk_speed`, and `head_pitch` / `head_yaw` (degrees). An unknown variable fails when the model is built and lists the available names.

A child file's bindings replace the parent's with the same part + channel and append the rest.

## Java API

Package `com.adamkali.dwm.model.json`:

- `EntityModelJson.parse` / `resolveParents` / `toLayerDefinition`
- `EntityModelJson.loadClasspath(id)` — unit tests (mod jar copy)
- `EntityModelJson.load(ResourceManager, id)` — pack-overridable bake
- `JsonEntityModel` — `EntityModel` for a baked mesh; given an `EntityAnimation` and `AnimationVariables` it applies the bindings in `setupAnim`
- `EntityModelAnimations.get(ModelLayerLocation)` — animation recorded when the layer loads
- `com.adamkali.dwm.model.json.anim` — `Expression`, `EntityAnimation`, `AnimationVariables`, `AnimationRunner`
- `JsonEntityModelLayers.register(ModelLayerLocation)` — Fabric layer whose supplier loads the JSON and records its `texture`
- `EntityModelTextures.get(ModelLayerLocation)` — texture declared by the layer's JSON

TARDIS `renderShell` / `renderDoors` stay in Java; renderer texture variants (e.g. per-variant skins) also stay in Java. Armor/humanoid templates are not in this format yet.
