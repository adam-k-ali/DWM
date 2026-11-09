"""Per-face painters. Every painter fills one face rectangle of the atlas (pixel space).

The finish follows the 1st-7th Doctor boxes: flat paint with soft, low-contrast tonal clouds and no
per-pixel grain, so the geometry carries the detail. Painted detail is limited to what cannot be
modelled at this scale (sign lettering, notice, badge, pane tints).

Noise is a pure hash of the atlas pixel and the era, so output is deterministic and shared nets are
painted once.
"""
from __future__ import annotations

from eras import Era
from font import text_pixels, text_width

Image = list[list[tuple[int, int, int, int]]]

# St John Ambulance roundel, simplified to 8 x 8: W ring and cross, K disc.
BADGE = [
    "..WWWW..",
    ".WKKKKW.",
    "WKKWWKKW",
    "WKWWWWKW",
    "WKWWWWKW",
    "WKKWWKKW",
    ".WKKKKW.",
    "..WWWW..",
]


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


def put(img: Image, x: int, y: int, c) -> None:
    img[y][x] = (c[0], c[1], c[2], 255)


class Painter:
    def __init__(self, era: Era, img: Image):
        self.era = era
        self.img = img
        self.seed = era.year

    # ------------------------------------------------------------------ base finish
    def tone(self, x: int, y: int, base, mottle: float | None = None):
        """Flat paint with soft tonal clouds (two octaves of smooth noise)."""
        m = self.era.mottle if mottle is None else mottle
        n = vnoise(x, y, 11.0, self.seed) * 0.65 + vnoise(x, y, 5.0, self.seed + 5) * 0.35
        return scale(base, 1.0 + m * (n - 0.5) * 2.0)

    def fill(self, rect, base, mottle=None, sunk=False):
        """sunk: shade the face as a recess (shadow under the top/left edge, light on the bottom/right)."""
        x0, y0, w, h = rect
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                c = self.tone(x, y, base, mottle)
                if sunk and w > 2 and h > 2:
                    if y == y0 or x == x0:
                        c = scale(c, 0.84)
                    elif y == y0 + h - 1 or x == x0 + w - 1:
                        c = scale(c, 1.07)
                put(self.img, x, y, c)

    def inside(self, face: str) -> float:
        """Faces that look into the box are a touch darker."""
        return 0.85 if face == "south" else 1.0

    # ------------------------------------------------------------------ roles
    def frame(self, rect, face):
        self.fill(rect, scale(self.era.body, self.inside(face)))

    def post(self, rect, face):
        self.fill(rect, scale(self.era.body, 1.04))

    def panel(self, rect, face):
        self.fill(rect, scale(self.era.body, 0.9 * self.inside(face)), sunk=face == "north")

    def panel_badge(self, rect, face):
        self.panel(rect, face)
        x0, y0, w, h = rect
        bx, by = x0 + (w - len(BADGE[0])) // 2, y0 + 3
        white, black = scale(self.era.sign_text, 0.95), (20, 20, 24)
        for j, row in enumerate(BADGE):
            for i, ch in enumerate(row):
                if ch != ".":
                    put(self.img, bx + i, by + j, white if ch == "W" else black)

    def bead(self, rect, face):
        self.fill(rect, self.era.mullion, mottle=self.era.mottle * 0.5)

    def glass(self, rect, face):
        """Pane tints behind the modelled glazing bars: 2 rows of 3 panes (3 x 7 px) with 1 px bars."""
        e = self.era
        x0, y0, w, h = rect
        pw, ph, bar = 3, 7, 1
        dark = scale(e.glass, 0.45)
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                lx, ly = (x - x0) % (pw + bar), (y - y0) % (ph + bar)
                if lx >= pw or ly >= ph:
                    put(self.img, x, y, dark)  # hidden behind the bars
                    continue
                c = scale(e.glass, 1.0 - 0.02 * ly)
                if lx == 0 or ly == 0:
                    c = scale(c, 0.86)  # shadow of the frame and bars along the top/left of each pane
                if e.hammered and (y - y0) // (ph + bar) == 1 and (lx + ly) % 2 == 1:
                    c = scale(c, 0.9)
                if face == "south":
                    c = scale(c, 0.8)
                put(self.img, x, y, c)

    def sign(self, rect, face):
        e = self.era
        x0, y0, w, h = rect
        self.fill(rect, (20, 20, 24), mottle=0.03)
        ty = y0 + (h - 5) // 2
        left, right = text_width("POLICE"), text_width("BOX")
        for dx, dy in text_pixels("POLICE"):
            put(self.img, x0 + 1 + dx, ty + dy, e.sign_text)
        for dx, dy in text_pixels("BOX"):
            put(self.img, x0 + w - 1 - right + dx, ty + dy, e.sign_text)
        # "PUBLIC" over "CALL" is too small to letter: two short bars of dimmer type
        m0, m1 = x0 + 1 + left + 2, x0 + w - 1 - right - 2
        dim = scale(e.sign_text, 0.62)
        for x in range(m0, m1):
            put(self.img, x, ty + 1, dim)
        for x in range(m0 + 1, m1 - 1):
            put(self.img, x, ty + 3, dim)

    def notice(self, rect, face):
        """Telephone notice: heading bar, three lines of copy, and the PULL TO OPEN line."""
        e = self.era
        x0, y0, w, h = rect
        self.fill(rect, e.notice, mottle=0.02)
        ink, faint = e.notice_text, tuple((a + 2 * b) // 3 for a, b in zip(e.notice_text, e.notice))
        for x in range(x0 + 2, x0 + w - 2):
            put(self.img, x, y0 + 2, ink)
            put(self.img, x, y0 + 3, ink)
        for row, inset in ((5, 1), (7, 2), (9, 1)):
            for x in range(x0 + 1 + inset // 2, x0 + w - 1 - inset):
                put(self.img, x, y0 + row, faint)
        for x in range(x0 + 2, x0 + w - 2):
            put(self.img, x, y0 + h - 3, ink)

    def notice_edge(self, rect, face):
        self.fill(rect, scale(self.era.notice, 0.8), mottle=0.02)

    def lock(self, rect, face):
        x0, y0, w, h = rect
        self.fill(rect, self.era.handle, mottle=0.0)
        put(self.img, x0 + w // 2, y0 + h // 2, (24, 24, 28))

    def handle(self, rect, face):
        x0, y0, w, h = rect
        self.fill(rect, scale(self.era.handle, 0.9), mottle=0.0)
        if face == "north":
            for y in range(y0, y0 + h):
                put(self.img, x0, y, self.era.handle)

    def lens(self, rect, face):
        e = self.era
        x0, y0, w, h = rect
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                k = 1.0
                if e.lamp.fresnel and face not in ("up", "down") and (y - y0) % 2 == 1:
                    k = 0.9
                put(self.img, x, y, scale(e.lens, k))

    def roof(self, rect, face):
        self.fill(rect, scale(self.era.body, 0.88 * self.era.shade))

    def plinth(self, rect, face):
        self.fill(rect, scale(self.era.body, 0.8 * self.era.shade))

    def paint(self, role: str, rect, face: str) -> None:
        getattr(self, role)(rect, face)
