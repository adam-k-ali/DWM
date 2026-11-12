"""Per-era design specs for the 2005 / 2010 / 2014 / 2018 police-box exteriors.

Everything that differs between eras lives here: paint colours, weathering, post and sign depth,
roof stack, lamp, window glazing, the St John Ambulance badge. Geometry and painting read these
fields and never branch on the year itself.

Colours sit in the same family as the 1st-7th Doctor boxes (dark, desaturated navy): each era keeps
its own hue but stays well below the brightness of the real props.
"""
from __future__ import annotations

from dataclasses import dataclass

RGB = tuple[int, int, int]


@dataclass(frozen=True)
class Lamp:
    lens_half: float  # half-width of the lens block
    lens_h: float  # lens height
    cage: bool  # four corner posts around the lens
    dome: bool  # stepped domed cap (else a flat cap with a knob)
    fresnel: bool  # ribbed lens (else plain frosted)


@dataclass(frozen=True)
class Era:
    year: int
    body: RGB  # main paint
    glass: RGB  # window glass
    mullion: RGB  # window frame and glazing bars
    sign_text: RGB  # white of the St John Ambulance badge
    notice: RGB  # telephone notice board
    notice_text: RGB
    lens: RGB
    handle: RGB
    mottle: float  # 0..1 strength of the soft tonal clouds (weathering)
    post_outer: float  # outer face of the corner posts (|x|, |z|)
    sign_front: float  # z of the sign face (more negative = deeper sign box)
    roof_tiers: tuple[tuple[float, float], ...]  # (half width, thickness): base band, then the pitch steps
    lamp: Lamp
    hammered: bool  # bottom row of window panes in hammered glass
    badge: bool  # St John Ambulance badge on the opening door
    shade: float = 1.0  # extra roof and plinth darkening

    @property
    def id(self) -> str:
        return f"tardis_{self.year}"


WHITE_FRAME: RGB = (196, 200, 198)

ERAS: list[Era] = [
    # Weathered teal-navy; blue window bars and warm glass; deep sign boxes; roof per the Series 1-4
    # blueprint (frieze, base band, four-step pitch, no overhang) and a
    # plain frosted lantern with a flat cap.
    Era(2005,
        body=(40, 66, 92), glass=(206, 196, 160), mullion=(40, 66, 92), sign_text=(214, 214, 204),
        notice=(202, 198, 180), notice_text=(34, 38, 48), lens=(226, 214, 176), handle=(150, 136, 96),
        mottle=0.10, post_outer=7.0, sign_front=-7.25,
        roof_tiers=((6.0, 1.0), (5.5, 0.25), (4.75, 0.25), (3.75, 0.25), (2.75, 0.25)),
        lamp=Lamp(lens_half=0.75, lens_h=1.5, cage=False, dome=False, fresnel=False),
        hammered=False, badge=False, shade=0.94),
    # Cleaner, brighter blue; white window frames with hammered bottom panes; shallower three-step pitch; caged
    # Fresnel lantern with a domed cap; St John Ambulance badge.
    Era(2010,
        body=(36, 74, 128), glass=(150, 164, 170), mullion=WHITE_FRAME, sign_text=(222, 222, 218),
        notice=(208, 208, 202), notice_text=(26, 28, 36), lens=(226, 222, 200), handle=(160, 164, 170),
        mottle=0.05, post_outer=7.0, sign_front=-7.25,
        roof_tiers=((6.0, 1.0), (5.25, 0.25), (4.25, 0.25), (3.0, 0.25)),
        lamp=Lamp(lens_half=0.75, lens_h=1.5, cage=True, dome=True, fresnel=True),
        hammered=True, badge=True),
    # The same box repainted a darker navy.
    Era(2014,
        body=(28, 48, 88), glass=(142, 156, 170), mullion=WHITE_FRAME, sign_text=(222, 222, 218),
        notice=(204, 204, 200), notice_text=(24, 26, 34), lens=(222, 222, 210), handle=(154, 158, 166),
        mottle=0.06, post_outer=7.0, sign_front=-7.25,
        roof_tiers=((6.0, 1.0), (5.25, 0.25), (4.25, 0.25), (3.0, 0.25)),
        lamp=Lamp(lens_half=0.75, lens_h=1.5, cage=True, dome=True, fresnel=True),
        hammered=True, badge=True, shade=0.96),
    # Deep saturated blue; slimmer posts and roof; no badge.
    Era(2018,
        body=(26, 56, 116), glass=(146, 164, 182), mullion=WHITE_FRAME, sign_text=(226, 226, 226),
        notice=(210, 210, 206), notice_text=(22, 24, 32), lens=(214, 222, 228), handle=(168, 172, 178),
        mottle=0.04, post_outer=6.75, sign_front=-7.0,
        roof_tiers=((5.75, 1.0), (5.25, 0.25), (4.5, 0.25), (3.5, 0.25), (2.75, 0.25)),
        lamp=Lamp(lens_half=0.75, lens_h=1.5, cage=True, dome=True, fresnel=True),
        hammered=False, badge=False),
]
