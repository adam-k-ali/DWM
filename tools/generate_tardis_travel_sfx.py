#!/usr/bin/env python3
"""
Synthesize seamless TARDIS dematerialise/materialise loop SFX and a landing thud.

Recreates the documented Radiophonic Workshop technique in spirit:
key-scrape on bass strings + tape echo/feedback + rising whoosh;
materialise is the reverse spectral motion; thud is a short landing bang.

Does not sample or copy BBC media. Requires numpy; writes WAV then ffmpeg → OGG.
"""

from __future__ import annotations

import argparse
import math
import subprocess
import sys
import wave
from pathlib import Path

import numpy as np

SR = 44100
LOOP_SECONDS = 2.0
THUD_SECONDS = 0.45


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
    # Prefer libvorbis; fall back to native vorbis (stereo-only on some builds).
    attempts = [
        ["ffmpeg", "-y", "-i", str(wav_path), "-c:a", "libvorbis", "-q:a", "6", str(ogg_path)],
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


def soft_clip(x: np.ndarray, drive: float = 1.4) -> np.ndarray:
    return np.tanh(x * drive) / math.tanh(drive)


def apply_feedback_delay(
    signal: np.ndarray,
    delay_samples: int,
    feedback: float,
    mix: float,
    passes: int = 4,
) -> np.ndarray:
    out = signal.astype(np.float64).copy()
    for _ in range(passes):
        delayed = np.zeros_like(out)
        if delay_samples < len(out):
            delayed[delay_samples:] = out[:-delay_samples]
        out = out + delayed * feedback
        # mild one-pole lowpass on the recirculating tail
        for i in range(1, len(out)):
            out[i] = out[i] * 0.65 + out[i - 1] * 0.35
    return (1.0 - mix) * signal + mix * out


def scrape_burst(
    n: int,
    rng: np.random.Generator,
    base_hz: float,
    sweep_octaves: float,
    brightness: float,
) -> np.ndarray:
    """Metallic key-on-string scrape: filtered noise with descending resonant sweep."""
    t = np.arange(n) / SR
    # descending scrape pitch (string scrape motion)
    start = base_hz * (2.0 ** (sweep_octaves * 0.5))
    end = base_hz * (2.0 ** (-sweep_octaves * 0.5))
    freqs = np.geomspace(start, end, n)
    phase = 2.0 * np.pi * np.cumsum(freqs) / SR
    carrier = np.sin(phase)

    noise = rng.standard_normal(n)
    # cheap resonant-ish coloration: band emphasis via differencing
    bright = np.diff(noise, prepend=noise[0])
    scrape = carrier * 0.55 + bright * brightness
    # percussive scrape envelope (attack + long grind)
    env = np.ones(n)
    attack = max(1, int(0.02 * SR))
    release = max(1, int(0.25 * SR))
    env[:attack] = np.linspace(0.0, 1.0, attack)
    env[-release:] *= np.linspace(1.0, 0.15, release)
    # amplitude modulation for "vworp" throb
    throb = 0.55 + 0.45 * np.sin(2.0 * np.pi * 1.15 * t + 0.3)
    return scrape * env * throb


def rising_whoosh(n: int) -> np.ndarray:
    t = np.arange(n) / SR
    # rising sine with feedback-like shimmer
    f0, f1 = 90.0, 420.0
    freqs = np.geomspace(f0, f1, n)
    phase = 2.0 * np.pi * np.cumsum(freqs) / SR
    tone = np.sin(phase) * 0.35
    shimmer = np.sin(phase * 1.97) * 0.12
    env = np.linspace(0.25, 1.0, n) ** 1.4
    return (tone + shimmer) * env


def make_seamless(signal: np.ndarray, crossfade: int) -> np.ndarray:
    """Crossfade ends so the clip loops without a click."""
    if crossfade <= 0 or crossfade * 2 >= len(signal):
        return signal
    out = signal.copy()
    fade_out = np.linspace(1.0, 0.0, crossfade)
    fade_in = 1.0 - fade_out
    head = out[:crossfade].copy()
    tail = out[-crossfade:].copy()
    out[-crossfade:] = tail * fade_out + head * fade_in
    out[:crossfade] = out[-crossfade:]
    # force endpoints to near-zero for Minecraft loop safety
    out[0] = 0.0
    out[-1] = 0.0
    return out


def synthesize_demat_loop(rng: np.random.Generator) -> np.ndarray:
    n = int(LOOP_SECONDS * SR)
    layers = np.zeros(n, dtype=np.float64)

    # staggered scrape bursts across the loop (classic multi-cut collage feel)
    bursts = [
        (0.00, 55.0, 1.8, 0.55),
        (0.45, 48.0, 2.1, 0.48),
        (0.90, 62.0, 1.6, 0.52),
        (1.35, 44.0, 2.0, 0.45),
    ]
    for start_s, base_hz, octaves, bright in bursts:
        start = int(start_s * SR)
        length = int(0.85 * SR)
        end = min(n, start + length)
        burst = scrape_burst(end - start, rng, base_hz, octaves, bright)
        layers[start:end] += burst

    # slower / deeper layer (tape varispeed)
    deep = scrape_burst(n, rng, 32.0, 1.2, 0.3) * 0.55
    layers += deep

    # rising whoosh demanded by the classic take-off character
    layers += rising_whoosh(n) * 0.9

    # tape echo / feedback trail
    layers = apply_feedback_delay(layers, delay_samples=int(0.18 * SR), feedback=0.62, mix=0.55, passes=5)
    layers = apply_feedback_delay(layers, delay_samples=int(0.09 * SR), feedback=0.35, mix=0.3, passes=3)

    layers = soft_clip(layers, drive=1.6)
    peak = np.max(np.abs(layers)) or 1.0
    layers = layers / peak * 0.85
    return make_seamless(layers, crossfade=int(0.08 * SR))


def synthesize_mat_loop(demat: np.ndarray) -> np.ndarray:
    # Materialisation: reverse of demat character with light re-feedback
    rev = demat[::-1].copy()
    rev = apply_feedback_delay(rev, delay_samples=int(0.14 * SR), feedback=0.45, mix=0.4, passes=3)
    rev = soft_clip(rev, drive=1.5)
    peak = np.max(np.abs(rev)) or 1.0
    rev = rev / peak * 0.85
    return make_seamless(rev, crossfade=int(0.08 * SR))


def synthesize_thud(rng: np.random.Generator) -> np.ndarray:
    n = int(THUD_SECONDS * SR)
    t = np.arange(n) / SR
    # low impact + short metallic rattle
    body = np.sin(2.0 * np.pi * 55.0 * t) * np.exp(-t * 14.0)
    body += 0.45 * np.sin(2.0 * np.pi * 90.0 * t) * np.exp(-t * 18.0)
    noise = rng.standard_normal(n) * np.exp(-t * 28.0) * 0.35
    click = np.zeros(n)
    click[: int(0.004 * SR)] = rng.uniform(-1.0, 1.0, int(0.004 * SR))
    thud = body + noise + click * 0.5
    thud = soft_clip(thud, drive=2.0)
    peak = np.max(np.abs(thud)) or 1.0
    return thud / peak * 0.95


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--out-dir",
        type=Path,
        default=Path(__file__).resolve().parents[1]
        / "src/client/resources/assets/dwm/sounds",
    )
    parser.add_argument("--seed", type=int, default=1963)
    args = parser.parse_args()

    out_dir: Path = args.out_dir
    out_dir.mkdir(parents=True, exist_ok=True)
    tmp = out_dir / ".tmp_sfx"
    tmp.mkdir(exist_ok=True)

    rng = np.random.default_rng(args.seed)
    demat = synthesize_demat_loop(rng)
    mat = synthesize_mat_loop(demat)
    thud = synthesize_thud(rng)

    jobs = [
        ("tardis_dematerialise_loop", demat),
        ("tardis_materialise_loop", mat),
        ("tardis_materialise_thud", thud),
    ]
    for name, samples in jobs:
        wav_path = tmp / f"{name}.wav"
        ogg_path = out_dir / f"{name}.ogg"
        write_wav(wav_path, samples)
        wav_to_ogg(wav_path, ogg_path)
        print(f"Wrote {ogg_path} ({len(samples) / SR:.2f}s)")

    for wav in tmp.glob("*.wav"):
        wav.unlink()
    tmp.rmdir()
    return 0


if __name__ == "__main__":
    sys.exit(main())
