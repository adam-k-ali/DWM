# AGENTS.md

## Scope
This file applies to `dwm/tools/` — offline Python tooling for DWM. These projects are **not** invoked by Gradle or CI.

## Projects

| Project | Path | Purpose |
| --- | --- | --- |
| Palette | [`palette/`](palette/) | Family colour-palette docs + recolour GUI |
| SFX | [`sfx/`](sfx/) | TARDIS travel and entity SFX generation/analysis |

Each project is a Poetry package with its own `pyproject.toml`, in-project `.venv`, and nested `AGENTS.md`.

## Commands (from repo root)

```bash
poetry -C dwm/tools/palette install
poetry -C dwm/tools/sfx install
```

See each project's `AGENTS.md` for run/test commands.
