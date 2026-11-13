"""Box-UV net allocation in vanilla `ModelPart.Cube` layout.

A cube of size (w, h, d) at texOffs (u, v) unwraps to a net 2(w+d) wide and d+h tall:

    top row     [ d gap ][ DOWN w ][ UP w ]          (DOWN = min Y = the visual top face)
    middle row  [ WEST d ][ NORTH w ][ EAST d ][ SOUTH w ]

NORTH is min Z, the outward face of every front-built wall. Inside each face the left column is
min X (NORTH) and the top row is min Y, so painted lettering reads correctly from outside.
"""
from __future__ import annotations

import math

FACES = ("down", "up", "west", "north", "east", "south")


def net_size(w: float, h: float, d: float) -> tuple[int, int]:
    return math.ceil(2 * (w + d) - 1e-9), math.ceil(d + h - 1e-9)


def face_rects(u: int, v: int, w: float, h: float, d: float) -> dict[str, tuple[float, float, float, float]]:
    """Face -> (u0, v0, width, height) in UV units for a net at (u, v)."""
    return {
        "down": (u + d, v, w, d),
        "up": (u + d + w, v, w, d),
        "west": (u, v + d, d, h),
        "north": (u + d, v + d, w, h),
        "east": (u + d + w, v + d, d, h),
        "south": (u + 2 * d + w, v + d, w, h),
    }


class Packer:
    """Shelf packer over a square atlas; identical keys share one net. Fails hard when full."""

    def __init__(self, size: int):
        self.size = size
        self.slots: dict[object, tuple[int, int]] = {}
        self.requests: dict[object, tuple[int, int]] = {}

    def request(self, key, w: float, h: float, d: float) -> None:
        if key not in self.requests:
            self.requests[key] = net_size(w, h, d)

    def pack(self) -> None:
        order = sorted(self.requests.items(), key=lambda kv: (-kv[1][1], -kv[1][0], repr(kv[0])))
        x = y = shelf_h = 0
        for key, (nw, nh) in order:
            if nw > self.size:
                raise ValueError(f"net {key} is {nw} wide, atlas is {self.size}")
            if x + nw > self.size:
                x, y, shelf_h = 0, y + shelf_h, 0
            if y + nh > self.size:
                raise ValueError(f"atlas {self.size} is full placing {key} ({nw}x{nh})")
            self.slots[key] = (x, y)
            x += nw
            shelf_h = max(shelf_h, nh)
        self._check_overlap()

    def _check_overlap(self) -> None:
        rects = [(k, x, y, *self.requests[k]) for k, (x, y) in self.slots.items()]
        for i, (ka, ax, ay, aw, ah) in enumerate(rects):
            for kb, bx, by, bw, bh in rects[i + 1:]:
                if ax < bx + bw and bx < ax + aw and ay < by + bh and by < ay + ah:
                    raise AssertionError(f"UV nets overlap: {ka} and {kb}")

    def slot(self, key) -> tuple[int, int]:
        return self.slots[key]
