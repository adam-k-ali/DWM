"""Per-era design specs for the 2005 / 2010 / 2014 / 2018 police-box exteriors.

Everything that differs between eras lives here: paint colours, wear, proportions, roof stack,
lamp, window glazing, sign depth, the St John Ambulance badge. Geometry and painting read these
fields and never branch on the year itself.
"""
from __future__ import annotations

from dataclasses import dataclass, field

RGB = tuple[int, int, int]


@dataclass(frozen=True)
class Lamp:
    lens_half: float  # half-width of the lens block
    lens_h: float  # lens height
    cage: bool  # four corner posts around the lens
    dome: bool  # domed cap (else a flat cap)
    fresnel: bool  # ribbed lens (else plain frosted)


@dataclass(frozen=True)
class Era:
    year: int
    body: RGB  # main paint
    glass: RGB  # lit window glass
    mullion: RGB  # window glazing bars and beading
    sign_text: RGB
    notice: RGB  # telephone notice board
    notice_text: RGB
    lens: RGB
    handle: RGB
    wear: float  # 0..1 faded/patchy paint
    plinth_half: float
    post_outer: float  # outer face of the corner posts (|x|, |z|)
    sign_front: float  # z of the sign face (more negative = deeper box)
    cornice_half: float
    roof_tiers: list[tuple[float, float]]  # (half width, thickness), stacked from the cornice up
    lamp: Lamp
    hammered: bool  # bottom-corner window panes in hammered glass
    badge: bool  # St John Ambulance badge on the opening door
    shade: float = 1.0  # roof and plinth darkening
    extra: dict = field(default_factory=dict)

    @property
    def id(self) -> str:
        return f"tardis_{self.year}"


WHITE_BEAD: RGB = (238, 240, 240)

ERAS: list[Era] = [
    # Weathered, slightly teal mid blue; blue glazing bars; deep sign boxes; tall two-step roof stack
    # and a plain frosted lantern with a flat cap.
    Era(2005,
        body=(36, 74, 110), glass=(244, 238, 214), mullion=(36, 74, 110), sign_text=(232, 232, 226),
        notice=(228, 226, 214), notice_text=(26, 30, 46), lens=(250, 240, 204), handle=(176, 158, 104),
        wear=0.4, plinth_half=7.5, post_outer=6.75, sign_front=-7.25, cornice_half=7.0,
        roof_tiers=[(6.25, 0.75), (5.5, 0.5), (4.5, 0.5)],
        lamp=Lamp(lens_half=0.625, lens_h=1.75, cage=False, dome=False, fresnel=False),
        hammered=False, badge=False, shade=0.92),
    # Cleaner, brighter blue; white windows with hammered bottom-corner panes; shallower roof; caged
    # Fresnel lantern with a domed cap; St John Ambulance badge.
    Era(2010,
        body=(34, 92, 166), glass=(208, 212, 202), mullion=WHITE_BEAD, sign_text=(240, 240, 238),
        notice=(240, 240, 236), notice_text=(18, 20, 28), lens=(255, 248, 222), handle=(196, 198, 202),
        wear=0.15, plinth_half=7.5, post_outer=6.75, sign_front=-7.1, cornice_half=7.0,
        roof_tiers=[(6.25, 0.5), (5.0, 0.5)],
        lamp=Lamp(lens_half=0.625, lens_h=1.5, cage=True, dome=True, fresnel=True),
        hammered=True, badge=True),
    # The same box repainted a darker navy.
    Era(2014,
        body=(20, 42, 92), glass=(194, 204, 216), mullion=WHITE_BEAD, sign_text=(240, 240, 238),
        notice=(238, 238, 234), notice_text=(16, 18, 26), lens=(250, 248, 236), handle=(184, 188, 196),
        wear=0.2, plinth_half=7.5, post_outer=6.75, sign_front=-7.1, cornice_half=7.0,
        roof_tiers=[(6.25, 0.5), (5.0, 0.5)],
        lamp=Lamp(lens_half=0.625, lens_h=1.5, cage=True, dome=True, fresnel=True),
        hammered=True, badge=True, shade=0.95),
    # Vivid, saturated blue; slightly narrower box with slimmer posts and signs; no badge.
    Era(2018,
        body=(18, 58, 150), glass=(192, 210, 228), mullion=WHITE_BEAD, sign_text=(246, 246, 246),
        notice=(242, 242, 240), notice_text=(14, 16, 24), lens=(240, 248, 255), handle=(206, 210, 216),
        wear=0.06, plinth_half=7.25, post_outer=6.5, sign_front=-6.95, cornice_half=6.75,
        roof_tiers=[(6.0, 0.5), (5.25, 0.5), (4.25, 0.25)],
        lamp=Lamp(lens_half=0.75, lens_h=1.5, cage=True, dome=True, fresnel=True),
        hammered=False, badge=False),
]
