"""Family palette loading and host-stone / ore recolour helpers.

Offline tooling only — not invoked by Gradle or CI.
"""

from __future__ import annotations

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
    """Load a seed+profile palette JSON and expand to step roles."""
    from dwm_palette.palette import load_palette as _load_palette

    return _load_palette(path)


def palette_hexes(palette: dict[str, Any]) -> list[str]:
    """Return expanded step hexes sorted dark→light by luminance."""
    from dwm_palette.palette import palette_hexes as _palette_hexes

    return _palette_hexes(palette)


# Back-compat aliases: any palette's expanded steps (usage is host vs mineral at product time).
host_hexes = palette_hexes
vein_hexes = palette_hexes

def unique_colours_by_luminance(image_rgb: np.ndarray) -> list[Rgb]:
    if image_rgb.ndim != 3 or image_rgb.shape[2] < 3:
        raise ValueError("image_rgb must be HxWx3 (or HxWx4) array")
    rgb = image_rgb[:, :, :3].astype(np.uint8, copy=False)
    flat = rgb.reshape(-1, 3)
    unique = {tuple(int(c) for c in row) for row in flat}
    return sorted(unique, key=luminance_u8)  # type: ignore[arg-type]


def host_colour_set(stone_rgb: np.ndarray) -> frozenset[Rgb]:
    """Unique RGB triples from a stone template, used to classify ore host pixels."""
    return frozenset(unique_colours_by_luminance(stone_rgb))


def split_ore_colours(
    ore_rgb: np.ndarray, host_colours: frozenset[Rgb]
) -> tuple[list[Rgb], list[Rgb]]:
    """Split ore unique colours into host (in host_colours) vs mineral (the rest)."""
    colours = unique_colours_by_luminance(ore_rgb)
    host = [c for c in colours if c in host_colours]
    mineral = [c for c in colours if c not in host_colours]
    if not host:
        raise ValueError("ore template has no host pixels matching stone colours")
    if not mineral:
        raise ValueError("ore template has no mineral pixels (all match stone colours)")
    return host, mineral


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


def build_rank_colour_map(
    source_colours: list[Rgb], target_hex_list: list[str]
) -> dict[Rgb, Rgb]:
    """Map source colours onto target hexes by luminance rank (not nearest luma).

    Source and targets are sorted dark→light. When counts differ, each source
    index maps to ``round(i * (n_targets - 1) / (n_sources - 1))`` (half-up).
    """
    if not source_colours:
        raise ValueError("source colour list is empty")
    if not target_hex_list:
        raise ValueError("target hex list is empty")

    sources = sorted(source_colours, key=luminance_u8)
    targets = [hex_to_rgb(h) for h in target_hex_list]
    # target_hex_list is already dark→light; keep that order.
    n_src = len(sources)
    n_tgt = len(targets)
    colour_map: dict[Rgb, Rgb] = {}
    if n_src == 1:
        colour_map[sources[0]] = targets[0]
        return colour_map

    for i, colour in enumerate(sources):
        target_i = int(i * (n_tgt - 1) / (n_src - 1) + 0.5)
        colour_map[colour] = targets[target_i]
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


def apply_ore_palettes(
    ore_rgb: np.ndarray,
    host_hex_list: list[str],
    vein_hex_list: list[str],
    host_colours: frozenset[Rgb],
) -> np.ndarray:
    """Remap ore template: host pixels → host hexes, mineral pixels → vein hexes.

    Host colours are classified against *host_colours* (frozen stone template
    uniques — vanilla ``stone.png`` for emerald layouts, or a DWM host cube for
    remapped DWM ores), not the live edited host hex list. Mineral colours use
    rank mapping so many source vein steps still reach the full target ramp
    (e.g. emerald's 8 greens → 4 mineral profile steps).

    Returns a new HxWx3 uint8 array.
    """
    if ore_rgb.ndim != 3 or ore_rgb.shape[2] < 3:
        raise ValueError("ore_rgb must be HxWx3 (or HxWx4) array")
    src = ore_rgb[:, :, :3].astype(np.uint8, copy=False)
    host_src, mineral_src = split_ore_colours(src, host_colours)
    colour_map = build_host_colour_map(host_src, host_hex_list)
    colour_map.update(build_rank_colour_map(mineral_src, vein_hex_list))

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



def load_rgba_image(path: Path) -> np.ndarray:
    """Load a PNG as HxWx4 uint8 via Pillow (alpha preserved)."""
    from PIL import Image

    with Image.open(path) as img:
        rgba = img.convert("RGBA")
        return np.asarray(rgba, dtype=np.uint8)


def apply_mineral_item_palette(
    template_rgba: np.ndarray, mineral_hex_list: list[str]
) -> np.ndarray:
    """Remap opaque item pixels onto mineral hexes; leave transparent pixels alone.

    Used for gem, crystal, and ingot item archetypes (no host/handle split).
    Opaque colours are rank-mapped onto the mineral ramp. Original alpha is
    preserved.

    Returns a new HxWx4 uint8 array.
    """
    if template_rgba.ndim != 3 or template_rgba.shape[2] != 4:
        raise ValueError("template_rgba must be HxWx4 array")
    if not mineral_hex_list:
        raise ValueError("mineral hex list is empty")

    src = template_rgba.astype(np.uint8, copy=False)
    alpha = src[:, :, 3]
    opaque = alpha > 0
    if not np.any(opaque):
        return src.copy()

    opaque_rgb = src[:, :, :3][opaque]
    colours = unique_colours_by_luminance(opaque_rgb.reshape(-1, 1, 3))
    colour_map = build_rank_colour_map(colours, mineral_hex_list)

    out = src.copy()
    h, w, _ = src.shape
    for y in range(h):
        for x in range(w):
            if out[y, x, 3] == 0:
                continue
            key = (int(src[y, x, 0]), int(src[y, x, 1]), int(src[y, x, 2]))
            mapped = colour_map[key]
            out[y, x, 0] = mapped[0]
            out[y, x, 1] = mapped[1]
            out[y, x, 2] = mapped[2]
            # alpha unchanged
    return out


def split_tool_colours(
    tool_rgba: np.ndarray, handle_colours: frozenset[Rgb]
) -> tuple[list[Rgb], list[Rgb]]:
    """Split opaque tool colours into handle (in handle_colours) vs metal."""
    if tool_rgba.ndim != 3 or tool_rgba.shape[2] != 4:
        raise ValueError("tool_rgba must be HxWx4 array")
    src = tool_rgba.astype(np.uint8, copy=False)
    opaque = src[:, :, 3] > 0
    if not np.any(opaque):
        raise ValueError("tool template has no opaque pixels")
    opaque_rgb = src[:, :, :3][opaque]
    colours = unique_colours_by_luminance(opaque_rgb.reshape(-1, 1, 3))
    handle = [c for c in colours if c in handle_colours]
    metal = [c for c in colours if c not in handle_colours]
    if not handle:
        raise ValueError("tool template has no handle pixels matching stick colours")
    if not metal:
        raise ValueError("tool template has no metal pixels (all match stick colours)")
    return handle, metal


def apply_tool_item_palette(
    template_rgba: np.ndarray,
    handle_hex_list: list[str],
    metal_hex_list: list[str],
    handle_colours: frozenset[Rgb],
) -> np.ndarray:
    """Remap tool item: handle pixels → handle hexes, metal pixels → metal hexes.

    Handle colours are classified against *handle_colours* (frozen stick template
    uniques for vanilla iron tools). Metal colours use rank mapping onto the
    metal/mineral ramp. Original alpha is preserved.

    Returns a new HxWx4 uint8 array.
    """
    if template_rgba.ndim != 3 or template_rgba.shape[2] != 4:
        raise ValueError("template_rgba must be HxWx4 array")
    if not handle_hex_list:
        raise ValueError("handle hex list is empty")
    if not metal_hex_list:
        raise ValueError("metal hex list is empty")
    if not handle_colours:
        raise ValueError("handle_colours must be a non-empty frozenset")

    src = template_rgba.astype(np.uint8, copy=False)
    handle_src, metal_src = split_tool_colours(src, handle_colours)
    colour_map = build_host_colour_map(handle_src, handle_hex_list)
    colour_map.update(build_rank_colour_map(metal_src, metal_hex_list))

    out = src.copy()
    h, w, _ = src.shape
    for y in range(h):
        for x in range(w):
            if out[y, x, 3] == 0:
                continue
            key = (int(src[y, x, 0]), int(src[y, x, 1]), int(src[y, x, 2]))
            mapped = colour_map[key]
            out[y, x, 0] = mapped[0]
            out[y, x, 1] = mapped[1]
            out[y, x, 2] = mapped[2]
            # alpha unchanged
    return out
