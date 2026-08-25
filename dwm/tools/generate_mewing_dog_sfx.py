#!/usr/bin/env python3
"""
Synthesize Mewing Dog ambient SFX.

Gallifrey forest dogs that mew (PROSE: The Twins in the Wood). Soft cat-like
mews — short tonal chirps with a light breathy onset. Original synthesis only.

Requires numpy; writes WAV then ffmpeg → OGG (same path as Flutterwing SFX).
"""

from __future__ import annotations

import argparse
import math
import subprocess
import sys
import tempfile
import wave
from pathlib import Path

import numpy as np

SR = 44100


def write_wav(path: Path, samples: np.ndarray, sample_rate: int = SR) -> None:
    """Write stereo WAV (duplicated mono) — Homebrew ffmpeg's vorbis encoder requires 2ch."""
    clipped = np.clip(samples, -1.0, 1.0)
    mono = (clipped * 32767.0).astype(np.int16)
    stereo = np.empty(mono.size * 2, dtype=np.int16)
    stereo[0::2] = mono
    stereo[1::2] = mono
    with wave.open(str(path), "wb") as wf:
        wf.setnchannels(2)
        wf.setsampwidth(2)
        wf.setframerate(sample_rate)
        wf.writeframes(stereo.tobytes())


def wav_to_ogg(wav_path: Path, ogg_path: Path) -> None:
    attempts = [
        ["ffmpeg", "-y", "-i", str(wav_path), "-c:a", "libvorbis", "-q:a", "8", str(ogg_path)],
        ["ffmpeg", "-y", "-i", str(wav_path), "-c:a", "vorbis", "-strict", "-2", str(ogg_path)],
    ]
    last_err: subprocess.CalledProcessError | None = None
    for cmd in attempts:
        try:
            subprocess.run(cmd, check=True, capture_output=True)
            return
        except subprocess.CalledProcessError as err:
            last_err = err
    assert last_err is not None
    raise last_err


def fade(signal: np.ndarray, fade_in_s: float, fade_out_s: float, sr: int = SR) -> np.ndarray:
    out = signal.copy()
    n_in = min(len(out), int(fade_in_s * sr))
    n_out = min(len(out), int(fade_out_s * sr))
    if n_in > 1:
        out[:n_in] *= np.linspace(0.0, 1.0, n_in)
    if n_out > 1:
        out[-n_out:] *= np.linspace(1.0, 0.0, n_out)
    return out


def peak_normalize(signal: np.ndarray, peak: float = 0.82) -> np.ndarray:
    mag = float(np.max(np.abs(signal))) or 1.0
    return signal / mag * peak


def fft_bandpass(signal: np.ndarray, low: float, high: float, order: int = 4) -> np.ndarray:
    n = len(signal)
    spec = np.fft.rfft(signal)
    freqs = np.maximum(np.fft.rfftfreq(n, 1.0 / SR), 1e-6)
    lp = 1.0 / np.sqrt(1.0 + (freqs / high) ** (2 * order))
    hp = 1.0 / np.sqrt(1.0 + (low / freqs) ** (2 * order))
    return np.fft.irfft(spec * lp * hp, n=n)


def hann(n: int) -> np.ndarray:
    if n <= 1:
        return np.ones(n)
    return 0.5 - 0.5 * np.cos(2.0 * math.pi * np.arange(n) / max(n - 1, 1))


def mew_chirp(
    n: int,
    t0: float,
    duration_s: float,
    f0: float,
    f1: float,
    amp: float,
) -> np.ndarray:
    out = np.zeros(n)
    i0 = int(round(t0 * SR))
    length = int(round(duration_s * SR))
    if length <= 1 or i0 >= n or i0 < 0:
        return out
    length = min(length, n - i0)
    t = np.arange(length) / SR
    # Slight upward-then-settle mew contour
    mid = duration_s * 0.35
    freq = np.where(
        t < mid,
        f0 + (f1 - f0) * (t / mid),
        f1 + (f0 * 0.92 - f1) * ((t - mid) / max(duration_s - mid, 1e-4)),
    )
    phase = 2.0 * math.pi * np.cumsum(freq) / SR
    # Soft odd harmonics for a cat-like tone (not a pure sine)
    tone = (
        np.sin(phase)
        + 0.28 * np.sin(2.0 * phase)
        + 0.12 * np.sin(3.0 * phase)
    )
    env = hann(length) * np.exp(-t / max(duration_s * 0.55, 1e-4))
    out[i0 : i0 + length] = tone * env * amp
    return out


def breath_onset(n: int, rng: np.random.Generator, t0: float, duration_s: float, amp: float) -> np.ndarray:
    out = np.zeros(n)
    i0 = int(round(t0 * SR))
    length = int(round(duration_s * SR))
    if length <= 1 or i0 >= n or i0 < 0:
        return out
    length = min(length, n - i0)
    noise = fft_bandpass(rng.standard_normal(length), 800.0, 4200.0, order=3)
    out[i0 : i0 + length] = noise * hann(length) * amp
    return out


def synthesize_ambient(rng: np.random.Generator, *, variant: int = 0) -> np.ndarray:
    duration = 0.72 if variant == 0 else 0.86
    n = int(duration * SR)
    f0 = 780.0 if variant == 0 else 690.0
    f1 = 1180.0 if variant == 0 else 1040.0
    # One or two soft mews
    mix = mew_chirp(n, 0.04, 0.28 if variant == 0 else 0.34, f0, f1, 0.72)
    mix += breath_onset(n, rng, 0.02, 0.08, 0.12)
    if variant == 1:
        mix += mew_chirp(n, 0.42, 0.26, f0 * 1.08, f1 * 0.95, 0.48)
        mix += breath_onset(n, rng, 0.40, 0.07, 0.08)
    # Tiny room noise so it doesn't sound dry/synthetic
    hiss = fft_bandpass(rng.standard_normal(n), 200.0, 6000.0, order=2) * 0.015
    return fade(peak_normalize(mix + hiss, 0.78), 0.004, 0.08)


def spectral_centroid(signal: np.ndarray) -> float:
    spec = np.abs(np.fft.rfft(signal))
    freqs = np.fft.rfftfreq(len(signal), 1.0 / SR)
    mag = float(np.sum(spec)) or 1.0
    return float(np.sum(freqs * spec) / mag)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--out-dir",
        type=Path,
        default=Path(__file__).resolve().parents[1]
        / "src/client/resources/assets/dwm/sounds/entity/mewing_dog",
    )
    parser.add_argument("--seed", type=int, default=1963)
    args = parser.parse_args()

    out_dir: Path = args.out_dir
    out_dir.mkdir(parents=True, exist_ok=True)

    rng = np.random.default_rng(args.seed)
    jobs = [
        ("ambient", synthesize_ambient(rng, variant=0)),
        ("ambient_2", synthesize_ambient(rng, variant=1)),
    ]

    with tempfile.TemporaryDirectory(prefix="mewing_dog_sfx_") as tmp:
        tmp_dir = Path(tmp)
        for name, samples in jobs:
            wav_path = tmp_dir / f"{name}.wav"
            ogg_path = out_dir / f"{name}.ogg"
            write_wav(wav_path, samples)
            wav_to_ogg(wav_path, ogg_path)
            centroid = spectral_centroid(samples)
            print(
                f"Wrote {ogg_path} ({len(samples) / SR:.2f}s) "
                f"centroid={centroid:.0f}Hz peak={float(np.max(np.abs(samples))):.2f}"
            )
    return 0


if __name__ == "__main__":
    sys.exit(main())
