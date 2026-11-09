# AGENTS.md

## Scope
This file applies to `dwm/tools/` — offline Python tooling for DWM. These projects are **not** invoked by Gradle or CI.

## Projects

| Project | Path | Purpose |
| --- | --- | --- |
| Palette | [`palette/`](palette/) | Family colour-palette docs + recolour GUI |
| SFX | [`sfx/`](sfx/) | TARDIS travel and entity SFX generation/analysis |
| TARDIS exteriors | [`tardis_exteriors/`](tardis_exteriors/) | Stdlib-only generator (`python3 dwm/tools/tardis_exteriors/generate.py`) that builds the 2005/2010/2014/2018 exterior meshes + textures from per-era specs in `eras.py`. They follow the 1st–7th Doctor boxes: detail is modelled (not painted), every coordinate sits on the 0.25 texture-pixel grid, paint is flat with soft mottling. `preview.py OUT_DIR [px] [reference_id ...]` renders offline previews, optionally beside shipped boxes such as `seventh_doctor_box` |

`palette/` and `sfx/` are Poetry packages with their own `pyproject.toml`, in-project `.venv`, and nested `AGENTS.md`; `tardis_exteriors/` is plain stdlib Python with no install step.

## Commands (from repo root)

```bash
poetry -C dwm/tools/palette install
poetry -C dwm/tools/sfx install
```

See each project's `AGENTS.md` for run/test commands.
