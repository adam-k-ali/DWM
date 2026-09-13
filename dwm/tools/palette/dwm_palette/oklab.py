"""OKLab / OKLCh colour space helpers for palette ramp expansion.

Offline tooling only — not invoked by Gradle or CI.
"""

from __future__ import annotations

import math

RgbFloat = tuple[float, float, float]
Oklab = tuple[float, float, float]  # L, a, b


def srgb_to_linear(c: float) -> float:
    if c <= 0.04045:
        return c / 12.92
    return ((c + 0.055) / 1.055) ** 2.4


def linear_to_srgb(c: float) -> float:
    if c <= 0.0031308:
        return 12.92 * c
    return 1.055 * (c ** (1.0 / 2.4)) - 0.055


def rgb_to_oklab(rgb: RgbFloat) -> Oklab:
    r, g, b = (srgb_to_linear(c) for c in rgb)
    l = 0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b
    m = 0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b
    s = 0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b
    l_ = l ** (1.0 / 3.0)
    m_ = m ** (1.0 / 3.0)
    s_ = s ** (1.0 / 3.0)
    L = 0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_
    a = 1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_
    b_ = 0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_
    return (L, a, b_)


def oklab_to_rgb(lab: Oklab) -> RgbFloat:
    L, a, b = lab
    l_ = L + 0.3963377774 * a + 0.2158037573 * b
    m_ = L - 0.1055613458 * a - 0.0638541728 * b
    s_ = L - 0.0894841775 * a - 1.2914855480 * b
    l = l_ * l_ * l_
    m = m_ * m_ * m_
    s = s_ * s_ * s_
    r = +4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s
    g = -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s
    b_ = -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s
    return (
        max(0.0, min(1.0, linear_to_srgb(r))),
        max(0.0, min(1.0, linear_to_srgb(g))),
        max(0.0, min(1.0, linear_to_srgb(b_))),
    )


def oklab_chroma(lab: Oklab) -> float:
    return math.hypot(lab[1], lab[2])


def oklab_with_lightness_chroma(lab: Oklab, L: float, chroma: float) -> Oklab:
    """Keep hue of *lab*, set lightness and chroma."""
    _, a, b = lab
    current = math.hypot(a, b)
    if current < 1e-12:
        return (max(0.0, min(1.0, L)), 0.0, 0.0)
    scale = chroma / current
    return (max(0.0, min(1.0, L)), a * scale, b * scale)
