"""Police-box geometry shared by every era.

Boxes are authored in "height space": x and z in model units, h = height above the ground
(model y = 24 - h). The front of the box faces -Z; doors fill the BOTI aperture
x -5..5, h 1..23 with their recess plane at z -6.0 (the hinge pivots sit at z -5.5).

Depth layers on a wall or door leaf (front = more negative z), kept >= 0.1 apart so no two
outward faces are coplanar:
    slab     -6.0 .. -5.25   recess ground (the leaf body)
    glass    -6.1 .. -6.0
    field    -6.2 .. -6.0    raised panels and window beading
    notice   -6.3 .. -6.0    telephone notice board
    frame    -6.4 .. -6.0    stiles and rails
    astragal -6.55 .. -6.4   centre strip on the fixed leaf
    handle   -6.65 .. -6.4
"""
from __future__ import annotations

from dataclasses import dataclass, field

from eras import Era

SLAB = (-6.0, -5.25)
GLASS = (-6.1, -6.0)
FIELD = (-6.2, -6.0)
NOTICE = (-6.3, -6.0)
FRAME = (-6.4, -6.0)

DOOR_TOP = 23.0
SIGN_H0, SIGN_H1 = 23.25, 26.0
HOOD_H1 = 26.375
POST_TOP = 27.0
CORNICE_H1 = 27.75

# Leaf layout, measured outward from the centre line (0) to the hinge/post side (5).
STILE_IN = (0.0, 0.75)
STILE_OUT = (4.25, 5.0)
PANELS = [(2.0, 6.5), (7.0, 11.5), (12.0, 16.5)]
RAILS = [(1.0, 2.0), (6.5, 7.0), (11.5, 12.0), (16.5, 17.0), (21.5, 23.0)]
WINDOW = (17.0, 21.5)


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
    return Box(xa, xb, h0, h1, za, zb, role, faces)


def leaf(era: Era, side: int, kind: str) -> list[Box]:
    """One door leaf / wall half. side +1 spans x 0..5, -1 spans x -5..0.

    kind: "wall", "door_open" (handle, lock, optional badge) or "door_fixed" (notice, astragal).
    """

    def X(a, b):
        return sorted((side * a, side * b))

    out = [box(*X(0, 5), 1.0, DOOR_TOP, *SLAB, "ground", south="leaf_back")]
    for a, b in (STILE_IN, STILE_OUT):
        out.append(box(*X(a, b), 1.0, DOOR_TOP, *FRAME, "frame"))
    for h0, h1 in RAILS:
        out.append(box(*X(0.75, 4.25), h0, h1, *FRAME, "frame"))

    for i, (h0, h1) in enumerate(PANELS):
        top_panel = i == len(PANELS) - 1
        if top_panel and kind == "door_fixed":
            out.append(box(*X(1.0, 4.0), h0 + 0.25, h1 - 0.25, *NOTICE, "frame", north="notice"))
        elif top_panel and kind == "door_open" and era.badge:
            out.append(box(*X(1.0, 4.0), h0 + 0.25, h1 - 0.25, *FIELD, "field", north="field_badge"))
        else:
            out.append(box(*X(1.0, 4.0), h0 + 0.25, h1 - 0.25, *FIELD, "field"))

    w0, w1 = WINDOW
    out += [
        box(*X(0.75, 1.0), w0, w1, *FIELD, "bead"),
        box(*X(4.0, 4.25), w0, w1, *FIELD, "bead"),
        box(*X(1.0, 4.0), w0, w0 + 0.25, *FIELD, "bead"),
        box(*X(1.0, 4.0), w1 - 0.25, w1, *FIELD, "bead"),
        box(*X(1.0, 4.0), w0 + 0.25, w1 - 0.25, *GLASS, "bead", north="glass"),
    ]

    if kind == "door_open":
        out.append(box(*X(0.2, 0.55), 11.25, 12.0, -6.65, -6.4, "handle"))
        out.append(box(*X(0.25, 0.5), 12.75, 13.25, -6.5, -6.4, "handle", north="lock"))
    if kind == "door_fixed":
        out.append(box(-0.25, 0.25, 1.0, DOOR_TOP, -6.55, -6.4, "frame"))
    return out


def sign_assembly(era: Era) -> list[Box]:
    """Lower moulding, the illuminated sign box and its hood, for one face of the box."""
    front = era.sign_front
    return [
        box(-5.0, 5.0, DOOR_TOP, SIGN_H0, -6.6, -6.0, "frame"),
        box(-5.5, 5.5, SIGN_H0, SIGN_H1, front, -6.0, "frame", north="sign"),
        box(-5.75, 5.75, SIGN_H1, HOOD_H1, front - 0.15, -6.0, "frame"),
    ]


def wall(era: Era) -> list[Box]:
    return leaf(era, +1, "wall") + leaf(era, -1, "wall") + sign_assembly(era)


def posts(era: Era) -> list[Box]:
    p = era.post_outer
    s0, s1 = 5.5, p - 0.5
    out = []
    for sx in (-1, 1):
        for sz in (-1, 1):
            xs = sorted((sx * 5.0, sx * p))
            zs = sorted((sz * 5.0, sz * p))
            out.append(box(*xs, 1.0, POST_TOP, *zs, "post"))
            # flush base block, a hair proud of the post on the two outer faces
            out.append(box(*sorted((sx * 5.0, sx * (p + 0.1))), 1.0, 2.0,
                           *sorted((sz * 5.0, sz * (p + 0.1))), "frame"))
            # raised centre strips on the two outward faces
            out.append(box(*sorted((sx * p, sx * (p + 0.1))), 2.0, 26.5,
                           *sorted((sz * s0, sz * s1)), "frame"))
            out.append(box(*sorted((sx * s0, sx * s1)), 2.0, 26.5,
                           *sorted((sz * p, sz * (p + 0.1))), "frame"))
    return out


def roof(era: Era) -> tuple[list[Box], float]:
    """Frieze, cornice and the stepped roof stack; returns (boxes, height of the top)."""
    c = era.cornice_half
    out = [
        box(-6.25, 6.25, HOOD_H1, POST_TOP, -6.25, 6.25, "frame"),
        box(-c, c, POST_TOP, CORNICE_H1, -c, c, "frame", down="roof"),
    ]
    h = CORNICE_H1
    for half, thick in era.roof_tiers:
        out.append(box(-half, half, h, h + thick, -half, half, "roof"))
        h += thick
    return out, h


def lamp(era: Era, base_h: float) -> list[Box]:
    L = era.lamp
    out = [box(-1.0, 1.0, base_h, base_h + 0.25, -1.0, 1.0, "frame")]
    h0 = base_h + 0.25
    h1 = h0 + L.lens_h
    r = L.lens_half
    out.append(box(-r, r, h0, h1, -r, r, "lens"))
    if L.cage:
        c0, c1 = r, r + 0.25
        for sx in (-1, 1):
            for sz in (-1, 1):
                out.append(box(*sorted((sx * c0, sx * c1)), h0, h1, *sorted((sz * c0, sz * c1)), "frame"))
        cap = r + 0.25
    else:
        cap = r + 0.125
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
    ph = era.plinth_half
    return [
        dict(name="base", boxes=[box(-ph, ph, 0.0, 1.0, -ph, ph, "plinth")]),
        dict(name="posts", boxes=posts(era)),
        dict(name="roof", boxes=roof_boxes),
        dict(name="lamp", boxes=lamp(era, top)),
        dict(name="front", boxes=sign_assembly(era)),
        dict(name="wall_left", boxes=wall(era), rotation=(0.0, -90.0, 0.0)),
        dict(name="wall_back", boxes=wall(era), rotation=(0.0, 180.0, 0.0)),
        dict(name="wall_right", boxes=wall(era), rotation=(0.0, 90.0, 0.0)),
        # LeftDoor hinges at x=+5 (viewer's right) and is the leaf that swings open.
        dict(name="LeftDoor", boxes=leaf(era, +1, "door_open"), pivot=(5.0, 24.0, -5.5)),
        dict(name="rightDoor", boxes=leaf(era, -1, "door_fixed"), pivot=(-5.0, 24.0, -5.5)),
    ]
