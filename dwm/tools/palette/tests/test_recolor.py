"""Unit tests for palette profiles, products, and stone/ore recolour.

Run: (cd dwm/tools/palette && poetry run python -m unittest discover -s tests)
"""

from __future__ import annotations

import json
import unittest
from pathlib import Path

import numpy as np

from dwm_palette.oklab import oklab_chroma, rgb_to_oklab
from dwm_palette.palette import (
    is_minecraft_template,
    load_palette,
    load_products,
    load_template_rgb,
    minecraft_client_jar,
    palette_hexes,
    vanilla_stone_host_colours,
)
from dwm_palette.paths import find_dwm_root
from dwm_palette.profiles import expand_ramp
from dwm_palette.recolor import (
    apply_host_palette,
    apply_ore_palettes,
    build_rank_colour_map,
    host_colour_set,
    load_rgb_image,
    parse_hex,
    rgb_to_hex,
    split_ore_colours,
    unique_colours_by_luminance,
)

DWM = find_dwm_root()
PALETTES = DWM / "docs" / "palettes"
TEXTURES = DWM / "src" / "client" / "resources" / "assets" / "dwm" / "textures" / "block"
GALLIFREY_JSON = PALETTES / "gallifrey_stone.json"
ZEITON_JSON = PALETTES / "zeiton.json"
AZBANTIUM_JSON = PALETTES / "azbantium.json"
PRODUCTS_JSON = PALETTES / "products.json"
GALLIFREY_PNG = TEXTURES / "gallifrey_stone.png"
COAL_ORE_PNG = TEXTURES / "gallifrey_coal_ore.png"

GALLIFREY_HEXES = ["#632715", "#75331B", "#853D20", "#9E5129"]
COAL_VEIN_GREYS = ["#252525", "#2E2E2E", "#363636", "#393C36", "#494B3F"]
EMERALD_TEMPLATE = "minecraft:block/emerald_ore.png"
EMERALD_MINERAL_REF = ["#007B18", "#1C9829", "#17DD62", "#D9FFEB"]


def _role_hex(palette: dict, role: str) -> str:
    for entry in palette["roles"]:
        if entry["role"] == role:
            return entry["hex"]
    raise AssertionError(f"role {role!r} not in palette")


class ExpandRampTests(unittest.TestCase):
    def test_stone_reference_seed_is_identity(self) -> None:
        roles = expand_ramp("#853D20", "stone")
        self.assertEqual([r["role"] for r in roles], ["shadow", "dark", "mid", "hi"])
        self.assertEqual([r["hex"] for r in roles], GALLIFREY_HEXES)

    def test_stone_other_seed_keeps_luma_order_and_hue(self) -> None:
        roles = expand_ramp("#4A7C59", "stone")
        hexes = [r["hex"] for r in roles]
        self.assertEqual(len(hexes), 4)
        lums = [rgb_to_oklab(parse_hex(h))[0] for h in hexes]
        self.assertEqual(lums, sorted(lums))
        mid_lab = rgb_to_oklab(parse_hex("#4A7C59"))
        for h in hexes:
            lab = rgb_to_oklab(parse_hex(h))
            # Hue angle roughly preserved (chroma may be tiny but a/b sign matches).
            if oklab_chroma(lab) > 0.01 and oklab_chroma(mid_lab) > 0.01:
                # Dot product of (a,b) vectors positive ⇒ same half-plane / similar hue.
                self.assertGreater(lab[1] * mid_lab[1] + lab[2] * mid_lab[2], 0.0)

    def test_mineral_four_steps(self) -> None:
        roles = expand_ramp("#09AF71", "mineral")
        self.assertEqual([r["role"] for r in roles], ["shadow", "dark", "mid", "hi"])
        self.assertEqual(roles[2]["hex"], "#09AF71")
        lums = [rgb_to_oklab(parse_hex(r["hex"]))[0] for r in roles]
        self.assertEqual(lums, sorted(lums))

    def test_mineral_reference_seed_is_identity(self) -> None:
        roles = expand_ramp("#17DD62", "mineral")
        self.assertEqual([r["hex"] for r in roles], EMERALD_MINERAL_REF)


class PaletteSchemaTests(unittest.TestCase):
    def test_gallifrey_loads_as_stone_ramp(self) -> None:
        palette = load_palette(GALLIFREY_JSON)
        self.assertEqual(palette["seed"], "#853D20")
        self.assertEqual(palette["profile"], "stone")
        self.assertEqual(palette_hexes(palette), GALLIFREY_HEXES)

    def test_zeiton_has_no_host_colours(self) -> None:
        raw = json.loads(ZEITON_JSON.read_text(encoding="utf-8"))
        self.assertNotIn("roles", raw)
        self.assertEqual(raw["profile"], "mineral")
        self.assertEqual(raw["seed"].upper(), "#09AF71")
        palette = load_palette(ZEITON_JSON)
        self.assertEqual(len(palette["roles"]), 4)
        self.assertEqual(
            [r["role"] for r in palette["roles"]],
            ["shadow", "dark", "mid", "hi"],
        )
        self.assertEqual(_role_hex(palette, "mid"), "#09AF71")

    def test_azbantium_is_cyan_mineral(self) -> None:
        raw = json.loads(AZBANTIUM_JSON.read_text(encoding="utf-8"))
        self.assertEqual(raw["seed"].upper(), "#CDEAE5")
        self.assertEqual(raw["profile"], "mineral")
        palette = load_palette(AZBANTIUM_JSON)
        self.assertEqual(_role_hex(palette, "mid"), "#CDEAE5")
        self.assertEqual(len(palette["roles"]), 4)

    def test_products_share_gallifrey_host_and_emerald_template(self) -> None:
        products = load_products(PRODUCTS_JSON)
        by_id = {p["id"]: p for p in products}
        self.assertIn("zeiton_ore", by_id)
        self.assertIn("azbantium_ore", by_id)
        for pid in ("zeiton_ore", "azbantium_ore"):
            self.assertEqual(by_id[pid]["host"], "gallifrey_stone")
            self.assertEqual(by_id[pid]["template"], EMERALD_TEMPLATE)
            self.assertTrue(is_minecraft_template(by_id[pid]["template"]))
        self.assertEqual(by_id["zeiton_ore"]["mineral"], "zeiton")
        self.assertEqual(by_id["azbantium_ore"]["mineral"], "azbantium")


class ApplyHostPaletteTests(unittest.TestCase):
    def test_synthetic_greys_map_onto_four_host_hexes(self) -> None:
        template = np.array(
            [
                [[0x40, 0x40, 0x40], [0x70, 0x70, 0x70]],
                [[0x90, 0x90, 0x90], [0xB0, 0xB0, 0xB0]],
            ],
            dtype=np.uint8,
        )
        hosts = GALLIFREY_HEXES
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
        hosts = palette_hexes(palette)
        self.assertEqual(len(hosts), 4)
        src = load_rgb_image(GALLIFREY_PNG)
        out = apply_host_palette(src, hosts)
        src_hexes = {rgb_to_hex(c) for c in unique_colours_by_luminance(src)}
        out_hexes = {rgb_to_hex(c) for c in unique_colours_by_luminance(out)}
        self.assertEqual(src_hexes, set(hosts))
        self.assertEqual(out_hexes, set(hosts))
        np.testing.assert_array_equal(out, src)


class ApplyOrePalettesTests(unittest.TestCase):
    def test_split_coal_ore_vs_gallifrey_stone(self) -> None:
        """Legacy coal-on-Gallifrey layout: 5 crushed dark greys (not the ore product template)."""
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

    def test_synthetic_emerald_like_eight_to_four_rank_map(self) -> None:
        """4 host greys + 8 mineral greens → host + 4 vein steps (no jar)."""
        host_greys = [
            (0x68, 0x68, 0x68),
            (0x74, 0x74, 0x74),
            (0x7F, 0x7F, 0x7F),
            (0x8F, 0x8F, 0x8F),
        ]
        mineral_greens = [
            (0x37, 0x5B, 0x3E),
            (0x00, 0x7B, 0x18),
            (0x52, 0x6B, 0x50),
            (0x1C, 0x98, 0x29),
            (0x17, 0xC5, 0x44),
            (0x17, 0xDD, 0x62),
            (0x41, 0xF3, 0x84),
            (0xD9, 0xFF, 0xEB),
        ]
        # 4×4 tile: first row hosts, remaining rows minerals (pad with last mineral).
        pixels = host_greys + mineral_greens + [mineral_greens[-1]] * 4
        ore = np.array(pixels, dtype=np.uint8).reshape(4, 4, 3)
        host_colours = frozenset(host_greys)
        host_src, mineral_src = split_ore_colours(ore, host_colours)
        self.assertEqual(len(host_src), 4)
        self.assertEqual(len(mineral_src), 8)

        veins = EMERALD_MINERAL_REF
        out = apply_ore_palettes(ore, GALLIFREY_HEXES, veins, host_colours)
        out_hexes = {rgb_to_hex(c) for c in unique_colours_by_luminance(out)}
        self.assertTrue(out_hexes <= set(GALLIFREY_HEXES) | set(veins))
        self.assertTrue(out_hexes & set(veins))
        self.assertTrue(out_hexes & set(GALLIFREY_HEXES))
        # Rank map should use both ends of the 4-step vein ramp.
        colour_map = build_rank_colour_map(mineral_src, veins)
        mapped = {rgb_to_hex(colour_map[c]) for c in mineral_src}
        self.assertIn(veins[0], mapped)
        self.assertIn(veins[-1], mapped)

    def test_rank_map_five_to_four_uses_shadow_and_highlight(self) -> None:
        sources = [
            (0x25, 0x25, 0x25),
            (0x2E, 0x2E, 0x2E),
            (0x36, 0x36, 0x36),
            (0x39, 0x3C, 0x36),
            (0x49, 0x4B, 0x3F),
        ]
        targets = ["#007B18", "#1C9829", "#17DD62", "#D9FFEB"]
        colour_map = build_rank_colour_map(sources, targets)
        mapped = [rgb_to_hex(colour_map[c]) for c in sources]
        self.assertEqual(mapped[0], "#007B18")
        self.assertEqual(mapped[-1], "#D9FFEB")
        self.assertGreater(len(set(mapped)), 1)

    @unittest.skipUnless(
        (Path.home() / ".gradle/caches/fabric-loom").is_dir(),
        "Fabric Loom cache not present",
    )
    def test_emerald_ore_splits_against_vanilla_stone(self) -> None:
        try:
            jar = minecraft_client_jar(DWM)
        except FileNotFoundError:
            self.skipTest("minecraft-client.jar not in Loom cache")
        self.assertTrue(jar.is_file())
        ore = load_template_rgb(DWM, EMERALD_TEMPLATE)
        host_colours = vanilla_stone_host_colours(DWM)
        host, mineral = split_ore_colours(ore, host_colours)
        self.assertEqual(len(host), 4)
        self.assertEqual(len(mineral), 8)

    @unittest.skipUnless(
        (Path.home() / ".gradle/caches/fabric-loom").is_dir(),
        "Fabric Loom cache not present",
    )
    def test_gallifrey_azbantium_on_emerald_template(self) -> None:
        try:
            ore = load_template_rgb(DWM, EMERALD_TEMPLATE)
            host_colours = vanilla_stone_host_colours(DWM)
        except FileNotFoundError:
            self.skipTest("minecraft-client.jar not in Loom cache")

        host_palette = load_palette(GALLIFREY_JSON)
        mineral_palette = load_palette(AZBANTIUM_JSON)
        hosts = palette_hexes(host_palette)
        veins = palette_hexes(mineral_palette)
        self.assertEqual(len(veins), 4)
        self.assertEqual(hosts, GALLIFREY_HEXES)

        out = apply_ore_palettes(ore, hosts, veins, host_colours)

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
