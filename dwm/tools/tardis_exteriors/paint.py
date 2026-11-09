"""Per-face painters. Every painter fills one face rectangle of the atlas (pixel space).

Noise is a pure hash of the atlas pixel and the era, so output is deterministic and shared nets
are painted once.
"""
from __future__ import annotations

from eras import Era
from font import text_pixels, text_width

Image = list[list[tuple[int, int, int, int]]]


def h2(x: int, y: int, s: int) -> float:
    n = (x * 374761393 + y * 668265263 + s * 982451653) & 0xFFFFFFFF
    n = ((n ^ (n >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((n ^ (n >> 16)) & 0xFFFFFF) / 0x1000000


def vnoise(x: float, y: float, cell: float, s: int) -> float:
    gx, gy = x / cell, y / cell
    ix, iy = int(gx // 1), int(gy // 1)
    fx, fy = gx - ix, gy - iy
    fx, fy = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy)
    a, b = h2(ix, iy, s), h2(ix + 1, iy, s)
    c, d = h2(ix, iy + 1, s), h2(ix + 1, iy + 1, s)
    return (a + (b - a) * fx) * (1 - fy) + (c + (d - c) * fx) * fy


def scale(c, k):
    return tuple(max(0, min(255, int(round(v * k)))) for v in c)


def mix(a, b, t):
    return tuple(max(0, min(255, int(round(x + (y - x) * t)))) for x, y in zip(a, b))


def put(img: Image, x: int, y: int, c) -> None:
    img[y][x] = (c[0], c[1], c[2], 255)


class Painter:
    def __init__(self, era: Era, img: Image):
        self.era = era
        self.img = img
        self.seed = era.year

    # ------------------------------------------------------------------ base finishes
    def paint_px(self, x: int, y: int, base, grain: bool = True, wear: float | None = None):
        """Painted timber: vertical grain, fine speckle and patchy fading."""
        e = self.era
        wear = e.wear if wear is None else wear
        k = 1.0
        if grain:
            k *= 0.96 + 0.06 * h2(x, 0, self.seed + 7)  # per-column tone
            if h2(x, y // 3, self.seed + 11) < 0.12:
                k *= 0.93  # short grain streaks
        k *= 0.97 + 0.06 * h2(x, y, self.seed)
        c = scale(base, k)
        # faded paint runs in vertical streaks (rain-washed), plus a little fine mottling
        fade = vnoise(x, y / 4.0, 5.0, self.seed + 3) * 0.75 + vnoise(x, y, 2.0, self.seed + 5) * 0.25
        fade = max(0.0, fade - (1.0 - wear)) / max(wear, 1e-6) if wear > 0 else 0.0
        if fade > 0:
            c = mix(c, (140, 162, 176), 0.28 * fade * wear + 0.04 * fade)
        if wear > 0.3 and h2(x, y, self.seed + 13) < 0.04 * wear:
            c = scale(c, 0.78)  # chips
        return c

    def fill(self, rect, base, grain=True, wear=None, edges=0):
        x0, y0, w, h = rect
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                c = self.paint_px(x, y, base, grain, wear)
                if edges and w > 2 and h > 2:
                    if y == y0 or x == x0:
                        c = scale(c, 1.0 + 0.12 * edges)
                    elif y == y0 + h - 1 or x == x0 + w - 1:
                        c = scale(c, 1.0 - 0.18 * edges)
                put(self.img, x, y, c)

    # ------------------------------------------------------------------ roles
    def frame(self, rect, face):
        self.fill(rect, self.era.body, edges=1 if face == "north" else 0)

    def post(self, rect, face):
        x0, y0, w, h = rect
        self.fill(rect, self.era.body)
        if face in ("north", "south", "east", "west") and w >= 4:
            for y in range(y0, y0 + h):  # rounded-arris shading down both edges
                put(self.img, x0, y, scale(self.img[y][x0], 1.12))
                put(self.img, x0 + w - 1, y, scale(self.img[y][x0 + w - 1], 0.8))

    def ground(self, rect, face):
        self.fill(rect, scale(self.era.body, 0.72), wear=self.era.wear * 0.5)

    def leaf_back(self, rect, face):
        self.fill(rect, scale(self.era.body, 0.85), wear=0.0)

    def field(self, rect, face):
        self.fill(rect, self.era.body, edges=1.4 if face == "north" else 0)

    def field_badge(self, rect, face):
        self.field(rect, face)
        x0, y0, w, h = rect
        cx, cy = x0 + w / 2 - 0.5, y0 + h * 0.42
        white, black = (238, 238, 236), (16, 16, 18)
        r_out, r_in = min(w, h) * 0.42, min(w, h) * 0.32
        for y in range(y0 + 1, y0 + h - 1):
            for x in range(x0 + 1, x0 + w - 1):
                d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
                if d <= r_in:
                    put(self.img, x, y, black)
                elif d <= r_out:
                    put(self.img, x, y, white)
        # eight-pointed cross: a plus with flared diagonal tips
        icx, icy = int(round(cx)), int(round(cy))
        for t in range(-2, 3):
            put(self.img, icx + t, icy, white)
            put(self.img, icx, icy + t, white)
        for dx, dy in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
            put(self.img, icx + 2 * dx, icy + dy, white)
            put(self.img, icx + dx, icy + 2 * dy, white)
        # caption line under the roundel
        ty = int(cy + r_out) + 2
        if ty < y0 + h - 1:
            for x in range(x0 + 2, x0 + w - 2):
                if h2(x, ty, self.seed + 21) < 0.7:
                    put(self.img, x, ty, white)

    def bead(self, rect, face):
        self.fill(rect, self.era.mullion, wear=self.era.wear * 0.6, edges=0.6 if face == "north" else 0)

    def glass(self, rect, face):
        e = self.era
        x0, y0, w, h = rect
        cols, rows, bar = 2, 3, 2
        pw = (w - bar * (cols - 1)) // cols
        ph = (h - bar * (rows - 1)) // rows
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                lx, ly = x - x0, y - y0
                ci, cj = lx // (pw + bar), ly // (ph + bar)
                in_bar = (lx % (pw + bar)) >= pw or (ly % (ph + bar)) >= ph
                if in_bar:
                    put(self.img, x, y, self.paint_px(x, y, e.mullion, grain=False, wear=e.wear * 0.5))
                    continue
                py = ly % (ph + bar)
                k = 1.0 - 0.10 * py / max(ph - 1, 1) - 0.03 * h2(x, y, self.seed + 31)
                c = scale(e.glass, k)
                if e.hammered and cj == rows - 1 and ci in (0, cols - 1):
                    if (lx + ly) % 2 == 0 or h2(x, y, self.seed + 33) < 0.3:
                        c = scale(c, 0.86)
                if (lx % (pw + bar)) == 0 or py == 0:
                    c = scale(c, 0.8)  # putty shadow along the top/left of each pane
                put(self.img, x, y, c)

    def sign(self, rect, face):
        e = self.era
        x0, y0, w, h = rect
        black = (14, 14, 16)
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                put(self.img, x, y, scale(black, 0.95 + 0.1 * h2(x, y, self.seed + 41)))
        ty = y0 + (h - 5) // 2
        left = text_width("POLICE")
        right = text_width("BOX")
        mid = w - left - right - 2 - 4  # 1px margins, 2px gaps either side of PUBLIC/CALL
        for dx, dy in text_pixels("POLICE"):
            put(self.img, x0 + 1 + dx, ty + dy, e.sign_text)
        for dx, dy in text_pixels("BOX"):
            put(self.img, x0 + w - 1 - right + dx, ty + dy, e.sign_text)
        # "PUBLIC" over "CALL", too small to letter at this density: two short lines of small type
        mx = x0 + 1 + left + 2
        dim = scale(e.sign_text, 0.7)
        for row, length in ((ty + 1, mid), (ty + 3, mid - 2)):
            off = (mid - length) // 2
            for i in range(length):
                put(self.img, mx + off + i, row, dim)

    def notice(self, rect, face):
        e = self.era
        x0, y0, w, h = rect
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                c = scale(e.notice, 0.97 + 0.05 * h2(x, y, self.seed + 51))
                if x in (x0, x0 + w - 1) or y in (y0, y0 + h - 1):
                    c = scale(e.body, 0.9)
                put(self.img, x, y, c)
        ink = e.notice_text
        # heading (two bold rows), body copy (thin rows), and the PULL TO OPEN line
        lines = [(y0 + 2, 2, 0.9), (y0 + 5, 1, 0.6), (y0 + 7, 1, 0.6), (y0 + 9, 1, 0.6), (y0 + 11, 1, 0.55),
                 (y0 + h - 3, 1, 0.85)]
        for ly, thick, density in lines:
            if ly + thick > y0 + h - 1:
                continue
            for x in range(x0 + 2, x0 + w - 2):
                word_gap = h2(x // 3, ly, self.seed + 53) < 0.18
                if not word_gap and h2(x, ly, self.seed + 55) < density:
                    for t in range(thick):
                        put(self.img, x, ly + t, ink)

    def lock(self, rect, face):
        x0, y0, w, h = rect
        self.fill(rect, self.era.handle, grain=False, wear=0.0)
        put(self.img, x0 + w // 2, y0 + h // 2, (20, 20, 22))

    def handle(self, rect, face):
        self.fill(rect, self.era.handle, grain=False, wear=0.0, edges=1)

    def lens(self, rect, face):
        e = self.era
        x0, y0, w, h = rect
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                k = 1.0
                if e.lamp.fresnel and face not in ("up", "down"):
                    k = 0.84 if (y - y0) % 2 == 1 else 1.0
                    if x in (x0, x0 + w - 1):
                        k *= 0.9
                else:
                    k = 0.95 + 0.05 * h2(x, y, self.seed + 61)
                put(self.img, x, y, scale(e.lens, k))

    def roof(self, rect, face):
        self.fill(rect, scale(self.era.body, 0.88 * self.era.shade), wear=min(1.0, self.era.wear * 1.4 + 0.05))

    def plinth(self, rect, face):
        self.fill(rect, scale(self.era.body, 0.8 * self.era.shade), wear=min(1.0, self.era.wear * 1.2))

    def paint(self, role: str, rect, face: str) -> None:
        getattr(self, role)(rect, face)
