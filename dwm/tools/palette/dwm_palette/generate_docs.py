"""Generate Markdown + PNG swatch docs from a palette JSON file.

Offline helper for block/item family palettes. Not invoked by Gradle or CI.

Docs example (from repo root)::

    poetry -C dwm/tools/palette run generate-family-palette-docs \\
      --palette dwm/docs/palettes/zeiton.json \\
      --out-dir dwm/docs/palettes

GUI example::

    poetry -C dwm/tools/palette run generate-family-palette-docs --gui
    # Optional: --mineral-palette … --ore-template …
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any

import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle

from dwm_palette.palette import (
    default_ore_template_from_products,
    is_minecraft_template,
    load_template_rgb,
    vanilla_stone_host_colours,
)
from dwm_palette.paths import find_dwm_root
from dwm_palette.recolor import host_colour_set, load_palette, load_rgb_image, luminance, parse_hex

DWM_DIR = find_dwm_root()
DEFAULT_PALETTE = DWM_DIR / "docs" / "palettes" / "gallifrey_stone.json"
DEFAULT_MINERAL_PALETTE = DWM_DIR / "docs" / "palettes" / "azbantium.json"
DEFAULT_TEMPLATE = (
    DWM_DIR
    / "src"
    / "client"
    / "resources"
    / "assets"
    / "dwm"
    / "textures"
    / "block"
    / "gallifrey_stone.png"
)
DEFAULT_ORE_SAVE_DIR = (
    DWM_DIR
    / "src"
    / "client"
    / "resources"
    / "assets"
    / "dwm"
    / "textures"
    / "block"
)


def write_swatch(palette: dict[str, Any], out_path: Path) -> None:
    roles = palette["roles"]
    n = len(roles)
    cell_w = 1.6
    fig_w = max(4.0, cell_w * n)
    fig_h = 2.2
    fig, ax = plt.subplots(figsize=(fig_w, fig_h), dpi=120)
    ax.set_xlim(0, n)
    ax.set_ylim(0, 1)
    ax.set_axis_off()
    fig.subplots_adjust(left=0.02, right=0.98, top=0.78, bottom=0.22)

    for i, entry in enumerate(roles):
        rgb = parse_hex(entry["hex"])
        ax.add_patch(Rectangle((i, 0.28), 1.0, 0.72, facecolor=rgb, edgecolor="#222222", linewidth=0.8))
        label_color = "#FFFFFF" if luminance(rgb) < 0.55 else "#111111"
        ax.text(
            i + 0.5,
            0.64,
            entry["hex"],
            ha="center",
            va="center",
            fontsize=8,
            color=label_color,
            fontfamily="monospace",
        )
        ax.text(
            i + 0.5,
            0.12,
            entry["role"],
            ha="center",
            va="center",
            fontsize=7,
            color="#222222",
            rotation=0,
        )

    ax.set_title(f"{palette['display_name']} palette", fontsize=11, pad=8)
    out_path.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(out_path, bbox_inches="tight", facecolor="white")
    plt.close(fig)


def write_markdown(palette: dict[str, Any], out_path: Path, swatch_name: str) -> None:
    lines: list[str] = [
        f"# Palette — {palette['display_name']}",
        "",
        f"Family id: `{palette['family_id']}`",
        "",
        f"Seed: `{palette['seed']}` · Profile: `{palette['profile']}`",
        "",
    ]
    if palette.get("map_color"):
        lines.append(f"Map colour: `{palette['map_color']}`")
        lines.append("")
    if palette.get("notes"):
        lines.append(palette["notes"])
        lines.append("")

    lines.append(
        "Step hexes below are **generated** from the seed and named contrast profile "
        "(not hand-authored)."
    )
    lines.append("")

    lines.extend(
        [
            f"![{palette['display_name']} colour swatch](./{swatch_name})",
            "",
            "| Role | Hex | Notes |",
            "|------|-----|-------|",
        ]
    )
    for entry in palette["roles"]:
        notes = entry["notes"].replace("|", "\\|") if entry["notes"] else ""
        lines.append(f"| `{entry['role']}` | `{entry['hex']}` | {notes} |")
    lines.append("")
    lines.append(
        "Source JSON: "
        f"[`{palette['family_id']}.json`](./{palette['family_id']}.json). "
        "Regenerate with `poetry -C dwm/tools/palette run generate-family-palette-docs` "
        "(from repo root: `--palette dwm/docs/palettes/<id>.json --out-dir dwm/docs/palettes`)."
    )
    lines.append("")

    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text("\n".join(lines), encoding="utf-8")


def generate(palette_path: Path, out_dir: Path) -> tuple[Path, Path]:
    palette = load_palette(palette_path)
    family_id = palette["family_id"]
    swatch_name = f"{family_id}-swatch.png"
    md_path = out_dir / f"{family_id}.md"
    swatch_path = out_dir / swatch_name
    write_swatch(palette, swatch_path)
    write_markdown(palette, md_path, swatch_name)
    return md_path, swatch_path


def _resolve_ore_for_gui(
    ore_template_arg: Path | None,
) -> tuple[Any, frozenset, str, Path]:
    """Load ore RGB + host classifier colours for the GUI.

    Default is the products.json template (vanilla emerald from the Loom jar),
    classified against vanilla ``stone.png``. A filesystem ``--ore-template``
    override uses Gallifrey stone colours from the stone cube template path
    only when the override is a DWM-owned PNG (already on host hexes).
    """
    if ore_template_arg is not None:
        path = ore_template_arg.resolve()
        if not path.is_file():
            raise FileNotFoundError(f"ore template not found: {path}")
        ore_rgb = load_rgb_image(path)
        # DWM remapped ores (e.g. gallifrey_coal_ore) sit on host hexes;
        # classify against the stone cube template the GUI already loaded.
        stone_rgb = load_rgb_image(DEFAULT_TEMPLATE)
        return (
            ore_rgb,
            host_colour_set(stone_rgb),
            path.name,
            path.parent,
        )

    template_id = default_ore_template_from_products(DWM_DIR)
    ore_rgb = load_template_rgb(DWM_DIR, template_id)
    if is_minecraft_template(template_id):
        host_colours = vanilla_stone_host_colours(DWM_DIR)
        label = template_id
    else:
        stone_rgb = load_rgb_image(DEFAULT_TEMPLATE)
        host_colours = host_colour_set(stone_rgb)
        label = Path(template_id).name
    return ore_rgb, host_colours, label, DEFAULT_ORE_SAVE_DIR


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description=(
            "Generate family palette Markdown docs and PNG swatches, "
            "or open the palette recolour GUI."
        )
    )
    parser.add_argument(
        "--gui",
        action="store_true",
        help="Open the interactive palette recolour GUI.",
    )
    parser.add_argument(
        "--palette",
        type=Path,
        help="Path to host palette JSON (seed + profile).",
    )
    parser.add_argument(
        "--out-dir",
        type=Path,
        help="Directory for <family_id>.md and <family_id>-swatch.png.",
    )
    parser.add_argument(
        "--template",
        type=Path,
        help="Stone template PNG for GUI host preview / classification "
        "(default: gallifrey_stone.png).",
    )
    parser.add_argument(
        "--mineral-palette",
        type=Path,
        help="Mineral palette JSON for ore preview (default: azbantium.json).",
    )
    parser.add_argument(
        "--ore-template",
        type=Path,
        help="Ore-in-stone template PNG for GUI ore preview "
        "(default: from products.json → minecraft:block/emerald_ore.png via Loom jar).",
    )
    args = parser.parse_args(argv)

    if args.gui:
        palette_path = args.palette or DEFAULT_PALETTE
        template_path = args.template or DEFAULT_TEMPLATE
        mineral_palette_path = args.mineral_palette or DEFAULT_MINERAL_PALETTE
        try:
            ore_rgb, ore_host_colours, ore_label, ore_save_dir = _resolve_ore_for_gui(
                args.ore_template
            )
        except (OSError, ValueError, json.JSONDecodeError) as exc:
            print(f"error: {exc}", file=sys.stderr)
            return 1
        try:
            from dwm_palette.gui import run_gui

            run_gui(
                palette_path=palette_path,
                template_path=template_path,
                mineral_palette_path=mineral_palette_path,
                ore_rgb=ore_rgb,
                ore_host_colours=ore_host_colours,
                ore_template_label=ore_label,
                ore_save_dir=ore_save_dir,
            )
        except ImportError as exc:
            print(
                "error: GUI dependencies missing "
                f"({exc}). Run `poetry -C dwm/tools/palette install`; "
                "on Homebrew Python also install python-tk "
                "(e.g. brew install python-tk@3.14).",
                file=sys.stderr,
            )
            return 1
        except (OSError, ValueError, json.JSONDecodeError) as exc:
            print(f"error: {exc}", file=sys.stderr)
            return 1
        return 0

    if args.palette is None or args.out_dir is None:
        parser.error("--palette and --out-dir are required unless --gui is set")

    try:
        md_path, swatch_path = generate(args.palette, args.out_dir)
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"error: {exc}", file=sys.stderr)
        return 1

    print(f"wrote {md_path}")
    print(f"wrote {swatch_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
