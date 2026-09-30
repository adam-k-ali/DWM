# JSON entity models

See also: [Docs Index](./index.md)

DWM entity and block-entity meshes can be authored as JSON under `assets/<namespace>/models/entity/` instead of `CubeListBuilder` trees in Java. The client compiles each file to a vanilla `LayerDefinition`.

This is **not** vanilla block/item JSON (no bone tree, per-face UV) and **not** Bedrock `geo.json`. Cubes use Minecraft Java `texOffs` unwrap.

## Resource path

| Layer id | File |
|----------|------|
| `dwm:dalek_laser` | `assets/dwm/models/entity/dalek_laser.json` |
| `dwm:entity/console_selector` (parent form) | `assets/dwm/models/entity/console_selector.json` |

`layer` (default `main`) is the `ModelLayerLocation` suffix. New meshes still need one Java registration line; resource packs can override the JSON of registered ids.

## File shape

```json
{
  "parent": "dwm:entity/console_selector",
  "layer": "main",
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
| `texture` | Optional default atlas (`Identifier` form, including `textures/` and `.png`) |
| `parts[].name` | `addOrReplaceChild` (keep names used by animation and tests) |
| `pivot` | `PartPose` translation |
| `rotation` | Rest pose in **degrees**; converted to radians at bake |
| `cubes[].uv` | `texOffs(u, v)` |
| `origin` + `size` | `addBox(x, y, z, w, h, d)` |
| `inflate` | `CubeDeformation` (negative allowed) |
| `mirror` | `CubeListBuilder.mirror()` |
| `parent` | Copy parent, then overlay `texture` / `layer` / texture size; non-empty `parts` replace the parent tree |

A file must have either `parent` or `texture_width` + `texture_height` + `parts`. Schema: `src/test/resources/schemas/entity_model.schema.json`.

## Java API

Package `com.adamkali.dwm.model.json`:

- `EntityModelJson.parse` / `resolveParents` / `toLayerDefinition`
- `EntityModelJson.loadClasspath(id)` — unit tests and model factory methods
- `EntityModelJson.load(ResourceManager, id)` — pack-overridable bake
- `JsonEntityModel` — mesh-only `EntityModel`
- `JsonEntityModelLayers.register(ModelLayerLocation)` — Fabric layer whose supplier loads the JSON

`setupAnim`, TARDIS `renderShell` / `renderDoors`, and renderer texture selection stay in Java. Armor/humanoid templates are not in this format yet.
