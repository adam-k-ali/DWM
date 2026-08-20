#!/usr/bin/env python3
"""
Synthesize Flutterwing ambient / hurt / death SFX.

Flutterwings are giant Gallifrey butterflies: slow discrete wingbeats with
air-whoosh + silk-membrane rustle, not a bee-like continuous buzz. Original
synthesis only — does not sample or copy third-party media.

Requires numpy; writes WAV then ffmpeg → OGG (same path as TARDIS travel SFX).
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


def pink_noise(n: int, rng: np.random.Generator) -> np.ndarray:
    white = rng.standard_normal(n)
    spec = np.fft.rfft(white)
    freqs = np.fft.rfftfreq(n)
    freqs[0] = freqs[1]
    spec /= np.sqrt(freqs)
    spec[0] = 0.0
    out = np.fft.irfft(spec, n=n)
    mag = float(np.max(np.abs(out))) or 1.0
    return out / mag


def velvet_noise(n: int, rng: np.random.Generator, density_hz: float) -> np.ndarray:
    """Sparse impulses — closer to fabric rustle than dense white noise."""
    out = np.zeros(n)
    interval = max(1, int(SR / density_hz))
    i = 0
    while i < n:
        i += int(rng.integers(interval // 2, interval * 2))
        if i >= n:
            break
        out[i] = rng.choice((-1.0, 1.0))
    return out


def fft_bandpass(signal: np.ndarray, low: float, high: float, order: int = 4) -> np.ndarray:
    n = len(signal)
    spec = np.fft.rfft(signal)
    freqs = np.maximum(np.fft.rfftfreq(n, 1.0 / SR), 1e-6)
    lp = 1.0 / np.sqrt(1.0 + (freqs / high) ** (2 * order))
    hp = 1.0 / np.sqrt(1.0 + (low / freqs) ** (2 * order))
    out = np.fft.irfft(spec * lp * hp, n=n)
    return out


def hann_burst(n: int) -> np.ndarray:
    if n <= 1:
        return np.ones(n)
    return 0.5 - 0.5 * np.cos(2.0 * math.pi * np.arange(n) / max(n - 1, 1))


def add_burst(env: np.ndarray, t0: float, duration_s: float, amp: float) -> None:
    i0 = int(round(t0 * SR))
    n = int(round(duration_s * SR))
    if n <= 0 or i0 >= len(env):
        return
    if i0 < 0:
        n += i0
        i0 = 0
    n = min(n, len(env) - i0)
    if n <= 0:
        return
    env[i0 : i0 + n] += amp * hann_burst(n)


def flap_envelope(
    n: int,
    flap_times: list[float],
    *,
    down_s: float,
    up_s: float,
    down_amp: float,
    up_amp: float,
) -> np.ndarray:
    env = np.zeros(n)
    for t0 in flap_times:
        add_burst(env, t0, down_s, down_amp)
        add_burst(env, t0 + down_s * 0.85, up_s, up_amp)
    return np.clip(env, 0.0, None)


def swept_partial(
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
    if length <= 1 or i0 >= n:
        return out
    length = min(length, n - max(i0, 0))
    if i0 < 0:
        return out
    t = np.arange(length) / SR
    phase = 2.0 * math.pi * (f0 * t + 0.5 * (f1 - f0) / duration_s * t * t)
    decay = np.exp(-t / max(duration_s * 0.35, 1e-4))
    out[i0 : i0 + length] = np.sin(phase) * hann_burst(length) * decay * amp
    return out


def air_whoosh(n: int, rng: np.random.Generator, env: np.ndarray, low: float, high: float) -> np.ndarray:
    noise = fft_bandpass(pink_noise(n, rng), low, high, order=3)
    # Slight downward air-mass on each flap: mix in a quieter, lower band.
    body = fft_bandpass(pink_noise(n, rng), low * 0.55, low * 1.8, order=2)
    return (noise * 0.78 + body * 0.32) * env


def membrane_rustle(n: int, rng: np.random.Generator, env: np.ndarray, low: float, high: float) -> np.ndarray:
    dense = fft_bandpass(pink_noise(n, rng), low, high, order=3)
    sparse = fft_bandpass(velvet_noise(n, rng, density_hz=420.0), low, high * 1.15, order=2)
    # Rustle is punchier than the whoosh — square the envelope.
    sharp = np.clip(env, 0.0, None) ** 1.6
    return (dense * 0.55 + sparse * 0.9) * sharp


def paper_wing(n: int, rng: np.random.Generator, env: np.ndarray, low: float, high: float) -> np.ndarray:
    """Mid band that reads as cloth/paper rather than a sub whoosh or hiss."""
    return fft_bandpass(pink_noise(n, rng), low, high, order=3) * env


def wing_thup(n: int, flap_times: list[float], f0: float, amp: float) -> np.ndarray:
    """Short decaying air-mass on each downstroke — discrete, not a bee drone."""
    out = np.zeros(n)
    for t0 in flap_times:
        out += swept_partial(n, t0, 0.07, f0, f0 * 0.62, amp)
    return out


def crystal_ticks(n: int, flap_times: list[float], f0: float, amp: float, decay_s: float) -> np.ndarray:
    out = np.zeros(n)
    for i, t0 in enumerate(flap_times):
        # Slightly inharmonic so it is not a musical note.
        freq = f0 * (1.0 + 0.017 * ((i % 3) - 1))
        out += swept_partial(n, t0 + 0.008, decay_s, freq, freq * 0.92, amp)
    return out


def synthesize_ambient(rng: np.random.Generator, *, variant: int = 0) -> np.ndarray:
    duration = 1.28 if variant == 0 else 1.36
    n = int(duration * SR)
    offset = 0.04 if variant == 0 else 0.06
    period = 0.138 if variant == 0 else 0.152
    jitter = 0.012
    flaps = []
    t = offset
    while t < duration - 0.22:
        flaps.append(t + float(rng.uniform(-jitter, jitter)))
        t += period * float(rng.uniform(0.92, 1.08))
    env = flap_envelope(n, flaps, down_s=0.048, up_s=0.042, down_amp=1.0, up_amp=0.38)
    whoosh = air_whoosh(n, rng, env, 70.0, 340.0)
    paper = paper_wing(n, rng, env, 520.0, 2100.0)
    rustle = membrane_rustle(n, rng, env, 1800.0, 6800.0)
    shimmer = fft_bandpass(pink_noise(n, rng), 5200.0, 9800.0, order=2) * (env * 0.14 + 0.03)
    thup = wing_thup(n, flaps, f0=760.0 if variant == 0 else 690.0, amp=0.16)
    crystal = crystal_ticks(n, flaps, f0=2480.0 if variant == 0 else 2710.0, amp=0.028, decay_s=0.08)
    mix = whoosh * 0.58 + paper * 0.48 + rustle * 0.34 + shimmer * 0.09 + thup + crystal
    return fade(peak_normalize(mix, 0.70), 0.012, 0.10)


def synthesize_hurt(rng: np.random.Generator) -> np.ndarray:
    duration = 0.40
    n = int(duration * SR)
    flaps = [0.012, 0.095]
    env = flap_envelope(n, flaps, down_s=0.032, up_s=0.028, down_amp=1.0, up_amp=0.45)
    whoosh = air_whoosh(n, rng, env, 120.0, 480.0)
    paper = paper_wing(n, rng, env, 700.0, 2800.0)
    rustle = membrane_rustle(n, rng, env, 2200.0, 8600.0)
    snap = fft_bandpass(rng.standard_normal(n), 700.0, 7500.0, order=2)
    snap_env = np.zeros(n)
    add_burst(snap_env, 0.0, 0.022, 1.0)
    thup = wing_thup(n, flaps, f0=980.0, amp=0.22)
    crystal = crystal_ticks(n, flaps, f0=3180.0, amp=0.055, decay_s=0.05)
    mix = whoosh * 0.42 + paper * 0.46 + rustle * 0.42 + snap * snap_env * 0.48 + thup + crystal
    return fade(peak_normalize(mix, 0.88), 0.002, 0.07)


def synthesize_death(rng: np.random.Generator) -> np.ndarray:
    duration = 1.12
    n = int(duration * SR)
    flaps = [0.04, 0.20, 0.44, 0.78]
    amps = [1.0, 0.78, 0.48, 0.22]
    env = np.zeros(n)
    for t0, amp in zip(flaps, amps):
        stretch = 1.0 + t0 * 0.55
        add_burst(env, t0, 0.055 * stretch, amp)
        add_burst(env, t0 + 0.048 * stretch, 0.050 * stretch, amp * 0.32)
    whoosh = air_whoosh(n, rng, env, 55.0, 280.0)
    paper = paper_wing(n, rng, env, 400.0, 1600.0)
    rustle = membrane_rustle(n, rng, env, 1200.0, 4800.0)
    # Closing wings: progressively darker.
    t = np.arange(n) / SR
    darken = np.exp(-t * 1.35)
    whoosh *= 0.45 + 0.55 * darken
    paper *= 0.55 + 0.45 * darken
    rustle *= darken
    crumple = fft_bandpass(pink_noise(n, rng), 180.0, 1400.0, order=2)
    crumple_env = np.zeros(n)
    add_burst(crumple_env, 0.72, 0.32, 0.85)
    falling = swept_partial(n, 0.06, 0.95, 1980.0, 720.0, 0.045)
    thup = wing_thup(n, flaps[:2], f0=640.0, amp=0.12)
    mix = whoosh * 0.58 + paper * 0.42 + rustle * 0.28 + crumple * crumple_env * 0.40 + falling + thup
    return fade(peak_normalize(mix, 0.80), 0.008, 0.16)


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
        / "src/client/resources/assets/dwm/sounds/entity/flutterwing",
    )
    parser.add_argument("--seed", type=int, default=1963)
    args = parser.parse_args()

    out_dir: Path = args.out_dir
    out_dir.mkdir(parents=True, exist_ok=True)

    rng = np.random.default_rng(args.seed)
    jobs = [
        ("ambient", synthesize_ambient(rng, variant=0)),
        ("ambient_2", synthesize_ambient(rng, variant=1)),
        ("hurt", synthesize_hurt(rng)),
        ("death", synthesize_death(rng)),
    ]

    with tempfile.TemporaryDirectory(prefix="flutterwing_sfx_") as tmp:
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
