#!/usr/bin/env python3
"""
Download a public TARDIS materialisation clip for local analysis only.

Writes tools/fixtures/tardis_ref.wav (gitignored). Never package this into the mod.
"""

from __future__ import annotations

import argparse
import subprocess
import sys
import urllib.request
from pathlib import Path

DEFAULT_URL = "https://www.myinstants.com/media/sounds/tardis.mp3"
FIXTURES = Path(__file__).resolve().parent / "fixtures"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--url", default=DEFAULT_URL)
    parser.add_argument(
        "--out",
        type=Path,
        default=FIXTURES / "tardis_ref.wav",
        help="Output WAV path (default: tools/fixtures/tardis_ref.wav)",
    )
    args = parser.parse_args()

    out: Path = args.out
    out.parent.mkdir(parents=True, exist_ok=True)
    mp3 = out.with_suffix(".mp3")

    print(f"Fetching {args.url}")
    req = urllib.request.Request(args.url, headers={"User-Agent": "Mozilla/5.0 (DWM-sfx-tools)"})
    with urllib.request.urlopen(req, timeout=60) as resp:
        mp3.write_bytes(resp.read())
    print(f"Wrote {mp3} ({mp3.stat().st_size} bytes)")

    subprocess.run(
        ["ffmpeg", "-y", "-i", str(mp3), str(out)],
        check=True,
        capture_output=True,
    )
    print(f"Wrote {out}")
    print("Reminder: fixtures audio is analysis-only and gitignored — do not commit or ship it.")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:  # noqa: BLE001 — CLI surface
        print(f"error: {exc}", file=sys.stderr)
        raise SystemExit(1)
