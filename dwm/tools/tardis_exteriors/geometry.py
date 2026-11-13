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
LINTEL_H1 = 23.25  # a narrow strip between the door top and the sign box
SIGN_H1 = 25.25  # sign box: 2 units (8 px) tall
FRIEZE_H1 = 26.0  # frieze band above the sign boxes; the roof starts here
POST_TOP = 26.25  # posts stand a little proud of the frieze, as corner caps
POST_INNER = 5.5
SIGN_HALF = 6.25  # sign boxes and frieze reach part-way across the posts, never past them

# Leaf layout, measured outward from the centre line (0) to the hinge side (5). The meeting stile is
# narrower than the hinge stile, as on the real doors.
STILE_IN = (0.0, 0.75)
STILE_OUT = (4.0, 5.0)
INNER = (STILE_IN[1], STILE_OUT[0])  # panels, rails and the window span this
PANELS = [(2.0, 6.0), (7.0, 11.0), (12.0, 16.0)]
RAILS = [(1.0, 2.0), (6.0, 7.0), (11.0, 12.0), (16.0, 17.0), (21.25, 23.0)]
WINDOW = (17.0, 21.25)
# Window, like the Doctor boxes: a 0.25 frame round 2 rows of 3 panes (0.75 wide, 1.75 tall),
# split by 0.25 glazing bars.
FRAME_W = 0.25
PANE_W, PANE_H = 0.75, 1.75
BAR = 0.25
COLS, ROWS = 3, 2


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
    i0, i1 = INNER
    for h0, h1 in RAILS:
        out.append(box(*X(i0, i1), h0, h1, *FRAME, "frame"))

    for i, (h0, h1) in enumerate(PANELS):
        top_panel = i == len(PANELS) - 1
        if top_panel and kind == "door_open" and era.badge:
            out.append(box(*X(i0, i1), h0, h1, *PANEL, "panel", north="panel_badge"))
        else:
            out.append(box(*X(i0, i1), h0, h1, *PANEL, "panel"))
        if top_panel and kind == "door_fixed":
            out.append(box(*X(i0 + 0.25, i1 - 0.25), h0 + 0.25, h1 - 0.25, *NOTICE, "notice_edge", north="notice"))

    out += window(X)

    if kind == "door_open":
        out.append(box(*X(0.25, 0.75), 10.75, 11.75, *PROUD, "handle"))
        out.append(box(*X(0.25, 0.75), 12.5, 13.0, *PROUD, "handle", north="lock"))
    if kind == "door_fixed":
        out.append(box(-0.25, 0.25, DOOR_BOTTOM, DOOR_TOP, *PROUD, "frame"))
    return out


def window(X) -> list[Box]:
    """Frame, glass and glazing bars for one leaf's window (2 rows of 3 panes)."""
    i0, i1 = INNER
    w0, w1 = WINDOW
    g0, g1 = i0 + FRAME_W, i1 - FRAME_W  # glass, inside the frame
    gh0, gh1 = w0 + FRAME_W, w1 - FRAME_W
    assert abs((g1 - g0) - (COLS * PANE_W + (COLS - 1) * BAR)) < 1e-6, "window panes do not fill the width"
    assert abs((gh1 - gh0) - (ROWS * PANE_H + (ROWS - 1) * BAR)) < 1e-6, "window panes do not fill the height"
    out = [
        box(*X(i0, g0), w0, w1, *WINDOW_FRAME, "bead"),
        box(*X(g1, i1), w0, w1, *WINDOW_FRAME, "bead"),
        box(*X(g0, g1), w0, gh0, *WINDOW_FRAME, "bead"),
        box(*X(g0, g1), gh1, w1, *WINDOW_FRAME, "bead"),
        box(*X(g0, g1), gh0, gh1, *GLASS, "bead", north="glass", south="glass"),
    ]
    cols = [g0 + c * (PANE_W + BAR) for c in range(COLS)]  # left edge of each pane column
    for c in range(1, COLS):  # upright bars, full height
        out.append(box(*X(cols[c] - BAR, cols[c]), gh0, gh1, *BARS, "bead"))
    for r in range(1, ROWS):  # cross bars, one segment per pane so no bars overlap
        h = gh0 + r * PANE_H + (r - 1) * BAR
        for x in cols:
            out.append(box(*X(x, x + PANE_W), h, h + BAR, *BARS, "bead"))
    return out


def surround(era: Era) -> list[Box]:
    """Fixed joinery around one doorway: jambs, lintel and the projecting sign box."""
    front = era.sign_front
    if front > -era.post_outer - 0.25 + 1e-6:
        raise ValueError(f"{era.year}: the sign box must stand at least 0.25 proud of the posts")
    return [
        box(-POST_INNER, -5.0, DOOR_BOTTOM, DOOR_TOP, *JAMB, "frame"),
        box(5.0, POST_INNER, DOOR_BOTTOM, DOOR_TOP, *JAMB, "frame"),
        box(-POST_INNER, POST_INNER, DOOR_TOP, LINTEL_H1, *JAMB, "frame"),
        box(-SIGN_HALF, SIGN_HALF, LINTEL_H1, SIGN_H1, front, -5.5, "frame", north="sign"),
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
    """Frieze band, roof base band and the stepped pitch. Returns (boxes, height of the top).

    Every tier steps in from the one below and stays inside the posts: there is no overhanging
    cornice.
    """
    out = [box(-SIGN_HALF, SIGN_HALF, SIGN_H1, FRIEZE_H1, -SIGN_HALF, SIGN_HALF, "frame")]
    h = FRIEZE_H1
    widest = SIGN_HALF
    for i, (half, thick) in enumerate(era.roof_tiers):
        if half >= widest:
            raise ValueError(f"{era.year}: roof tier {i} (±{half}) must step in from ±{widest}")
        out.append(box(-half, half, h, h + thick, -half, half, "frame" if i == 0 else "roof", down="roof"))
        h += thick
        widest = half
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
