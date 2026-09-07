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
    load_template_rgba,
    minecraft_client_jar,
    palette_hexes,
    vanilla_stone_host_colours,
    vanilla_tool_handle_colours,
)
from dwm_palette.paths import find_dwm_root
from dwm_palette.profiles import expand_ramp
from dwm_palette.recolor import (
    apply_mineral_item_palette,
    apply_host_palette,
    apply_ore_palettes,
    apply_tool_item_palette,
    build_rank_colour_map,
    host_colour_set,
    load_rgb_image,
    parse_hex,
    rgb_to_hex,
    split_ore_colours,
    split_tool_colours,
    unique_colours_by_luminance,
)

DWM = find_dwm_root()
PALETTES = DWM / "docs" / "palettes"
TEXTURES = DWM / "src" / "client" / "resources" / "assets" / "dwm" / "textures" / "block"
GALLIFREY_JSON = PALETTES / "gallifrey_stone.json"
ZEITON_JSON = PALETTES / "zeiton.json"
AZBANTIUM_JSON = PALETTES / "azbantium.json"
STEEL_JSON = PALETTES / "steel.json"
TOOL_HANDLE_JSON = PALETTES / "tool_handle.json"
PRODUCTS_JSON = PALETTES / "products.json"
GALLIFREY_PNG = TEXTURES / "gallifrey_stone.png"
COAL_ORE_PNG = TEXTURES / "gallifrey_coal_ore.png"

GALLIFREY_HEXES = ["#632715", "#75331B", "#853D20", "#9E5129"]
COAL_VEIN_GREYS = ["#252525", "#2E2E2E", "#363636", "#393C36", "#494B3F"]
EMERALD_TEMPLATE = "minecraft:block/emerald_ore.png"
EMERALD_MINERAL_REF = ["#007B18", "#1C9829", "#17DD62", "#D9FFEB"]
STICK_HANDLE_HEXES = ["#281E0B", "#493615", "#684E1E", "#896727"]
STEEL_SEED = "#58616A"


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

    def test_steel_is_dark_silvery_mineral(self) -> None:
        raw = json.loads(STEEL_JSON.read_text(encoding="utf-8"))
        self.assertEqual(raw["seed"].upper(), STEEL_SEED)
        self.assertEqual(raw["profile"], "mineral")
        self.assertEqual(raw["map_color"], "COLOR_GRAY")
        palette = load_palette(STEEL_JSON)
        self.assertEqual(_role_hex(palette, "mid"), STEEL_SEED)
        self.assertEqual(len(palette["roles"]), 4)
        hexes = palette_hexes(palette)
        lums = [rgb_to_oklab(parse_hex(h))[0] for h in hexes]
        self.assertEqual(lums, sorted(lums))

    def test_tool_handle_is_stone_wood(self) -> None:
        raw = json.loads(TOOL_HANDLE_JSON.read_text(encoding="utf-8"))
        self.assertEqual(raw["seed"].upper(), "#684E1E")
        self.assertEqual(raw["profile"], "stone")
        palette = load_palette(TOOL_HANDLE_JSON)
        self.assertEqual(_role_hex(palette, "mid"), "#684E1E")
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

        self.assertIn("azbantium", by_id)
        self.assertIn("zeiton_crystals", by_id)
        self.assertEqual(by_id["azbantium"]["archetype"], "gem")
        self.assertEqual(by_id["azbantium"]["template"], "minecraft:item/diamond.png")
        self.assertEqual(by_id["azbantium"]["mineral"], "azbantium")
        self.assertNotIn("host", by_id["azbantium"])
        self.assertEqual(by_id["zeiton_crystals"]["archetype"], "crystal")
        self.assertEqual(by_id["zeiton_crystals"]["template"], "minecraft:item/quartz.png")
        self.assertEqual(by_id["zeiton_crystals"]["mineral"], "zeiton")
        self.assertNotIn("host", by_id["zeiton_crystals"])

        self.assertIn("steel_ingot", by_id)
        self.assertEqual(by_id["steel_ingot"]["archetype"], "ingot")
        self.assertEqual(by_id["steel_ingot"]["template"], "minecraft:item/iron_ingot.png")
        self.assertEqual(by_id["steel_ingot"]["mineral"], "steel")
        self.assertNotIn("host", by_id["steel_ingot"])
        self.assertNotIn("handle", by_id["steel_ingot"])

        self.assertIn("steel_pickaxe", by_id)
        self.assertEqual(by_id["steel_pickaxe"]["archetype"], "pickaxe")
        self.assertEqual(
            by_id["steel_pickaxe"]["template"], "minecraft:item/iron_pickaxe.png"
        )
        self.assertEqual(by_id["steel_pickaxe"]["handle"], "tool_handle")
        self.assertEqual(by_id["steel_pickaxe"]["mineral"], "steel")
        self.assertNotIn("host", by_id["steel_pickaxe"])

        self.assertIn("steel_sword", by_id)
        self.assertEqual(by_id["steel_sword"]["archetype"], "sword")
        self.assertEqual(by_id["steel_sword"]["template"], "minecraft:item/iron_sword.png")
        self.assertEqual(by_id["steel_sword"]["handle"], "tool_handle")
        self.assertEqual(by_id["steel_sword"]["mineral"], "steel")
        self.assertNotIn("host", by_id["steel_sword"])

        self.assertIn("steel_shovel", by_id)
        self.assertEqual(by_id["steel_shovel"]["archetype"], "shovel")
        self.assertEqual(by_id["steel_shovel"]["template"], "minecraft:item/iron_shovel.png")
        self.assertEqual(by_id["steel_shovel"]["handle"], "tool_handle")
        self.assertEqual(by_id["steel_shovel"]["mineral"], "steel")
        self.assertNotIn("host", by_id["steel_shovel"])

        self.assertIn("steel_axe", by_id)
        self.assertEqual(by_id["steel_axe"]["archetype"], "axe")
        self.assertEqual(by_id["steel_axe"]["template"], "minecraft:item/iron_axe.png")
        self.assertEqual(by_id["steel_axe"]["handle"], "tool_handle")
        self.assertEqual(by_id["steel_axe"]["mineral"], "steel")
        self.assertNotIn("host", by_id["steel_axe"])

        self.assertIn("steel_hoe", by_id)
        self.assertEqual(by_id["steel_hoe"]["archetype"], "hoe")
        self.assertEqual(by_id["steel_hoe"]["template"], "minecraft:item/iron_hoe.png")
        self.assertEqual(by_id["steel_hoe"]["handle"], "tool_handle")
        self.assertEqual(by_id["steel_hoe"]["mineral"], "steel")
        self.assertNotIn("host", by_id["steel_hoe"])

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



class ApplyMineralItemPaletteTests(unittest.TestCase):
    def test_synthetic_rgba_preserves_alpha_and_rank_maps(self) -> None:
        template = np.zeros((2, 2, 4), dtype=np.uint8)
        template[0, 0] = [0, 0, 0, 0]
        template[0, 1] = [40, 40, 40, 255]
        template[1, 0] = [120, 120, 120, 200]
        template[1, 1] = [200, 200, 200, 128]
        minerals = ["#112233", "#334455", "#556677", "#778899"]
        out = apply_mineral_item_palette(template, minerals)
        self.assertEqual(out.shape, (2, 2, 4))
        self.assertEqual(int(out[0, 0, 3]), 0)
        self.assertEqual(tuple(out[0, 0, :3]), (0, 0, 0))
        self.assertEqual(int(out[0, 1, 3]), 255)
        self.assertEqual(int(out[1, 0, 3]), 200)
        self.assertEqual(int(out[1, 1, 3]), 128)
        opaque_hexes = {
            rgb_to_hex(tuple(int(c) for c in out[y, x, :3]))
            for y in range(2)
            for x in range(2)
            if out[y, x, 3] > 0
        }
        self.assertTrue(opaque_hexes <= set(minerals))
        self.assertIn(minerals[0], opaque_hexes)
        self.assertIn(minerals[-1], opaque_hexes)

    @unittest.skipUnless(
        (Path.home() / ".gradle/caches/fabric-loom").is_dir(),
        "Fabric Loom cache not present",
    )
    def test_diamond_gem_and_quartz_crystal_masks(self) -> None:
        try:
            diamond = load_template_rgba(DWM, "minecraft:item/diamond.png")
            quartz = load_template_rgba(DWM, "minecraft:item/quartz.png")
        except FileNotFoundError:
            self.skipTest("minecraft-client.jar not in Loom cache")

        az = load_palette(AZBANTIUM_JSON)
        zt = load_palette(ZEITON_JSON)
        az_hexes = palette_hexes(az)
        zt_hexes = palette_hexes(zt)

        gem = apply_mineral_item_palette(diamond, az_hexes)
        crystal = apply_mineral_item_palette(quartz, zt_hexes)

        np.testing.assert_array_equal(gem[:, :, 3], diamond[:, :, 3])
        np.testing.assert_array_equal(crystal[:, :, 3], quartz[:, :, 3])

        gem_cols = {
            rgb_to_hex(tuple(int(c) for c in gem[y, x, :3]))
            for y in range(gem.shape[0])
            for x in range(gem.shape[1])
            if gem[y, x, 3] > 0
        }
        crystal_cols = {
            rgb_to_hex(tuple(int(c) for c in crystal[y, x, :3]))
            for y in range(crystal.shape[0])
            for x in range(crystal.shape[1])
            if crystal[y, x, 3] > 0
        }
        self.assertTrue(gem_cols <= set(az_hexes))
        self.assertTrue(crystal_cols <= set(zt_hexes))


class ApplyToolItemPaletteTests(unittest.TestCase):
    def test_synthetic_tool_preserves_alpha_and_splits_channels(self) -> None:
        handle_src = [(0x28, 0x1E, 0x0B), (0x68, 0x4E, 0x1E)]
        metal_src = [(0x40, 0x40, 0x40), (0xC0, 0xC0, 0xC0)]
        template = np.zeros((2, 2, 4), dtype=np.uint8)
        template[0, 0] = [*handle_src[0], 255]
        template[0, 1] = [*handle_src[1], 200]
        template[1, 0] = [*metal_src[0], 255]
        template[1, 1] = [*metal_src[1], 128]
        handle_hexes = ["#281E0B", "#684E1E"]
        metal_hexes = ["#13191E", "#58616A", "#949698"]
        handle_colours = frozenset(handle_src)
        out = apply_tool_item_palette(
            template, handle_hexes, metal_hexes, handle_colours
        )
        self.assertEqual(out.shape, (2, 2, 4))
        self.assertEqual(int(out[0, 0, 3]), 255)
        self.assertEqual(int(out[0, 1, 3]), 200)
        self.assertEqual(int(out[1, 0, 3]), 255)
        self.assertEqual(int(out[1, 1, 3]), 128)
        handle_out = {
            rgb_to_hex(tuple(int(c) for c in out[0, x, :3])) for x in range(2)
        }
        metal_out = {
            rgb_to_hex(tuple(int(c) for c in out[1, x, :3])) for x in range(2)
        }
        self.assertTrue(handle_out <= set(handle_hexes))
        self.assertTrue(metal_out <= set(metal_hexes))
        self.assertIn(metal_hexes[0], metal_out)
        self.assertIn(metal_hexes[-1], metal_out)

    @unittest.skipUnless(
        (Path.home() / ".gradle/caches/fabric-loom").is_dir(),
        "Fabric Loom cache not present",
    )
    def test_iron_tools_split_against_stick_and_remap_to_steel(self) -> None:
        try:
            handle_colours = vanilla_tool_handle_colours(DWM)
            sword = load_template_rgba(DWM, "minecraft:item/iron_sword.png")
            pickaxe = load_template_rgba(DWM, "minecraft:item/iron_pickaxe.png")
            ingot = load_template_rgba(DWM, "minecraft:item/iron_ingot.png")
        except FileNotFoundError:
            self.skipTest("minecraft-client.jar not in Loom cache")

        stick_hexes = {rgb_to_hex(c) for c in handle_colours}
        self.assertEqual(stick_hexes, set(STICK_HANDLE_HEXES))

        for name, tool in (("sword", sword), ("pickaxe", pickaxe)):
            handle_src, metal_src = split_tool_colours(tool, handle_colours)
            self.assertGreaterEqual(len(handle_src), 1, name)
            self.assertGreaterEqual(len(metal_src), 1, name)
            for c in handle_src:
                self.assertIn(c, handle_colours, name)

        handle_palette = load_palette(TOOL_HANDLE_JSON)
        steel_palette = load_palette(STEEL_JSON)
        handle_hexes = palette_hexes(handle_palette)
        steel_hexes = palette_hexes(steel_palette)

        for name, tool in (("sword", sword), ("pickaxe", pickaxe)):
            out = apply_tool_item_palette(
                tool, handle_hexes, steel_hexes, handle_colours
            )
            np.testing.assert_array_equal(out[:, :, 3], tool[:, :, 3], err_msg=name)
            handle_src, metal_src = split_tool_colours(tool, handle_colours)
            handle_set = set(handle_src)
            metal_set = set(metal_src)
            allowed = set(handle_hexes) | set(steel_hexes)
            h, w, _ = tool.shape
            for y in range(h):
                for x in range(w):
                    if out[y, x, 3] == 0:
                        continue
                    src_key = (
                        int(tool[y, x, 0]),
                        int(tool[y, x, 1]),
                        int(tool[y, x, 2]),
                    )
                    out_hex = rgb_to_hex(tuple(int(c) for c in out[y, x, :3]))
                    self.assertIn(out_hex, allowed, name)
                    if src_key in handle_set:
                        self.assertIn(out_hex, set(handle_hexes), name)
                    else:
                        self.assertIn(src_key, metal_set, name)
                        self.assertIn(out_hex, set(steel_hexes), name)

        steel_ingot = apply_mineral_item_palette(ingot, steel_hexes)
        np.testing.assert_array_equal(steel_ingot[:, :, 3], ingot[:, :, 3])
        ingot_cols = {
            rgb_to_hex(tuple(int(c) for c in steel_ingot[y, x, :3]))
            for y in range(steel_ingot.shape[0])
            for x in range(steel_ingot.shape[1])
            if steel_ingot[y, x, 3] > 0
        }
        self.assertTrue(ingot_cols <= set(steel_hexes))
        self.assertIn(steel_hexes[0], ingot_cols)
        self.assertIn(steel_hexes[-1], ingot_cols)


if __name__ == "__main__":
    raise SystemExit(unittest.main())
