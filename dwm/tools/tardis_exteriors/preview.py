#!/usr/bin/env python3
"""Offline orthographic preview of the generated exteriors (not shipped; for iteration/review).

Rasterises every cube face with the vanilla `ModelPart.Cube` UV mapping, a z-buffer and simple
directional shading, then writes one sheet per era: front, side, three-quarter, and three-quarter
with the door open. Any other entity model ids given on the command line (for example
`seventh_doctor_box`) are rendered from the shipped JSON + PNG and appended to every sheet, to
judge the generated boxes against the hand-built ones.

Usage:  python3 dwm/tools/tardis_exteriors/preview.py OUT_DIR [px_per_unit] [reference_id ...]
"""
from __future__ import annotations

import json
import math
import re
import struct
import sys
import zlib
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from eras import ERAS  # noqa: E402
from generate import MODEL_DIR, TEXTURE_DIR, build, png_bytes  # noqa: E402

BACKGROUND = (30, 32, 36, 255)

FACE_DEFS = {
    # verts by corner name, (ua, va, ub, vb) in terms of the net columns/rows
    "down": (("l1", "l0", "t0", "t1"), ("u1", "v0", "u2", "v1")),
    "up": (("t2", "t3", "l3", "l2"), ("u2", "v1", "u22", "v0")),
    "west": (("t0", "l0", "l3", "t3"), ("u0", "v1", "u1", "v2")),
    "north": (("t1", "t0", "t3", "t2"), ("u1", "v1", "u2", "v2")),
    "east": (("l1", "t1", "t2", "l2"), ("u2", "v1", "u3", "v2")),
    "south": (("l0", "l1", "l2", "l3"), ("u3", "v1", "u4", "v2")),
}


def read_png(path: Path):
    """Minimal PNG reader (8-bit RGB / RGBA / palette, non-interlaced) -> rows of RGBA tuples."""
    data = path.read_bytes()
    pos, idat, palette, alpha = 8, b"", None, b""
    while pos < len(data):
        length, tag = struct.unpack(">I4s", data[pos:pos + 8])
        body = data[pos + 8:pos + 8 + length]
        if tag == b"IHDR":
            w, h, depth, ctype, _, _, interlace = struct.unpack(">IIBBBBB", body)
            if depth != 8 or interlace or ctype not in (2, 3, 6):
                raise ValueError(f"{path.name}: unsupported PNG (depth {depth}, colour type {ctype})")
        elif tag == b"PLTE":
            palette = body
        elif tag == b"tRNS":
            alpha = body
        elif tag == b"IDAT":
            idat += body
        pos += 12 + length
    bpp = {2: 3, 3: 1, 6: 4}[ctype]
    raw = zlib.decompress(idat)
    stride = w * bpp
    prev = bytearray(stride)
    rows = []
    for y in range(h):
        f = raw[y * (stride + 1)]
        line = bytearray(raw[y * (stride + 1) + 1:(y + 1) * (stride + 1)])
        for i in range(stride):
            a = line[i - bpp] if i >= bpp else 0
            b = prev[i]
            c = prev[i - bpp] if i >= bpp else 0
            if f == 1:
                line[i] = (line[i] + a) & 255
            elif f == 2:
                line[i] = (line[i] + b) & 255
            elif f == 3:
                line[i] = (line[i] + (a + b) // 2) & 255
            elif f == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                line[i] = (line[i] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 255
        prev = line
        if ctype == 6:
            rows.append([tuple(line[i:i + 4]) for i in range(0, stride, 4)])
        elif ctype == 2:
            rows.append([(line[i], line[i + 1], line[i + 2], 255) for i in range(0, stride, 3)])
        else:
            rows.append([(*palette[3 * i:3 * i + 3], alpha[i] if i < len(alpha) else 255) for i in line])
    return rows


def rotation(rx, ry, rz):
    """ModelPart rotation matrix (Z * Y * X), angles in degrees."""
    cx, sx = math.cos(math.radians(rx)), math.sin(math.radians(rx))
    cy, sy = math.cos(math.radians(ry)), math.sin(math.radians(ry))
    cz, sz = math.cos(math.radians(rz)), math.sin(math.radians(rz))
    return [
        [cz * cy, cz * sy * sx - sz * cx, cz * sy * cx + sz * sx],
        [sz * cy, sz * sy * sx + cz * cx, sz * sy * cx - cz * sx],
        [-sy, cy * sx, cy * cx],
    ]


def mat_mul(a, b):
    return [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]


def apply(m, p):
    return tuple(m[i][0] * p[0] + m[i][1] * p[1] + m[i][2] * p[2] for i in range(3))


def door_angles(model, progress):
    """Part path -> extra Y rotation, from the model's `door_progress * N` bindings."""
    out = {}
    for b in model.get("animation", {}).get("bindings", []):
        m = re.fullmatch(r"door_progress \* (\d+(?:\.\d+)?)", b.get("value", ""))
        if m and b.get("channel") == "rot_y":
            out[b.get("part", "")] = progress * float(m.group(1))
    return out


def quads(model, door_progress=0.0):
    """World-space (y up) textured quads: list of (4 points, 4 uvs)."""
    doors = door_angles(model, door_progress)
    out = []

    def walk(part, matrix, offset, path):
        path = f"{path}/{part['name']}" if path else part["name"]
        rx, ry, rz = part.get("rotation") or (0.0, 0.0, 0.0)
        local = rotation(rx, ry + doors.get(path, 0.0), rz)
        pivot = apply(matrix, part.get("pivot") or (0.0, 0.0, 0.0))
        offset = tuple(offset[i] + pivot[i] for i in range(3))
        matrix = mat_mul(matrix, local)
        for cube in part.get("cubes", []):
            (ox, oy, oz), (w, h, d) = cube["origin"], cube["size"]
            g = cube.get("inflate", 0.0)
            u, v = cube["uv"]
            x0, y0, z0, x1, y1, z1 = ox - g, oy - g, oz - g, ox + w + g, oy + h + g, oz + d + g
            if cube.get("mirror"):
                x0, x1 = x1, x0
            corners = {
                "t0": (x0, y0, z0), "t1": (x1, y0, z0), "t2": (x1, y1, z0), "t3": (x0, y1, z0),
                "l0": (x0, y0, z1), "l1": (x1, y0, z1), "l2": (x1, y1, z1), "l3": (x0, y1, z1),
            }
            cols = dict(u0=u, u1=u + d, u2=u + d + w, u22=u + d + 2 * w, u3=u + 2 * d + w, u4=u + 2 * d + 2 * w,
                        v0=v, v1=v + d, v2=v + d + h)
            for verts, (ua, va, ub, vb) in FACE_DEFS.values():
                uva, vva, uvb, vvb = cols[ua], cols[va], cols[ub], cols[vb]
                uvs = [(uvb, vva), (uva, vva), (uva, vvb), (uvb, vvb)]
                pts = []
                for name in verts:
                    q = apply(matrix, corners[name])
                    # entity flip: y up, front toward +z
                    pts.append((q[0] + offset[0], 24.0 - (q[1] + offset[1]), -(q[2] + offset[2])))
                out.append((pts, uvs))
        for child in part.get("children", []):
            walk(child, matrix, offset, path)

    identity = [[1.0, 0.0, 0.0], [0.0, 1.0, 0.0], [0.0, 0.0, 1.0]]
    for part in model["parts"]:
        walk(part, identity, (0.0, 0.0, 0.0), "")
    return out


def render(model, img, yaw, pitch, k, door_progress=0.0):
    qs = quads(model, door_progress)
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
    out = [[BACKGROUND] * W for _ in range(H)]
    light = (0.35, 0.8, 0.5)
    ln = math.sqrt(sum(c * c for c in light))
    light = tuple(c / ln for c in light)
    tex_h, tex_w = len(img), len(img[0])
    su, sv = tex_w / model["texture_width"], tex_h / model["texture_height"]
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
                    tu = (w0 * ua[0] + w1 * ub[0] + w2 * uc[0]) * su
                    tv = (w0 * ua[1] + w1 * ub[1] + w2 * uc[1]) * sv
                    ti, tj = min(tex_w - 1, max(0, int(tu))), min(tex_h - 1, max(0, int(tv)))
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
            row += im[y] if y < len(im) else [BACKGROUND] * len(im[0])
        rows.append(row)
    return rows


def shipped(model_id: str):
    """(model json, RGBA rows) of an entity model already in the resources."""
    model = json.loads((MODEL_DIR / f"{model_id}.json").read_text())
    texture = model["texture"].split(":", 1)[1].removeprefix("textures/entity/")
    return model, read_png(TEXTURE_DIR / texture)


def main():
    out_dir = Path(sys.argv[1])
    k = int(sys.argv[2]) if len(sys.argv) > 2 else 12
    out_dir.mkdir(parents=True, exist_ok=True)
    references = [render(*shipped(ref), -35, 15, k) for ref in sys.argv[3:]]
    for era in ERAS:
        model, img = build(era)
        views = [
            render(model, img, 0, 0, k),
            render(model, img, 90, 0, k),
            render(model, img, -35, 15, k),
            render(model, img, 35, 15, k, door_progress=1.0),
        ]
        (out_dir / f"{era.id}_preview.png").write_bytes(png_bytes(hstack(views + references)))
        print(f"preview {era.id}")


if __name__ == "__main__":
    main()
