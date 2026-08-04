# Asset Import Pipeline

Agent / local tooling for promoting production textures from the DWM asset archive into this mod. Not a Gradle or CI gate.

## Archive layout

| Path | Role |
|------|------|
| `/Users/adamali/Developer/Assets/DWM-Asset-Archive/assets/dwm/` | Production-ready Minecraft resource tree — **use this** |
| `.../unstructured/` | Scratch / Blockbench working files — **never bulk-import** |

Default texture root for building families: `assets/dwm/textures/block/`.

## Promote textures

From the repo root:

```bash
python3 tools/import_archive_textures.py --list
python3 tools/import_archive_textures.py --family gallifrey_stone --dry-run
python3 tools/import_archive_textures.py --family gallifrey_stone
```

Copies land under `src/client/resources/assets/dwm/` (same relative paths as the archive).

Add new families by extending the allowlist in `tools/import_archive_textures.py`.

## Register + datagen

1. Register blocks/items in common Java (`DWMBlocks`, settings, creative tabs).
2. Extend Fabric datagen providers under `com.adamkali.dwm.datagen` (models, lang, recipes, tags).
3. Add block/item display names in `DWMLanguageProvider` (`item.dwm.*` for BlockItems; also `block.dwm.*` when useful). Lang is generated only — no hand-merged `en_us.json`.
4. Run:

```bash
./gradlew runDatagen
./gradlew test
```

`runDatagen` finishes with `pruneDatagenItemModels`, which drops generated item models that duplicate hand-maintained assets under `src/client/resources` (Gallifrey item models are kept).

Datagen entrypoint is `DWMClientDataGenerator` (client source set) so model + data providers share one output cache.

Delete any `src/main/generated/.cache` before committing.

## Current families

- `gallifrey_stone` — stone / bricks / cobble / sandstone / dirt / sand (DWM-011). Grass block deferred until a top texture exists in the archive.

Wood and citadel families (DWM-012+) should reuse this same import + datagen pattern.

## Related docs

- [Building Content System](./feature-building-content.md) — player-facing building content
- [Branding Guidelines](./branding-guidelines.md) — `snake_case` IDs and family naming
