#!/usr/bin/env python3
"""Generate Markdown + PNG swatch docs from a family colour-palette JSON file.

Offline helper for block/item family palettes. Not invoked by Gradle or CI.

Example (from repo root)::

    dwm/tools/.venv/bin/python dwm/tools/generate_family_palette_docs.py \\
      --palette dwm/docs/palettes/zeiton.json \\
      --out-dir dwm/docs/palettes
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any

import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle

HEX_RE = re.compile(r"^#[0-9A-Fa-f]{6}$")


def parse_hex(value: str) -> tuple[float, float, float]:
    raw = value.lstrip("#")
    return tuple(int(raw[i : i + 2], 16) / 255.0 for i in (0, 2, 4))  # type: ignore[return-value]


def luminance(rgb: tuple[float, float, float]) -> float:
    r, g, b = rgb
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def load_palette(path: Path) -> dict[str, Any]:
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        raise ValueError(f"{path}: root must be a JSON object")

    family_id = data.get("family_id")
    display_name = data.get("display_name")
    roles = data.get("roles")
    if not isinstance(family_id, str) or not family_id:
        raise ValueError(f"{path}: family_id must be a non-empty string")
    if not isinstance(display_name, str) or not display_name:
        raise ValueError(f"{path}: display_name must be a non-empty string")
    if not isinstance(roles, list) or not roles:
        raise ValueError(f"{path}: roles must be a non-empty list")

    seen: set[str] = set()
    normalized: list[dict[str, str]] = []
    for i, entry in enumerate(roles):
        if not isinstance(entry, dict):
            raise ValueError(f"{path}: roles[{i}] must be an object")
        role = entry.get("role")
        hex_value = entry.get("hex")
        notes = entry.get("notes", "")
        if not isinstance(role, str) or not role:
            raise ValueError(f"{path}: roles[{i}].role must be a non-empty string")
        if role in seen:
            raise ValueError(f"{path}: duplicate role {role!r}")
        seen.add(role)
        if not isinstance(hex_value, str) or not HEX_RE.match(hex_value):
            raise ValueError(
                f"{path}: roles[{i}].hex must be #RRGGBB, got {hex_value!r}"
            )
        if notes is None:
            notes = ""
        if not isinstance(notes, str):
            raise ValueError(f"{path}: roles[{i}].notes must be a string")
        normalized.append(
            {"role": role, "hex": hex_value.upper(), "notes": notes}
        )

    map_color = data.get("map_color")
    if map_color is not None and not isinstance(map_color, str):
        raise ValueError(f"{path}: map_color must be a string when present")
    notes = data.get("notes", "")
    if notes is None:
        notes = ""
    if not isinstance(notes, str):
        raise ValueError(f"{path}: notes must be a string when present")

    return {
        "family_id": family_id,
        "display_name": display_name,
        "map_color": map_color,
        "notes": notes,
        "roles": normalized,
    }


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
    ]
    if palette.get("map_color"):
        lines.append(f"Map colour: `{palette['map_color']}`")
        lines.append("")
    if palette.get("notes"):
        lines.append(palette["notes"])
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
        notes = entry["notes"].replace("|", "\\|")
        lines.append(f"| `{entry['role']}` | `{entry['hex']}` | {notes} |")
    lines.append("")
    lines.append(
        "Source JSON: "
        f"[`{palette['family_id']}.json`](./{palette['family_id']}.json). "
        "Regenerate with `tools/generate_family_palette_docs.py` "
        "(from `dwm/`: `--palette docs/palettes/<id>.json --out-dir docs/palettes`)."
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


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description="Generate family palette Markdown docs and PNG swatches."
    )
    parser.add_argument(
        "--palette",
        type=Path,
        required=True,
        help="Path to palette JSON (role→hex definition).",
    )
    parser.add_argument(
        "--out-dir",
        type=Path,
        required=True,
        help="Directory for <family_id>.md and <family_id>-swatch.png.",
    )
    args = parser.parse_args(argv)

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
