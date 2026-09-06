"""Named contrast profiles and seed→ramp expansion.

Offline tooling only — not invoked by Gradle or CI.
"""

from __future__ import annotations

import json
from functools import lru_cache
from pathlib import Path
from typing import Any

from dwm_palette.oklab import (
    oklab_chroma,
    oklab_to_rgb,
    oklab_with_lightness_chroma,
    rgb_to_oklab,
)

PROFILES_PATH = Path(__file__).resolve().parent / "data" / "profiles.json"


@lru_cache(maxsize=1)
def load_profiles() -> dict[str, Any]:
    data = json.loads(PROFILES_PATH.read_text(encoding="utf-8"))
    if not isinstance(data, dict) or not data:
        raise ValueError(f"{PROFILES_PATH}: expected a non-empty object")
    return data


def get_profile(profile_id: str) -> dict[str, Any]:
    profiles = load_profiles()
    if profile_id not in profiles:
        known = ", ".join(sorted(profiles))
        raise ValueError(f"unknown profile {profile_id!r}; known: {known}")
    return profiles[profile_id]


def _rgb_float_to_hex(rgb: tuple[float, float, float]) -> str:
    return "#{:02X}{:02X}{:02X}".format(
        int(round(rgb[0] * 255.0)),
        int(round(rgb[1] * 255.0)),
        int(round(rgb[2] * 255.0)),
    )


def expand_ramp(seed: str, profile_id: str) -> list[dict[str, str]]:
    """Expand a mid seed into ordered role entries using a named profile.

    When the profile has a ``reference`` and *seed* matches ``reference.seed``,
    return the reference hexes exactly (identity for Gallifrey stone).
    """
    from dwm_palette.recolor import HEX_RE, parse_hex

    if not isinstance(seed, str) or not HEX_RE.match(seed):
        raise ValueError(f"seed must be #RRGGBB, got {seed!r}")
    seed_u = seed.upper()
    profile = get_profile(profile_id)
    steps: list[str] = list(profile["steps"])
    delta_L: list[float] = list(profile["delta_L"])
    chroma_ratio: list[float] = list(profile["chroma_ratio"])
    if not (len(steps) == len(delta_L) == len(chroma_ratio)):
        raise ValueError(f"profile {profile_id!r}: steps/delta_L/chroma_ratio length mismatch")

    reference = profile.get("reference")
    if isinstance(reference, dict):
        ref_seed = str(reference.get("seed", "")).upper()
        ref_hexes = reference.get("hexes")
        if seed_u == ref_seed and isinstance(ref_hexes, list) and len(ref_hexes) == len(steps):
            return [
                {"role": step, "hex": str(h).upper(), "notes": ""}
                for step, h in zip(steps, ref_hexes)
            ]

    mid_name = profile.get("mid", "mid")
    mid_lab = rgb_to_oklab(parse_hex(seed_u))
    mid_chroma = oklab_chroma(mid_lab)
    roles: list[dict[str, str]] = []
    for step, dL, c_ratio in zip(steps, delta_L, chroma_ratio):
        if step == mid_name:
            # Keep the authored mid hex exact (no OKLab round-trip drift).
            roles.append({"role": step, "hex": seed_u, "notes": ""})
            continue
        L = mid_lab[0] + dL
        chroma = mid_chroma * c_ratio
        lab = oklab_with_lightness_chroma(mid_lab, L, chroma)
        roles.append({"role": step, "hex": _rgb_float_to_hex(oklab_to_rgb(lab)), "notes": ""})
    return roles
