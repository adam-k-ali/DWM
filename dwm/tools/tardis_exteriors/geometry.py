"""Police-box geometry shared by every era, built the way the 1st-7th Doctor boxes are.

Construction rules (the classic boxes' design language):
  * detail is geometry, not paint: stiles, rails, sunk panels, window frames and glazing bars are
    all separate cubes;
  * relief is chunky: structural cubes are a full unit thick and sit 0.25-0.5 apart in depth;
  * every coordinate is a multiple of GRID (one texture pixel), so no face is thinner than a pixel
    and painted edges land on geometry edges;
  * corner posts are two overlapping boxes that notch the outer corner.

Boxes are authored in "height space": x and z in model units, h = height above the ground
(model y = 24 - h). The front of the box faces -Z; the door leaves fill the BOTI aperture
x -5..5, h 1..23 (the hinge pivots sit at z -5.5).

Depth planes on a wall or door leaf (front = more negative z):
    glass     -5.75        behind everything
    panel     -6.0         sunk panels
    bars      -6.0         glazing bars, in front of the glass
    notice    -6.25        telephone notice board, on its panel
    window    -6.25        window frame
    frame     -6.5         stiles and rails
    jamb      -6.75        door jambs, lintel, centre astragal, handle
    post      -post_outer  corner posts (the sign box is at era.sign_front)
"""
from __future__ import annotations

from dataclasses import dataclass, field

from eras import Era

GRID = 0.25

FRAME = (-6.5, -5.5)
PANEL = (-6.0, -5.0)
WINDOW_FRAME = (-6.25, -5.5)
BARS = (-6.0, -5.75)
GLASS = (-5.75, -5.0)
NOTICE = (-6.25, -6.0)
PROUD = (-6.75, -6.5)  # astragal, handle, lock: on the face of the frame
JAMB = (-6.75, -5.0)

DOOR_BOTTOM, DOOR_TOP = 1.0, 23.0
LINTEL_H1 = 23.5
SIGN_H1 = 25.5
POST_TOP = 26.0
POST_INNER = 5.5

# Leaf layout, measured outward from the centre line (0) to the hinge side (5).
STILE_IN = (0.0, 1.0)
STILE_OUT = (4.0, 5.0)
PANELS = [(2.0, 6.0), (7.0, 11.0), (12.0, 16.0)]
RAILS = [(1.0, 2.0), (6.0, 7.0), (11.0, 12.0), (16.0, 17.0), (21.5, 23.0)]
WINDOW = (17.0, 21.5)
# Window: a 0.25 frame, then 2 x 3 panes of 1 x 1 separated by 0.5 bars.
PANE = 1.0
BAR = 0.5


@dataclass
class Box:
    x0: float
    x1: float
    h0: float
    h1: float
    z0: float
    z1: float
    role: str  # default paint role for every face
    faces: dict[str, str] = field(default_factory=dict)  # per-face overrides

    @property
    def size(self) -> tuple[float, float, float]:
        return self.x1 - self.x0, self.h1 - self.h0, self.z1 - self.z0

    def roles(self) -> tuple[tuple[str, str], ...]:
        from uvpack import FACES
        return tuple((f, self.faces.get(f, self.role)) for f in FACES)

    def key(self):
        w, h, d = self.size
        return (round(w, 3), round(h, 3), round(d, 3), self.roles())


def box(x0, x1, h0, h1, z0, z1, role, **faces) -> Box:
    xa, xb = sorted((x0, x1))
    za, zb = sorted((z0, z1))
    for v in (xa, xb, h0, h1, za, zb):
        if abs(v / GRID - round(v / GRID)) > 1e-6:
            raise ValueError(f"{role} box coordinate {v} is off the {GRID} grid")
    if min(xb - xa, h1 - h0, zb - za) < GRID - 1e-6:
        raise ValueError(f"{role} box is thinner than {GRID}")
    return Box(xa, xb, h0, h1, za, zb, role, faces)


def leaf(era: Era, side: int, kind: str) -> list[Box]:
    """One door leaf / wall half. side +1 spans x 0..5, -1 spans x -5..0.

    kind: "wall", "door_open" (handle, lock, optional badge) or "door_fixed" (notice, astragal).
    """

    def X(a, b):
        return sorted((side * a, side * b))

    out = []
    for a, b in (STILE_IN, STILE_OUT):
        out.append(box(*X(a, b), DOOR_BOTTOM, DOOR_TOP, *FRAME, "frame"))
    for h0, h1 in RAILS:
        out.append(box(*X(1.0, 4.0), h0, h1, *FRAME, "frame"))

    for i, (h0, h1) in enumerate(PANELS):
        top_panel = i == len(PANELS) - 1
        if top_panel and kind == "door_open" and era.badge:
            out.append(box(*X(1.0, 4.0), h0, h1, *PANEL, "panel", north="panel_badge"))
        else:
            out.append(box(*X(1.0, 4.0), h0, h1, *PANEL, "panel"))
        if top_panel and kind == "door_fixed":
            out.append(box(*X(1.25, 3.75), h0 + 0.25, h1 - 0.25, *NOTICE, "notice_edge", north="notice"))

    w0, w1 = WINDOW
    out += [
        box(*X(1.0, 1.25), w0, w1, *WINDOW_FRAME, "bead"),
        box(*X(3.75, 4.0), w0, w1, *WINDOW_FRAME, "bead"),
        box(*X(1.25, 3.75), w0, w0 + 0.25, *WINDOW_FRAME, "bead"),
        box(*X(1.25, 3.75), w1 - 0.25, w1, *WINDOW_FRAME, "bead"),
        box(*X(1.25, 3.75), w0 + 0.25, w1 - 0.25, *GLASS, "bead", north="glass", south="glass"),
        # glazing bars: one upright, and two cross bars split either side of it
        box(*X(1.25 + PANE, 1.25 + PANE + BAR), w0 + 0.25, w1 - 0.25, *BARS, "bead"),
    ]
    for row in (1, 2):
        h = w0 + 0.25 + row * PANE + (row - 1) * BAR
        out.append(box(*X(1.25, 1.25 + PANE), h, h + BAR, *BARS, "bead"))
        out.append(box(*X(1.25 + PANE + BAR, 3.75), h, h + BAR, *BARS, "bead"))

    if kind == "door_open":
        out.append(box(*X(0.25, 0.75), 10.75, 11.75, *PROUD, "handle"))
        out.append(box(*X(0.25, 0.75), 12.5, 13.0, *PROUD, "handle", north="lock"))
    if kind == "door_fixed":
        out.append(box(-0.25, 0.25, DOOR_BOTTOM, DOOR_TOP, *PROUD, "frame"))
    return out


def surround(era: Era) -> list[Box]:
    """Fixed joinery around one doorway: jambs, lintel, the sign box and its hood."""
    front = era.sign_front
    return [
        box(-POST_INNER, -5.0, DOOR_BOTTOM, DOOR_TOP, *JAMB, "frame"),
        box(5.0, POST_INNER, DOOR_BOTTOM, DOOR_TOP, *JAMB, "frame"),
        box(-POST_INNER, POST_INNER, DOOR_TOP, LINTEL_H1, *JAMB, "frame"),
        box(-POST_INNER, POST_INNER, LINTEL_H1, SIGN_H1, front, -5.5, "frame", north="sign"),
        box(-POST_INNER, POST_INNER, SIGN_H1, POST_TOP, front - 0.25, -5.5, "frame"),
    ]


def wall(era: Era) -> list[Box]:
    return leaf(era, +1, "wall") + leaf(era, -1, "wall") + surround(era)


def posts(era: Era) -> list[Box]:
    """Each corner post is two overlapping boxes, leaving a 0.25 notch down the outer corner."""
    p = era.post_outer
    out = []
    for sx in (-1, 1):
        for sz in (-1, 1):
            out.append(box(sx * POST_INNER, sx * p, DOOR_BOTTOM, POST_TOP,
                           sz * POST_INNER, sz * (p - 0.25), "post"))
            out.append(box(sx * POST_INNER, sx * (p - 0.25), DOOR_BOTTOM, POST_TOP,
                           sz * POST_INNER, sz * p, "post"))
    return out


def roof(era: Era) -> tuple[list[Box], float]:
    """The stepped roof stack; the first tier is the cornice. Returns (boxes, height of the top)."""
    out = []
    h = POST_TOP
    for i, (half, thick) in enumerate(era.roof_tiers):
        out.append(box(-half, half, h, h + thick, -half, half, "frame" if i == 0 else "roof", down="roof"))
        h += thick
    return out, h


def lamp(era: Era, base_h: float) -> list[Box]:
    L = era.lamp
    r = L.lens_half
    cap = r + 0.25
    out = [box(-cap, cap, base_h, base_h + 0.25, -cap, cap, "frame")]
    h0 = base_h + 0.25
    h1 = h0 + L.lens_h
    out.append(box(-r, r, h0, h1, -r, r, "lens"))
    if L.cage:
        for sx in (-1, 1):
            for sz in (-1, 1):
                out.append(box(sx * r, sx * cap, h0, h1, sz * r, sz * cap, "frame"))
    out.append(box(-cap, cap, h1, h1 + 0.25, -cap, cap, "frame"))
    if L.dome:
        out.append(box(-0.5, 0.5, h1 + 0.25, h1 + 0.5, -0.5, 0.5, "frame"))
        out.append(box(-0.25, 0.25, h1 + 0.5, h1 + 0.75, -0.25, 0.25, "frame"))
    else:
        out.append(box(-0.25, 0.25, h1 + 0.25, h1 + 0.5, -0.25, 0.25, "frame"))
    return out


def parts(era: Era) -> list[dict]:
    """Root parts in render order. Door parts carry their pivot; everything else pivots at (0,24,0)."""
    roof_boxes, top = roof(era)
    return [
        dict(name="base", boxes=[box(-7.5, 7.5, 0.0, 1.0, -7.5, 7.5, "plinth")]),
        dict(name="posts", boxes=posts(era)),
        dict(name="roof", boxes=roof_boxes),
        dict(name="lamp", boxes=lamp(era, top)),
        dict(name="front", boxes=surround(era)),
        dict(name="wall_left", boxes=wall(era), rotation=(0.0, -90.0, 0.0)),
        dict(name="wall_back", boxes=wall(era), rotation=(0.0, 180.0, 0.0)),
        dict(name="wall_right", boxes=wall(era), rotation=(0.0, 90.0, 0.0)),
        # LeftDoor hinges at x=+5 (viewer's right) and is the leaf that swings open.
        dict(name="LeftDoor", boxes=leaf(era, +1, "door_open"), pivot=(5.0, 24.0, -5.5)),
        dict(name="rightDoor", boxes=leaf(era, -1, "door_fixed"), pivot=(-5.0, 24.0, -5.5)),
    ]
