"""Family palette loading and host-stone recolour helpers.

Offline tooling only — not invoked by Gradle or CI.
"""

from __future__ import annotations

import json
import re
from pathlib import Path
from typing import Any

import numpy as np

HEX_RE = re.compile(r"^#[0-9A-Fa-f]{6}$")

Rgb = tuple[int, int, int]
RgbFloat = tuple[float, float, float]


def parse_hex(value: str) -> RgbFloat:
    raw = value.lstrip("#")
    return tuple(int(raw[i : i + 2], 16) / 255.0 for i in (0, 2, 4))  # type: ignore[return-value]


def hex_to_rgb(value: str) -> Rgb:
    raw = value.lstrip("#")
    return (
        int(raw[0:2], 16),
        int(raw[2:4], 16),
        int(raw[4:6], 16),
    )


def rgb_to_hex(rgb: Rgb) -> str:
    return f"#{rgb[0]:02X}{rgb[1]:02X}{rgb[2]:02X}"


def luminance(rgb: RgbFloat) -> float:
    r, g, b = rgb
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def luminance_u8(rgb: Rgb) -> float:
    return luminance((rgb[0] / 255.0, rgb[1] / 255.0, rgb[2] / 255.0))


def load_palette(path: Path) -> dict[str, Any]:
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        raise ValueError(f"{path}: root must be a JSON object")

    family_id = data.get("family_id")
    display_name = data.get("display_name")
    roles = data.get("roles")
    if not isinstance(family_id, str) or not family_id:
        raise ValueError(f"{path}: family_id must be a non-empty string")
    if not isinstance(display_name, str) or not display_name:
        raise ValueError(f"{path}: display_name must be a non-empty string")
    if not isinstance(roles, list) or not roles:
        raise ValueError(f"{path}: roles must be a non-empty list")

    seen: set[str] = set()
    normalized: list[dict[str, str]] = []
    for i, entry in enumerate(roles):
        if not isinstance(entry, dict):
            raise ValueError(f"{path}: roles[{i}] must be an object")
        role = entry.get("role")
        hex_value = entry.get("hex")
        notes = entry.get("notes", "")
        if not isinstance(role, str) or not role:
            raise ValueError(f"{path}: roles[{i}].role must be a non-empty string")
        if role in seen:
            raise ValueError(f"{path}: duplicate role {role!r}")
        seen.add(role)
        if not isinstance(hex_value, str) or not HEX_RE.match(hex_value):
            raise ValueError(
                f"{path}: roles[{i}].hex must be #RRGGBB, got {hex_value!r}"
            )
        if notes is None:
            notes = ""
        if not isinstance(notes, str):
            raise ValueError(f"{path}: roles[{i}].notes must be a string")
        normalized.append(
            {"role": role, "hex": hex_value.upper(), "notes": notes}
        )

    map_color = data.get("map_color")
    if map_color is not None and not isinstance(map_color, str):
        raise ValueError(f"{path}: map_color must be a string when present")
    notes = data.get("notes", "")
    if notes is None:
        notes = ""
    if not isinstance(notes, str):
        raise ValueError(f"{path}: notes must be a string when present")

    return {
        "family_id": family_id,
        "display_name": display_name,
        "map_color": map_color,
        "notes": notes,
        "roles": normalized,
    }


def host_hexes(palette: dict[str, Any]) -> list[str]:
    """Return host_* role hexes sorted dark→light by luminance."""
    hosts = [
        entry["hex"]
        for entry in palette["roles"]
        if entry["role"].startswith("host_")
    ]
    if not hosts:
        raise ValueError("palette has no host_* roles")
    return sorted(hosts, key=lambda h: luminance(parse_hex(h)))


def unique_colours_by_luminance(image_rgb: np.ndarray) -> list[Rgb]:
    if image_rgb.ndim != 3 or image_rgb.shape[2] < 3:
        raise ValueError("image_rgb must be HxWx3 (or HxWx4) array")
    rgb = image_rgb[:, :, :3].astype(np.uint8, copy=False)
    flat = rgb.reshape(-1, 3)
    unique = {tuple(int(c) for c in row) for row in flat}
    return sorted(unique, key=luminance_u8)  # type: ignore[arg-type]


def build_host_colour_map(
    template_colours: list[Rgb], host_hex_list: list[str]
) -> dict[Rgb, Rgb]:
    """Map each unique template colour to a host hex by luminance.

    When the unique-colour count equals the host count, pair dark→light 1:1.
    Otherwise map each colour to the nearest host by luminance.
    """
    if not template_colours:
        raise ValueError("template has no colours")
    if not host_hex_list:
        raise ValueError("host hex list is empty")

    hosts = [hex_to_rgb(h) for h in host_hex_list]
    if len(template_colours) == len(hosts):
        return {colour: hosts[i] for i, colour in enumerate(template_colours)}

    host_lums = [luminance_u8(h) for h in hosts]
    colour_map: dict[Rgb, Rgb] = {}
    for colour in template_colours:
        lum = luminance_u8(colour)
        best_i = min(range(len(hosts)), key=lambda i: abs(host_lums[i] - lum))
        colour_map[colour] = hosts[best_i]
    return colour_map


def apply_host_palette(template_rgb: np.ndarray, host_hex_list: list[str]) -> np.ndarray:
    """Remap template pixels onto host palette hexes (no interpolation).

    Returns a new HxWx3 uint8 array.
    """
    if template_rgb.ndim != 3 or template_rgb.shape[2] < 3:
        raise ValueError("template_rgb must be HxWx3 (or HxWx4) array")
    src = template_rgb[:, :, :3].astype(np.uint8, copy=False)
    colours = unique_colours_by_luminance(src)
    colour_map = build_host_colour_map(colours, host_hex_list)

    h, w, _ = src.shape
    out = np.empty((h, w, 3), dtype=np.uint8)
    for y in range(h):
        for x in range(w):
            key = (int(src[y, x, 0]), int(src[y, x, 1]), int(src[y, x, 2]))
            out[y, x] = colour_map[key]
    return out


def load_rgb_image(path: Path) -> np.ndarray:
    """Load a PNG as HxWx3 uint8 via Pillow."""
    from PIL import Image

    with Image.open(path) as img:
        rgb = img.convert("RGB")
        return np.asarray(rgb, dtype=np.uint8)
