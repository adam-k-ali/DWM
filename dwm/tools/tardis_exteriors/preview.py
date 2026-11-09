#!/usr/bin/env python3
"""Offline orthographic preview of the generated exteriors (not shipped; for iteration/review).

Rasterises every cube face with the vanilla `ModelPart.Cube` UV mapping, a z-buffer and simple
directional shading, then writes one sheet per era: front, side, three-quarter, and three-quarter
with the door open.

Usage:  python3 dwm/tools/tardis_exteriors/preview.py OUT_DIR [px_per_unit]
"""
from __future__ import annotations

import math
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from eras import ERAS  # noqa: E402
from generate import PX, build, png_bytes  # noqa: E402

FACE_DEFS = {
    # verts by corner name, (ua, va, ub, vb) in terms of the net columns/rows
    "down": (("l1", "l0", "t0", "t1"), ("u1", "v0", "u2", "v1")),
    "up": (("t2", "t3", "l3", "l2"), ("u2", "v1", "u22", "v0")),
    "west": (("t0", "l0", "l3", "t3"), ("u0", "v1", "u1", "v2")),
    "north": (("t1", "t0", "t3", "t2"), ("u1", "v1", "u2", "v2")),
    "east": (("l1", "t1", "t2", "l2"), ("u2", "v1", "u3", "v2")),
    "south": (("l0", "l1", "l2", "l3"), ("u3", "v1", "u4", "v2")),
}


def rot_y(p, deg):
    a = math.radians(deg)
    c, s = math.cos(a), math.sin(a)
    x, y, z = p
    return (x * c + z * s, y, -x * s + z * c)


def quads(model, door_deg=0.0):
    """World-space (y up) textured quads: list of (4 points, 4 uvs)."""
    out = []
    for part in model["parts"]:
        px, py, pz = part["pivot"]
        ry = (part.get("rotation") or [0, 0, 0])[1]
        if part["name"] == "LeftDoor":
            ry += door_deg
        for cube in part["cubes"]:
            (ox, oy, oz), (w, h, d) = cube["origin"], cube["size"]
            u, v = cube["uv"]
            corners = {
                "t0": (ox, oy, oz), "t1": (ox + w, oy, oz), "t2": (ox + w, oy + h, oz), "t3": (ox, oy + h, oz),
                "l0": (ox, oy, oz + d), "l1": (ox + w, oy, oz + d), "l2": (ox + w, oy + h, oz + d),
                "l3": (ox, oy + h, oz + d),
            }
            cols = dict(u0=u, u1=u + d, u2=u + d + w, u22=u + d + 2 * w, u3=u + 2 * d + w, u4=u + 2 * d + 2 * w,
                        v0=v, v1=v + d, v2=v + d + h)
            for verts, (ua, va, ub, vb) in FACE_DEFS.values():
                uva, vva, uvb, vvb = cols[ua], cols[va], cols[ub], cols[vb]
                uvs = [(uvb, vva), (uva, vva), (uva, vvb), (uvb, vvb)]
                pts = []
                for name in verts:
                    q = rot_y(corners[name], ry)
                    m = (q[0] + px, q[1] + py, q[2] + pz)
                    pts.append((m[0], -m[1] + 24.0, -m[2]))  # entity flip: y up, front toward +z
                out.append((pts, uvs))
    return out


def render(model, img, yaw, pitch, k, door_deg=0.0):
    qs = quads(model, door_deg)
    cy, sy = math.cos(math.radians(yaw)), math.sin(math.radians(yaw))
    cp, sp = math.cos(math.radians(pitch)), math.sin(math.radians(pitch))

    def view(p):
        x, y, z = p
        x, z = x * cy + z * sy, -x * sy + z * cy
        y, z = y * cp - z * sp, y * sp + z * cp
        return x, y, z

    W, H = int(24 * k), int(38 * k)
    cx, cyy = W / 2, H - 3 * k
    zbuf = [[-1e9] * W for _ in range(H)]
    out = [[(30, 32, 36, 255)] * W for _ in range(H)]
    light = (0.35, 0.8, 0.5)
    ln = math.sqrt(sum(c * c for c in light))
    light = tuple(c / ln for c in light)
    tex_w = len(img[0])
    for pts, uvs in qs:
        vp = [view(p) for p in pts]
        e1 = [vp[1][i] - vp[0][i] for i in range(3)]
        e2 = [vp[2][i] - vp[0][i] for i in range(3)]
        n = (e1[1] * e2[2] - e1[2] * e2[1], e1[2] * e2[0] - e1[0] * e2[2], e1[0] * e2[1] - e1[1] * e2[0])
        nl = math.sqrt(sum(c * c for c in n)) or 1
        n = tuple(c / nl for c in n)
        shade = 0.55 + 0.45 * abs(sum(n[i] * light[i] for i in range(3)))
        sp2 = [(cx + p[0] * k, cyy - p[1] * k, p[2]) for p in vp]
        for tri in ((0, 1, 2), (0, 2, 3)):
            a, b, c = (sp2[i] for i in tri)
            ua, ub, uc = (uvs[i] for i in tri)
            area = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0])
            if abs(area) < 1e-9:
                continue
            x0, x1 = max(0, int(min(a[0], b[0], c[0]))), min(W - 1, int(max(a[0], b[0], c[0])) + 1)
            y0, y1 = max(0, int(min(a[1], b[1], c[1]))), min(H - 1, int(max(a[1], b[1], c[1])) + 1)
            for y in range(y0, y1 + 1):
                for x in range(x0, x1 + 1):
                    px, py = x + 0.5, y + 0.5
                    w0 = ((b[0] - px) * (c[1] - py) - (b[1] - py) * (c[0] - px)) / area
                    w1 = ((c[0] - px) * (a[1] - py) - (c[1] - py) * (a[0] - px)) / area
                    w2 = 1 - w0 - w1
                    if w0 < 0 or w1 < 0 or w2 < 0:
                        continue
                    z = w0 * a[2] + w1 * b[2] + w2 * c[2]
                    if z <= zbuf[y][x]:
                        continue
                    tu = (w0 * ua[0] + w1 * ub[0] + w2 * uc[0]) * PX
                    tv = (w0 * ua[1] + w1 * ub[1] + w2 * uc[1]) * PX
                    ti, tj = min(tex_w - 1, max(0, int(tu))), min(tex_w - 1, max(0, int(tv)))
                    r, g, bb, al = img[tj][ti]
                    if al == 0:
                        continue
                    zbuf[y][x] = z
                    out[y][x] = (int(r * shade), int(g * shade), int(bb * shade), 255)
    return out


def hstack(images):
    h = max(len(i) for i in images)
    rows = []
    for y in range(h):
        row = []
        for im in images:
            row += im[y] if y < len(im) else [(30, 32, 36, 255)] * len(im[0])
        rows.append(row)
    return rows


def main():
    out_dir = Path(sys.argv[1])
    k = int(sys.argv[2]) if len(sys.argv) > 2 else 12
    out_dir.mkdir(parents=True, exist_ok=True)
    for era in ERAS:
        model, img = build(era)
        views = [
            render(model, img, 0, 0, k),
            render(model, img, 90, 0, k),
            render(model, img, -35, 15, k),
            render(model, img, 35, 15, k, door_deg=60),
        ]
        (out_dir / f"{era.id}_preview.png").write_bytes(png_bytes(hstack(views)))
        print(f"preview {era.id}")


if __name__ == "__main__":
    main()
