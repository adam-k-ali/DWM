"""Unit tests for family palette host-stone recolour.

Run: (cd dwm/tools/palette && poetry run python -m unittest discover -s tests)
"""

from __future__ import annotations

import unittest
from pathlib import Path

import numpy as np

from dwm_palette.paths import find_dwm_root
from dwm_palette.recolor import (
    apply_host_palette,
    host_hexes,
    load_palette,
    load_rgb_image,
    rgb_to_hex,
    unique_colours_by_luminance,
)

DWM = find_dwm_root()
GALLIFREY_JSON = DWM / "docs" / "palettes" / "gallifrey_stone.json"
GALLIFREY_PNG = (
    DWM
    / "src"
    / "client"
    / "resources"
    / "assets"
    / "dwm"
    / "textures"
    / "block"
    / "gallifrey_stone.png"
)


class ApplyHostPaletteTests(unittest.TestCase):
    def test_synthetic_greys_map_onto_four_host_hexes(self) -> None:
        # Four greys, dark→light, on a 2×2 tile.
        template = np.array(
            [
                [[0x40, 0x40, 0x40], [0x70, 0x70, 0x70]],
                [[0x90, 0x90, 0x90], [0xB0, 0xB0, 0xB0]],
            ],
            dtype=np.uint8,
        )
        hosts = ["#632715", "#75331B", "#853D20", "#9E5129"]
        out = apply_host_palette(template, hosts)
        self.assertEqual(out.shape, (2, 2, 3))
        expected = [
            [hosts[0], hosts[1]],
            [hosts[2], hosts[3]],
        ]
        for y in range(2):
            for x in range(2):
                self.assertEqual(rgb_to_hex(tuple(out[y, x])), expected[y][x])

    def test_gallifrey_identity_preserves_palette_hexes(self) -> None:
        self.assertTrue(GALLIFREY_JSON.is_file(), GALLIFREY_JSON)
        self.assertTrue(GALLIFREY_PNG.is_file(), GALLIFREY_PNG)
        palette = load_palette(GALLIFREY_JSON)
        hosts = host_hexes(palette)
        self.assertEqual(len(hosts), 4)
        src = load_rgb_image(GALLIFREY_PNG)
        out = apply_host_palette(src, hosts)
        src_hexes = {rgb_to_hex(c) for c in unique_colours_by_luminance(src)}
        out_hexes = {rgb_to_hex(c) for c in unique_colours_by_luminance(out)}
        self.assertEqual(src_hexes, set(hosts))
        self.assertEqual(out_hexes, set(hosts))
        np.testing.assert_array_equal(out, src)

    def test_host_hexes_ignores_non_host_roles(self) -> None:
        palette = {
            "roles": [
                {"role": "host_shadow", "hex": "#111111", "notes": ""},
                {"role": "host_hi", "hex": "#EEEEEE", "notes": ""},
                {"role": "vein_mid", "hex": "#00FF00", "notes": ""},
            ]
        }
        self.assertEqual(host_hexes(palette), ["#111111", "#EEEEEE"])


if __name__ == "__main__":
    raise SystemExit(unittest.main())
