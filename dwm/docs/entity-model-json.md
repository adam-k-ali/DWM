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
| `texture` | Atlas (`Identifier` form, including `textures/` and `.png`). Documentation only: renderers use the Java `TEXTURE_LOCATION`, and a unit test asserts the two match. Pack overrides do not retarget it. |
| `parts[].name` | `addOrReplaceChild` (keep names used by animation and tests); must be unique among siblings, otherwise the file fails to parse |
| `pivot` | `PartPose` translation |
| `rotation` | Rest pose in **degrees**; converted to radians at bake |
| `cubes[].uv` | `texOffs(u, v)` |
| `origin` + `size` | `addBox(x, y, z, w, h, d)` |
| `inflate` | `CubeDeformation` (negative allowed) |
| `mirror` | `CubeListBuilder.mirror()` |
| `parent` | Copy parent, then overlay `texture` / texture size; child `parts` replace same-named parent parts and append new names (an empty or absent `parts` inherits the parent tree) |

A resolved model must have at least one part. A file must have either `parent` or `texture_width` + `texture_height` + `parts`. Schema: `src/test/resources/schemas/entity_model.schema.json`.

## Java API

Package `com.adamkali.dwm.model.json`:

- `EntityModelJson.parse` / `resolveParents` / `toLayerDefinition`
- `EntityModelJson.loadClasspath(id)` — unit tests (mod jar copy)
- `EntityModelJson.load(ResourceManager, id)` — pack-overridable bake
- `JsonEntityModel` — mesh-only `EntityModel`
- `JsonEntityModelLayers.register(ModelLayerLocation)` — Fabric layer whose supplier loads the JSON

`setupAnim`, TARDIS `renderShell` / `renderDoors`, and renderer texture selection stay in Java. Armor/humanoid templates are not in this format yet.
