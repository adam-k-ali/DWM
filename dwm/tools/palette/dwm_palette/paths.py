"""Resolve the DWM mod root from this package's location."""

from __future__ import annotations

from pathlib import Path


def find_dwm_root(start: Path | None = None) -> Path:
    """Walk up from *start* until ``docs/palettes`` and ``src/client/resources`` exist."""
    here = (start or Path(__file__)).resolve()
    for candidate in [here, *here.parents]:
        if (candidate / "docs" / "palettes").is_dir() and (
            candidate / "src" / "client" / "resources"
        ).is_dir():
            return candidate
    raise FileNotFoundError(
        "Could not find DWM root (expected docs/palettes and src/client/resources)"
    )
