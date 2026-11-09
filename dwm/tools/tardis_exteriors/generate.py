#!/usr/bin/env python3
"""Procedurally generate the 2005/2010/2014/2018 TARDIS exterior meshes + textures.

Stdlib only. Writes, per era:
  src/client/resources/assets/dwm/models/entity/tardis_<year>.json
  src/client/resources/assets/dwm/textures/entity/tardis_<year>.png   (128x128 RGBA)

The output is placeholder quality and meant to be replaced by hand-authored Blockbench
models later; the part names (root `LeftDoor` / `rightDoor`) and the `door_progress * 60`
binding match what `TardisModel.getDoorParts()` expects.

Run from the repo root:  python3 dwm/tools/tardis_exteriors/generate.py
"""
from __future__ import annotations

import json
import math
import struct
import zlib
from pathlib import Path

TEX = 128
SWATCH_W, SWATCH_H = 64, 32  # each material owns a 64x32 solid-ish region of the atlas
MATERIALS = ["body", "panel", "frame", "glass", "sign", "lamp", "trim", "handle"]

ROOT = Path(__file__).resolve().parents[2]
MODEL_DIR = ROOT / "src/client/resources/assets/dwm/models/entity"
TEXTURE_DIR = ROOT / "src/client/resources/assets/dwm/textures/entity"


def swatch_origin(material: str) -> tuple[int, int]:
    i = MATERIALS.index(material)
    return (i % 2) * SWATCH_W, (i // 2) * SWATCH_H


class Era:
    def __init__(self, year, colours, sign_height, roof_tiers, lamp, window_rows, door_rows, wall_panels, weathering):
        self.year = year
        self.id = f"tardis_{year}"
        self.colours = colours  # material -> (r, g, b)
        self.sign_height = sign_height
        self.roof_tiers = roof_tiers  # [(half_width, thickness)]
        self.lamp = lamp  # (size, height)
        self.window_rows = window_rows  # window rows on the three non-door walls
        self.door_rows = door_rows  # window rows per door
        self.wall_panels = wall_panels  # raised lower panel(s) on walls
        self.weathering = weathering  # 0..1 noise amount


ERAS = [
    Era(2005,
        dict(body=(28, 58, 112), panel=(22, 46, 92), frame=(18, 38, 80), glass=(236, 214, 150), sign=(14, 14, 18),
             lamp=(255, 196, 80), trim=(24, 48, 98), handle=(170, 160, 120)),
        sign_height=2, roof_tiers=[(7, 1), (5, 1)], lamp=(2, 3), window_rows=3, door_rows=3, wall_panels=1,
        weathering=0.45),
    Era(2010,
        dict(body=(30, 88, 170), panel=(24, 72, 148), frame=(20, 60, 126), glass=(255, 236, 170), sign=(10, 10, 14),
             lamp=(255, 240, 150), trim=(28, 80, 156), handle=(214, 200, 150)),
        sign_height=3, roof_tiers=[(7, 1), (6, 1), (4, 1)], lamp=(3, 4), window_rows=3, door_rows=3, wall_panels=1,
        weathering=0.12),
    Era(2014,
        dict(body=(14, 38, 84), panel=(9, 26, 62), frame=(8, 22, 54), glass=(190, 214, 245), sign=(8, 8, 12),
             lamp=(250, 244, 220), trim=(12, 32, 72), handle=(150, 160, 175)),
        sign_height=3, roof_tiers=[(7, 1), (5, 1), (3, 1)], lamp=(2, 3), window_rows=2, door_rows=2, wall_panels=2,
        weathering=0.2),
    Era(2018,
        dict(body=(20, 40, 108), panel=(16, 32, 90), frame=(205, 210, 216), glass=(235, 244, 255), sign=(12, 14, 22),
             lamp=(255, 255, 255), trim=(176, 182, 190), handle=(225, 228, 232)),
        sign_height=2, roof_tiers=[(7, 1), (6, 1)], lamp=(2, 2), window_rows=3, door_rows=3, wall_panels=1,
        weathering=0.08),
]


# ---------------------------------------------------------------- geometry
def cube(material, x0, x1, h0, h1, z0, z1, ox=0.0, oz=0.0):
    """Cube from world-ish extents (h = height above ground); origin is relative to a part pivot at (ox, h=0, oz)."""
    w, hh, d = x1 - x0, h1 - h0, z1 - z0
    u, v = swatch_origin(material)
    need_w, need_h = math.ceil(2 * (w + d)), math.ceil(hh + d)
    if need_w > SWATCH_W or need_h > SWATCH_H:
        raise ValueError(f"{material} cube {w}x{hh}x{d} needs {need_w}x{need_h} > swatch")
    return {"uv": [u, v], "origin": [round(x0 - ox, 3), round(-h1, 3), round(z0 - oz, 3)],
            "size": [round(w, 3), round(hh, 3), round(d, 3)]}


def window_grid(rows, x_ranges, base_h, top_h):
    """Frame + glass pairs stacked between base_h and top_h; yields cubes in local wall space (z = -6 face)."""
    cubes = []
    row_h = (top_h - base_h) / rows
    for r in range(rows):
        h0 = base_h + r * row_h
        h1 = h0 + row_h
        for (x0, x1) in x_ranges:
            cubes.append(cube("frame", x0, x1, h0, h1, -6.2, -5.2))
            cubes.append(cube("glass", x0 + 0.5, x1 - 0.5, h0 + 0.5, h1 - 0.5, -6.3, -5.3))
    return cubes


def wall_cubes(era, with_slab=True):
    cubes = []
    if with_slab:
        cubes.append(cube("body", -5, 5, 1, 23, -6, -5))
        panel_h = 6.0 / era.wall_panels
        for i in range(era.wall_panels):
            h0 = 1.5 + i * panel_h
            cubes.append(cube("panel", -4.5, 4.5, h0, h0 + panel_h - 1.0, -6.2, -5.2))
        cubes += window_grid(era.window_rows, [(-5, 0), (0, 5)], 7.0, 7.0 + 5.0 * era.window_rows)
    cubes.append(sign_band(era))
    return cubes


def sign_band(era):
    return cube("sign", -5.5, 5.5, 23, 23 + era.sign_height, -6.3, -5.3)


def door_cubes(era, side):
    """side = +1 for LeftDoor (hinge at x=+5), -1 for rightDoor (hinge at x=-5). Pivot (side*5, 24, -5.5)."""
    ox, oz = side * 5.0, -5.5

    def span(a, b):  # world x range for door-local offsets a..b measured from the centre line
        lo, hi = sorted((side * a, side * b))
        return lo, hi

    cubes = []
    x0, x1 = span(0, 5)
    cubes.append(cube("body", x0, x1, 1, 23, -6, -5, ox, oz))
    rows = era.door_rows
    row_h = 14.0 / rows if rows else 0
    for r in range(rows):
        h0 = 7.0 + r * row_h
        h1 = h0 + row_h
        fx0, fx1 = span(0.5, 4.5)
        cubes.append(cube("frame", fx0, fx1, h0, h1, -6.2, -5.2, ox, oz))
        gx0, gx1 = span(1.0, 4.0)
        cubes.append(cube("glass", gx0, gx1, h0 + 0.5, h1 - 0.5, -6.3, -5.3, ox, oz))
    px0, px1 = span(0.5, 4.5)
    cubes.append(cube("panel", px0, px1, 1.5, 6.0, -6.2, -5.2, ox, oz))
    hx0, hx1 = span(0.5, 1.5)
    cubes.append(cube("handle", hx0, hx1, 11.0, 13.0, -6.5, -5.5, ox, oz))
    return cubes


def build_model(era):
    posts = []
    top = 23 + era.sign_height
    for sx in (-1, 1):
        for sz in (-1, 1):
            x0, x1 = (5, 6) if sx > 0 else (-6, -5)
            z0, z1 = (5, 6) if sz > 0 else (-6, -5)
            posts.append(cube("trim", x0, x1, 1, top, z0, z1))

    roof = []
    h = top
    for half, thick in era.roof_tiers:
        roof.append(cube("trim", -half, half, h, h + thick, -half, half))
        h += thick
    lamp_size, lamp_h = era.lamp
    roof.append(cube("trim", -0.5, 0.5, h, h + 1, -0.5, 0.5))
    lh = lamp_size / 2
    roof.append(cube("lamp", -lh, lh, h + 1, h + 1 + lamp_h, -lh, lh))

    def part(name, cubes, pivot=(0.0, 24.0, 0.0), rotation=None):
        p = {"name": name, "pivot": list(pivot), "cubes": cubes}
        if rotation:
            p["rotation"] = list(rotation)
        return p

    parts = [
        part("base", [cube("trim", -7.5, 7.5, 0, 1, -7.5, 7.5)]),
        part("posts", posts),
        part("roof", roof),
        part("front", [sign_band(era)]),
        part("wall_left", wall_cubes(era), rotation=(0.0, -90.0, 0.0)),
        part("wall_back", wall_cubes(era), rotation=(0.0, 180.0, 0.0)),
        part("wall_right", wall_cubes(era), rotation=(0.0, 90.0, 0.0)),
        part("LeftDoor", door_cubes(era, +1), pivot=(5.0, 24.0, -5.5)),
        part("rightDoor", door_cubes(era, -1), pivot=(-5.0, 24.0, -5.5)),
    ]
    return {
        "credit": f"Procedurally generated by dwm/tools/tardis_exteriors/generate.py ({era.year} TARDIS exterior)",
        "texture_width": TEX,
        "texture_height": TEX,
        "texture": f"dwm:textures/entity/{era.id}.png",
        "parts": parts,
        "animation": {"bindings": [{"part": "LeftDoor", "channel": "rot_y", "value": "door_progress * 60"}]},
    }


# ---------------------------------------------------------------- texture
def lcg(seed):
    state = seed & 0x7FFFFFFF
    while True:
        state = (state * 1103515245 + 12345) & 0x7FFFFFFF
        yield state / 0x7FFFFFFF


def clamp(v):
    return max(0, min(255, int(round(v))))


def paint(era):
    pixels = [[(0, 0, 0, 0)] * TEX for _ in range(TEX)]
    rnd = lcg(era.year)
    for material in MATERIALS:
        base = era.colours[material]
        u0, v0 = swatch_origin(material)
        for y in range(SWATCH_H):
            for x in range(SWATCH_W):
                n = (next(rnd) - 0.5) * 2
                amount = era.weathering if material in ("body", "panel", "trim") else 0.05
                shade = 1 + n * amount * 0.35
                r, g, b = (c * shade for c in base)
                if material in ("body", "panel", "trim") and x % 4 == 0:
                    r, g, b = r * 0.92, g * 0.92, b * 0.92  # faint vertical board seams
                if material == "glass":
                    # cross mullion + vertical glow gradient
                    glow = 1.0 - 0.25 * (y % 8) / 8.0
                    r, g, b = r * glow, g * glow, b * glow
                    if x % 8 == 0 or y % 8 == 0:
                        r, g, b = r * 0.55, g * 0.55, b * 0.6
                if material == "sign":
                    # stylised white lettering: dashes on alternating rows
                    if y % 4 in (1, 2) and (x // 2) % 3 != 2 and (x // 2) % 7 != 0:
                        r, g, b = 235, 235, 235
                if material == "lamp":
                    glow = 0.85 + 0.15 * math.sin((x + y) / 3.0)
                    r, g, b = r * glow, g * glow, b * glow
                pixels[v0 + y][u0 + x] = (clamp(r), clamp(g), clamp(b), 255)
    return pixels


def write_png(path, pixels):
    raw = b"".join(b"\x00" + b"".join(struct.pack("4B", *px) for px in row) for row in pixels)

    def chunk(tag, data):
        body = tag + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

    png = (b"\x89PNG\r\n\x1a\n"
           + chunk(b"IHDR", struct.pack(">IIBBBBB", TEX, TEX, 8, 6, 0, 0, 0))
           + chunk(b"IDAT", zlib.compress(raw, 9))
           + chunk(b"IEND", b""))
    path.write_bytes(png)


def main():
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    for era in ERAS:
        (MODEL_DIR / f"{era.id}.json").write_text(json.dumps(build_model(era), indent=2) + "\n")
        write_png(TEXTURE_DIR / f"{era.id}.png", paint(era))
        print(f"wrote {era.id}")


if __name__ == "__main__":
    main()
