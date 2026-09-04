#!/usr/bin/env python3
"""
Synthesize Dalek ambient / hurt / death / shoot SFX.

Original mechanical synthesis only — metallic grate, laser zap, casing impact.
Does not sample or imitate BBC Dalek speech.

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


def metallic_partials(n: int, t0: float, duration_s: float, freqs: list[float], amp: float) -> np.ndarray:
    out = np.zeros(n)
    i0 = int(round(t0 * SR))
    length = int(round(duration_s * SR))
    if length <= 1 or i0 >= n:
        return out
    length = min(length, n - max(i0, 0))
    if i0 < 0:
        return out
    t = np.arange(length) / SR
    decay = np.exp(-t / max(duration_s * 0.28, 1e-4))
    window = 0.5 - 0.5 * np.cos(2.0 * math.pi * np.arange(length) / max(length - 1, 1))
    mix = np.zeros(length)
    for i, freq in enumerate(freqs):
        mix += np.sin(2.0 * math.pi * freq * t) * (0.55 ** i)
    out[i0 : i0 + length] = mix * decay * window * amp
    return out


def synthesize_ambient(rng: np.random.Generator, *, variant: int = 0) -> np.ndarray:
    duration = 0.85 if variant == 0 else 0.96
    n = int(duration * SR)
    t = np.arange(n) / SR
    grate = fft_bandpass(rng.standard_normal(n), 180.0, 1400.0, order=3)
    servo = np.sin(2.0 * math.pi * (42.0 if variant == 0 else 36.0) * t)
    servo *= 0.18 + 0.08 * np.sin(2.0 * math.pi * 3.2 * t)
    clicks = np.zeros(n)
    for t0 in (0.08, 0.31, 0.54, 0.72):
        clicks += metallic_partials(n, t0 + float(rng.uniform(-0.02, 0.02)), 0.07, [720.0, 1180.0, 1830.0], 0.12)
    mix = grate * 0.42 + servo * 0.55 + clicks
    return fade(peak_normalize(mix, 0.62), 0.02, 0.12)


def synthesize_hurt(rng: np.random.Generator) -> np.ndarray:
    duration = 0.32
    n = int(duration * SR)
    impact = fft_bandpass(rng.standard_normal(n), 200.0, 4200.0, order=2)
    env = np.exp(-np.arange(n) / SR / 0.07)
    clang = metallic_partials(n, 0.0, 0.22, [510.0, 980.0, 1640.0, 2470.0], 0.7)
    mix = impact * env * 0.55 + clang
    return fade(peak_normalize(mix, 0.88), 0.001, 0.06)


def synthesize_death(rng: np.random.Generator) -> np.ndarray:
    duration = 0.95
    n = int(duration * SR)
    t = np.arange(n) / SR
    wind_down = np.sin(2.0 * math.pi * (90.0 - 55.0 * t) * t) * np.exp(-t * 2.4)
    scrapes = fft_bandpass(rng.standard_normal(n), 90.0, 900.0, order=2) * np.exp(-t * 1.8)
    clangs = (
        metallic_partials(n, 0.02, 0.28, [430.0, 870.0, 1410.0], 0.55)
        + metallic_partials(n, 0.22, 0.35, [310.0, 640.0, 1020.0], 0.4)
        + metallic_partials(n, 0.52, 0.38, [220.0, 480.0], 0.28)
    )
    mix = wind_down * 0.45 + scrapes * 0.5 + clangs
    return fade(peak_normalize(mix, 0.82), 0.004, 0.14)


def synthesize_shoot(rng: np.random.Generator) -> np.ndarray:
    duration = 0.28
    n = int(duration * SR)
    t = np.arange(n) / SR
    sweep = np.sin(2.0 * math.pi * (1680.0 + 2200.0 * t) * t) * np.exp(-t / 0.09)
    hiss = fft_bandpass(rng.standard_normal(n), 2400.0, 7800.0, order=2) * np.exp(-t / 0.06)
    click = metallic_partials(n, 0.0, 0.04, [2100.0, 3400.0], 0.35)
    mix = sweep * 0.7 + hiss * 0.45 + click
    return fade(peak_normalize(mix, 0.9), 0.001, 0.05)


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
        / "src/client/resources/assets/dwm/sounds/entity/dalek",
    )
    parser.add_argument("--seed", type=int, default=1963)
    args = parser.parse_args()

    out_dir: Path = args.out_dir
    out_dir.mkdir(parents=True, exist_ok=True)

    rng = np.random.default_rng(args.seed)
    jobs = [
        ("ambient", synthesize_ambient(rng, variant=0)),
        ("hurt", synthesize_hurt(rng)),
        ("death", synthesize_death(rng)),
        ("shoot", synthesize_shoot(rng)),
    ]

    with tempfile.TemporaryDirectory(prefix="dalek_sfx_") as tmp:
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
