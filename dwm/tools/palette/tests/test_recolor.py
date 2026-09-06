"""Unit tests for family palette host-stone and ore recolour.

Run: (cd dwm/tools/palette && poetry run python -m unittest discover -s tests)
"""

from __future__ import annotations

import unittest
from pathlib import Path

import numpy as np

from dwm_palette.paths import find_dwm_root
from dwm_palette.recolor import (
    apply_host_palette,
    apply_ore_palettes,
    build_rank_colour_map,
    host_colour_set,
    host_hexes,
    load_palette,
    load_rgb_image,
    rgb_to_hex,
    split_ore_colours,
    unique_colours_by_luminance,
    vein_hexes,
)

DWM = find_dwm_root()
PALETTES = DWM / "docs" / "palettes"
TEXTURES = DWM / "src" / "client" / "resources" / "assets" / "dwm" / "textures" / "block"
GALLIFREY_JSON = PALETTES / "gallifrey_stone.json"
AZBANTIUM_JSON = PALETTES / "azbantium.json"
GALLIFREY_PNG = TEXTURES / "gallifrey_stone.png"
COAL_ORE_PNG = TEXTURES / "gallifrey_coal_ore.png"

# Coal ore mineral greys (dark→light), from gallifrey_coal_ore.png analysis.
COAL_VEIN_GREYS = ["#252525", "#2E2E2E", "#363636", "#393C36", "#494B3F"]


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


class VeinHexesTests(unittest.TestCase):
    def test_vein_hexes_ignores_host_and_gem(self) -> None:
        palette = {
            "roles": [
                {"role": "host_mid", "hex": "#843D1D", "notes": ""},
                {"role": "vein_shadow", "hex": "#BBBBBB", "notes": ""},
                {"role": "vein_mid", "hex": "#E8E8E8", "notes": ""},
                {"role": "vein_hi", "hex": "#FFFFFF", "notes": ""},
                {"role": "gem_mid", "hex": "#CDEAE5", "notes": ""},
            ]
        }
        self.assertEqual(vein_hexes(palette), ["#BBBBBB", "#E8E8E8", "#FFFFFF"])

    def test_vein_hexes_errors_when_missing(self) -> None:
        palette = {
            "roles": [
                {"role": "host_mid", "hex": "#843D1D", "notes": ""},
                {"role": "gem_mid", "hex": "#CDEAE5", "notes": ""},
            ]
        }
        with self.assertRaises(ValueError) as ctx:
            vein_hexes(palette)
        self.assertIn("vein_*", str(ctx.exception))


class ApplyOrePalettesTests(unittest.TestCase):
    def test_split_coal_ore_vs_stone(self) -> None:
        self.assertTrue(COAL_ORE_PNG.is_file(), COAL_ORE_PNG)
        self.assertTrue(GALLIFREY_PNG.is_file(), GALLIFREY_PNG)
        ore = load_rgb_image(COAL_ORE_PNG)
        stone = load_rgb_image(GALLIFREY_PNG)
        host_colours = host_colour_set(stone)
        host, mineral = split_ore_colours(ore, host_colours)
        self.assertEqual(len(host), 4)
        self.assertEqual(len(mineral), 5)
        self.assertEqual(
            {rgb_to_hex(c) for c in mineral},
            set(COAL_VEIN_GREYS),
        )

    def test_coal_identity_with_coal_greys_as_veins(self) -> None:
        self.assertTrue(GALLIFREY_JSON.is_file(), GALLIFREY_JSON)
        self.assertTrue(COAL_ORE_PNG.is_file(), COAL_ORE_PNG)
        self.assertTrue(GALLIFREY_PNG.is_file(), GALLIFREY_PNG)
        host_palette = load_palette(GALLIFREY_JSON)
        ore = load_rgb_image(COAL_ORE_PNG)
        host_colours = host_colour_set(load_rgb_image(GALLIFREY_PNG))
        out = apply_ore_palettes(
            ore,
            host_hexes(host_palette),
            COAL_VEIN_GREYS,
            host_colours,
        )
        np.testing.assert_array_equal(out, ore)

    def test_rank_map_five_to_three_uses_shadow_and_highlight(self) -> None:
        sources = [
            (0x25, 0x25, 0x25),
            (0x2E, 0x2E, 0x2E),
            (0x36, 0x36, 0x36),
            (0x39, 0x3C, 0x36),
            (0x49, 0x4B, 0x3F),
        ]
        targets = ["#BBBBBB", "#E8E8E8", "#FFFFFF"]
        colour_map = build_rank_colour_map(sources, targets)
        mapped = [rgb_to_hex(colour_map[c]) for c in sources]
        self.assertEqual(mapped[0], "#BBBBBB")
        self.assertEqual(mapped[-1], "#FFFFFF")
        self.assertIn("#E8E8E8", mapped)
        # Not all vein_shadow
        self.assertGreater(len(set(mapped)), 1)

    def test_gallifrey_azbantium_mask_and_colours(self) -> None:
        self.assertTrue(GALLIFREY_JSON.is_file(), GALLIFREY_JSON)
        self.assertTrue(AZBANTIUM_JSON.is_file(), AZBANTIUM_JSON)
        self.assertTrue(COAL_ORE_PNG.is_file(), COAL_ORE_PNG)
        self.assertTrue(GALLIFREY_PNG.is_file(), GALLIFREY_PNG)

        host_palette = load_palette(GALLIFREY_JSON)
        mineral_palette = load_palette(AZBANTIUM_JSON)
        ore = load_rgb_image(COAL_ORE_PNG)
        host_colours = host_colour_set(load_rgb_image(GALLIFREY_PNG))
        hosts = host_hexes(host_palette)
        veins = vein_hexes(mineral_palette)
        self.assertEqual(veins, ["#BBBBBB", "#E8E8E8", "#FFFFFF"])

        out = apply_ore_palettes(ore, hosts, veins, host_colours)

        # Mineral mask positions preserved from the coal template.
        _, mineral_src = split_ore_colours(ore, host_colours)
        mineral_set = set(mineral_src)
        h, w, _ = ore.shape
        for y in range(h):
            for x in range(w):
                src_key = (int(ore[y, x, 0]), int(ore[y, x, 1]), int(ore[y, x, 2]))
                out_hex = rgb_to_hex(tuple(out[y, x]))
                if src_key in mineral_set:
                    self.assertIn(out_hex, set(veins))
                else:
                    self.assertIn(out_hex, set(hosts))

        out_hexes = {rgb_to_hex(c) for c in unique_colours_by_luminance(out)}
        self.assertTrue(out_hexes <= set(hosts) | set(veins))
        self.assertTrue(out_hexes & set(veins))
        self.assertTrue(out_hexes & set(hosts))


if __name__ == "__main__":
    raise SystemExit(unittest.main())
