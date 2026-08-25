#!/usr/bin/env python3
"""
Compare generated TARDIS travel SFX against a local golden reference.

Prints a failure-mode metric table, writes envelope/spectrogram/centroid/band
PNGs + markdown report, and optionally plays a loudness-matched A/B clip.

Golden audio is analysis-only (tools/fixtures/) and must never be packaged.
"""

from __future__ import annotations

import argparse
import platform
import subprocess
import sys
from pathlib import Path

from tardis_sfx_analysis import (
    SR,
    build_metric_rows,
    format_metric_table,
    hard_gate_failures,
    load_audio_mono,
    ref_analysis_slice,
    resample_linear,
    similarity_detail,
    similarity_score,
    write_ab_wav,
    write_compare_plots,
    write_markdown_report,
)

ROOT = Path(__file__).resolve().parent
DEFAULT_REF = ROOT / "fixtures" / "tardis_ref.wav"
DEFAULT_OURS = (
    ROOT.parent / "src/client/resources/assets/dwm/sounds/tardis_dematerialise_loop.ogg"
)
DEFAULT_OUT = ROOT / "fixtures" / "compare_out"


def play_wav(path: Path) -> None:
    system = platform.system()
    if system == "Darwin":
        subprocess.run(["afplay", str(path)], check=False)
    elif system == "Linux":
        for cmd in (["paplay", str(path)], ["aplay", str(path)]):
            try:
                subprocess.run(cmd, check=True, capture_output=True)
                return
            except (FileNotFoundError, subprocess.CalledProcessError):
                continue
        print(f"Wrote A/B WAV but no player found: {path}", file=sys.stderr)
    else:
        print(f"Wrote A/B WAV (open manually): {path}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ref", type=Path, default=DEFAULT_REF, help="Golden WAV/MP3")
    parser.add_argument("--ours", type=Path, default=DEFAULT_OURS, help="Generated OGG/WAV")
    parser.add_argument(
        "--mat",
        type=Path,
        default=None,
        help="Optional materialise loop to include in the report",
    )
    parser.add_argument("--out-dir", type=Path, default=DEFAULT_OUT)
    parser.add_argument(
        "--play-ab",
        action="store_true",
        help="Play loudness-matched ours/golden/ours after writing report",
    )
    parser.add_argument(
        "--gate",
        action="store_true",
        help="Exit 1 if hard spectral gates fail (same as generator --validate-ref)",
    )
    args = parser.parse_args()

    if not args.ref.exists():
        print(
            f"Missing golden reference: {args.ref}\n"
            f"Run: tools/.venv/bin/python tools/fetch_tardis_ref.py",
            file=sys.stderr,
        )
        return 2
    if not args.ours.exists():
        print(
            f"Missing generated audio: {args.ours}\n"
            f"Run: tools/.venv/bin/python tools/generate_tardis_travel_sfx.py",
            file=sys.stderr,
        )
        return 2

    ref, ref_sr = load_audio_mono(args.ref)
    ref = resample_linear(ref_analysis_slice(ref, ref_sr), ref_sr, SR)

    ours, ours_sr = load_audio_mono(args.ours)
    ours = resample_linear(ours, ours_sr, SR)

    rows = build_metric_rows(ours, ref, SR)
    sim = similarity_score(ours, ref, SR)
    print(f"similarity: {sim.score:.1f}/100  ({similarity_detail(sim)})")
    print()
    print(format_metric_table(rows))
    print()

    out_dir: Path = args.out_dir
    plots = write_compare_plots(ours, ref, out_dir, SR, label="demat")

    mat_path = args.mat
    if mat_path is None:
        candidate = args.ours.parent / "tardis_materialise_loop.ogg"
        if candidate.exists():
            mat_path = candidate
    if mat_path is not None and mat_path.exists():
        mat, mat_sr = load_audio_mono(mat_path)
        mat = resample_linear(mat, mat_sr, SR)
        print("\n## materialise\n")
        mat_rows = build_metric_rows(mat, ref, SR)
        print(format_metric_table(mat_rows))
        plots.extend(write_compare_plots(mat, ref, out_dir, SR, label="mat"))
        # Prefer demat rows for gate/summary; keep mat plots in report
        report_rows = rows
    else:
        report_rows = rows

    ab_path = out_dir / "ab_demat.wav"
    write_ab_wav(ours, ref, ab_path, SR)

    report_path = out_dir / "report.md"
    write_markdown_report(
        report_rows,
        plots,
        report_path,
        ours_path=args.ours,
        ref_path=args.ref,
    )
    print(f"\nWrote report {report_path}")
    print(f"Wrote A/B wav {ab_path}")
    for p in plots:
        print(f"  plot {p}")

    if args.play_ab:
        print("Playing A/B (ours → golden → ours)…")
        play_wav(ab_path)

    if args.gate:
        failures = hard_gate_failures(ours, ref, SR)
        if failures:
            print("\nHARD GATE FAILED:", file=sys.stderr)
            for msg in failures:
                print(f"  - {msg}", file=sys.stderr)
            return 1
        print("\nHard gate OK.")
    return 0


if __name__ == "__main__":
    # Allow running as tools/compare_tardis_sfx.py without installing a package.
    sys.path.insert(0, str(Path(__file__).resolve().parent))
    raise SystemExit(main())
