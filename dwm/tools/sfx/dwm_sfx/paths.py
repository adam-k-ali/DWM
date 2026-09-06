"""Resolve DWM mod root and this SFX Poetry project root."""

from __future__ import annotations

from pathlib import Path


def find_sfx_project_root(start: Path | None = None) -> Path:
    """Walk up until ``pyproject.toml`` and ``fixtures/`` exist (sfx project root)."""
    here = (start or Path(__file__)).resolve()
    for candidate in [here, *here.parents]:
        if (candidate / "pyproject.toml").is_file() and (candidate / "fixtures").is_dir():
            return candidate
    raise FileNotFoundError(
        "Could not find sfx project root (expected pyproject.toml and fixtures/)"
    )


def find_dwm_root(start: Path | None = None) -> Path:
    """Walk up until ``docs/palettes`` and ``src/client/resources`` exist."""
    here = (start or Path(__file__)).resolve()
    for candidate in [here, *here.parents]:
        if (candidate / "docs" / "palettes").is_dir() and (
            candidate / "src" / "client" / "resources"
        ).is_dir():
            return candidate
    raise FileNotFoundError(
        "Could not find DWM root (expected docs/palettes and src/client/resources)"
    )
