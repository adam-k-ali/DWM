#!/usr/bin/env python3
"""Copy allowlisted textures from the DWM asset archive into the mod resources.

Local / agent tooling only — not wired into Gradle or CI.

Example:
  python3 tools/import_archive_textures.py --family gallifrey_stone
  python3 tools/import_archive_textures.py --family gallifrey_stone --dry-run
  python3 tools/import_archive_textures.py --list
"""

from __future__ import annotations

import argparse
import shutil
import sys
from pathlib import Path

DEFAULT_ARCHIVE = Path("/Users/adamali/Developer/Assets/DWM-Asset-Archive/assets/dwm")
REPO_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_DEST = REPO_ROOT / "src" / "client" / "resources" / "assets" / "dwm"

# Paths are relative to archive assets/dwm (never unstructured/).
FAMILIES: dict[str, list[str]] = {
    "gallifrey_stone": [
        "textures/block/gallifrey_stone.png",
        "textures/block/gallifrey_stone_bricks.png",
        "textures/block/chiseled_gallifrey_stone_bricks.png",
        "textures/block/cracked_gallifrey_stone_bricks.png",
        "textures/block/mossy_gallifrey_stone_bricks.png",
        "textures/block/gallifrey_cobblestone.png",
        "textures/block/gallifrey_mossy_cobblestone.png",
        "textures/block/gallifrey_smooth_stone.png",
        "textures/block/gallifrey_dirt.png",
        "textures/block/gallifrey_coarse_dirt.png",
        "textures/block/gallifrey_sand.png",
        "textures/block/gallifrey_cut_sandstone.png",
        "textures/block/gallifrey_chiseled_sandstone.png",
        "textures/block/gallifrey_sandstone.png",
        "textures/block/gallifrey_sandstone_top.png",
        "textures/block/gallifrey_sandstone_bottom.png",
    ],
}


def list_families() -> None:
    for name, paths in sorted(FAMILIES.items()):
        print(f"{name} ({len(paths)} files)")
        for path in paths:
            print(f"  - {path}")


def import_family(
    family: str,
    archive_root: Path,
    dest_root: Path,
    dry_run: bool,
) -> int:
    if family not in FAMILIES:
        print(f"Unknown family: {family}", file=sys.stderr)
        print(f"Known families: {', '.join(sorted(FAMILIES))}", file=sys.stderr)
        return 1

    if not archive_root.is_dir():
        print(f"Archive root not found: {archive_root}", file=sys.stderr)
        return 1

    copied = 0
    missing: list[str] = []

    for rel in FAMILIES[family]:
        if "unstructured" in Path(rel).parts:
            print(f"Refusing unstructured path: {rel}", file=sys.stderr)
            return 1

        src = archive_root / rel
        dst = dest_root / rel

        if not src.is_file():
            missing.append(rel)
            continue

        print(f"{'[dry-run] ' if dry_run else ''}{src} -> {dst}")
        if not dry_run:
            dst.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(src, dst)
        copied += 1

    if missing:
        print("Missing in archive:", file=sys.stderr)
        for path in missing:
            print(f"  - {path}", file=sys.stderr)
        return 1

    print(f"{'Would copy' if dry_run else 'Copied'} {copied} file(s) for family '{family}'.")
    return 0


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--archive",
        type=Path,
        default=DEFAULT_ARCHIVE,
        help=f"Archive assets/dwm root (default: {DEFAULT_ARCHIVE})",
    )
    parser.add_argument(
        "--dest",
        type=Path,
        default=DEFAULT_DEST,
        help=f"Mod assets/dwm destination (default: {DEFAULT_DEST})",
    )
    parser.add_argument(
        "--family",
        help="Family allowlist to import (e.g. gallifrey_stone)",
    )
    parser.add_argument(
        "--list",
        action="store_true",
        help="List known families and exit",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print planned copies without writing files",
    )
    args = parser.parse_args(argv)

    if args.list:
        list_families()
        return 0

    if not args.family:
        parser.error("--family is required unless --list is set")

    return import_family(args.family, args.archive, args.dest, args.dry_run)


if __name__ == "__main__":
    raise SystemExit(main())
