#!/usr/bin/env python3
"""
Synthesize Dalek voice lines and SFX: exterminate / ambient / hurt / death / shoot.

The speech is original: plain text-to-speech from CMU Flite (BSD-style licence, free for
commercial use; see dwm/docs/feature-dalek.md) flattened to a monotone and run through a
Dalek effects chain (band-limit, ~30 Hz ring modulation, soft clipping, metallic comb).
No BBC audio is sampled or imitated clip-for-clip — only the generic "ring-modulated robot"
effect. The laser zap and the casing clangs under the death line are numpy synthesis.

Requires numpy, the `flite` binary on PATH (`brew install flite`; dev-time only, the OGGs are
committed) and ffmpeg for WAV -> OGG (same path as Flutterwing SFX).
"""

from __future__ import annotations

import argparse
import math
import shutil
import subprocess
import sys
import tempfile
import wave
from dataclasses import dataclass
from pathlib import Path

import numpy as np

from dwm_sfx.paths import find_dwm_root

SR = 44100

DEFAULT_VOICE = "kal16"
# Mean pitch (Hz) of the flattened monotone per Flite voice; deliberately low-ish so the
# ring modulator, not the pitch, supplies the "robot" character.
VOICE_F0_HZ = {"kal16": 100.0, "kal": 100.0, "rms": 95.0, "awb": 105.0, "slt": 150.0}

RING_MOD_HZ = 30.0
VOICE_BAND_HZ = (250.0, 4500.0)


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


# --------------------------------------------------------------------------- speech


def resample(signal: np.ndarray, src_rate: int, dst_rate: int = SR) -> np.ndarray:
    """Linear-interpolation resample; the Dalek band-pass removes the imaging above 4.5 kHz."""
    if src_rate == dst_rate:
        return signal.astype(np.float64)
    n_out = int(round(len(signal) * dst_rate / src_rate))
    positions = np.arange(n_out) * (src_rate / dst_rate)
    return np.interp(positions, np.arange(len(signal)), signal)


def read_wav_mono(path: Path) -> tuple[np.ndarray, int]:
    with wave.open(str(path), "rb") as wf:
        if wf.getsampwidth() != 2:
            raise ValueError(f"expected 16-bit PCM in {path}, got {wf.getsampwidth() * 8}-bit")
        channels = wf.getnchannels()
        rate = wf.getframerate()
        raw = np.frombuffer(wf.readframes(wf.getnframes()), dtype="<i2").astype(np.float64) / 32768.0
    if channels > 1:
        raw = raw.reshape(-1, channels).mean(axis=1)
    return raw, rate


def speak(text: str, voice: str = DEFAULT_VOICE, stretch: float = 1.0, f0_hz: float | None = None) -> np.ndarray:
    """Render `text` with Flite as a flat-pitch (monotone) 44.1 kHz mono signal."""
    if shutil.which("flite") is None:
        raise RuntimeError("flite not found on PATH — install it (macOS: `brew install flite`) to regenerate Dalek voice clips")
    mean_hz = f0_hz if f0_hz is not None else VOICE_F0_HZ.get(voice, 100.0)
    with tempfile.TemporaryDirectory(prefix="dalek_flite_") as tmp:
        wav_path = Path(tmp) / "speech.wav"
        subprocess.run(
            [
                "flite",
                "-voice", voice,
                "--setf", f"int_f0_target_mean={mean_hz:g}",
                "--setf", "int_f0_target_stddev=0",
                "--setf", f"duration_stretch={stretch:g}",
                "-t", text,
                "-o", str(wav_path),
            ],
            check=True,
            capture_output=True,
        )
        samples, rate = read_wav_mono(wav_path)
    return resample(samples, rate)


def trim_silence(signal: np.ndarray, threshold: float = 0.02, pad_s: float = 0.015) -> np.ndarray:
    """Cut leading/trailing samples quieter than `threshold` of the peak, keeping a short pad."""
    mag = np.abs(signal)
    peak = float(mag.max()) if len(mag) else 0.0
    if peak <= 0.0:
        return signal
    loud = np.flatnonzero(mag >= threshold * peak)
    pad = int(pad_s * SR)
    return signal[max(int(loud[0]) - pad, 0) : int(loud[-1]) + pad + 1]


def dalek_voice_fx(
    signal: np.ndarray,
    *,
    ring_hz: float = RING_MOD_HZ,
    ring_wet: float = 0.7,
    drive: float = 2.2,
    peak: float = 0.86,
) -> np.ndarray:
    """Turn a flat TTS signal into a Dalek voice: band-limit, ring-mod, soft-clip, metallic comb."""
    x = trim_silence(signal)
    if len(x) < 8:
        return x
    x = fft_bandpass(x, *VOICE_BAND_HZ, order=3)
    t = np.arange(len(x)) / SR
    carrier = np.sin(2.0 * math.pi * ring_hz * t)
    x = x * (1.0 - ring_wet) + x * carrier * ring_wet
    x = peak_normalize(x, 1.0)
    x = np.tanh(x * drive) / math.tanh(drive)
    for delay_s, gain in ((0.0042, 0.38), (0.0091, 0.2)):
        d = int(delay_s * SR)
        echo = np.zeros_like(x)
        echo[d:] = x[:-d] * gain
        x = x + echo
    return fade(peak_normalize(x, peak), 0.004, 0.05)


def servo_hum(n: int, freq: float, level: float) -> np.ndarray:
    t = np.arange(n) / SR
    hum = np.sin(2.0 * math.pi * freq * t) * (0.6 + 0.4 * np.sin(2.0 * math.pi * 3.2 * t))
    return hum * level


def pad_to(signal: np.ndarray, n: int) -> np.ndarray:
    out = np.zeros(n)
    m = min(n, len(signal))
    out[:m] = signal[:m]
    return out


def cap_length(signal: np.ndarray, max_s: float) -> np.ndarray:
    return fade(signal[: int(max_s * SR)], 0.0, 0.04) if len(signal) > int(max_s * SR) else signal


def pitch_wind_down(signal: np.ndarray, end_rate: float) -> np.ndarray:
    """Slow playback from 1.0x to `end_rate`x over the clip (pitch and tempo fall together)."""
    n = len(signal)
    rate = np.linspace(1.0, end_rate, n)
    positions = np.cumsum(rate) - rate[0]
    positions = positions[positions <= n - 1]
    return np.interp(positions, np.arange(n), signal)


# --------------------------------------------------------------------------- clips


@dataclass(frozen=True)
class Line:
    text: str
    stretch: float = 1.0
    f0_hz: float | None = None


EXTERMINATE_LINES = [
    Line("Exterminate!", 1.35),
    Line("Exterminate!", 1.7),
    Line("Ex ter min ate!", 1.15),
]
AMBIENT_LINES = [
    Line("Seek, locate, destroy.", 1.0),
    Line("Daleks are supreme.", 1.05),
    Line("Obey.", 1.5),
    Line("You will be exterminated.", 1.0),
]
HURT_LINES = [
    Line("Emergency!", 0.85),
    Line("Damage!", 1.0),
    Line("Emergency, emergency!", 0.75),
]
DEATH_LINES = [
    Line("My vision is impaired!", 1.0),
    Line("I cannot see!", 1.3),
]

# Hurt fires on every hit, so it must stay short; ambient/exterminate may breathe.
HURT_MAX_S = 0.8


def voiced_clip(line: Line, voice: str) -> np.ndarray:
    return dalek_voice_fx(speak(line.text, voice, line.stretch, line.f0_hz))


def synthesize_exterminate(voice: str, take: int) -> np.ndarray:
    voiced = voiced_clip(EXTERMINATE_LINES[take], voice)
    return peak_normalize(voiced, 0.9)


def synthesize_ambient(voice: str, take: int, rng: np.random.Generator) -> np.ndarray:
    voiced = voiced_clip(AMBIENT_LINES[take], voice)
    n = len(voiced) + int(0.12 * SR)
    hum = servo_hum(n, 42.0 if take % 2 == 0 else 36.0, 0.05)
    grate = fft_bandpass(rng.standard_normal(n), 180.0, 1400.0, order=3) * 0.012
    mix = pad_to(voiced, n) + hum + grate
    return fade(peak_normalize(mix, 0.78), 0.01, 0.1)


def synthesize_hurt(voice: str, take: int, rng: np.random.Generator) -> np.ndarray:
    voiced = cap_length(voiced_clip(HURT_LINES[take], voice), HURT_MAX_S)
    n = len(voiced)
    impact = fft_bandpass(rng.standard_normal(n), 200.0, 4200.0, order=2)
    spark = impact * np.exp(-np.arange(n) / SR / 0.04) * 0.25
    clang = metallic_partials(n, 0.0, 0.18, [510.0, 980.0, 1640.0], 0.22)
    return fade(peak_normalize(voiced + spark + clang, 0.88), 0.001, 0.05)


def synthesize_death(voice: str, take: int, rng: np.random.Generator) -> np.ndarray:
    voiced = pitch_wind_down(voiced_clip(DEATH_LINES[take], voice), 0.55)
    n = len(voiced) + int(0.45 * SR)
    t = np.arange(n) / SR
    scrapes = fft_bandpass(rng.standard_normal(n), 90.0, 900.0, order=2) * np.exp(-t * 1.8) * 0.5
    wind_down = np.sin(2.0 * math.pi * (90.0 - 55.0 * np.minimum(t, 1.5)) * t) * np.exp(-t * 2.4) * 0.3
    tail = len(voiced) / SR
    clangs = (
        metallic_partials(n, tail * 0.6, 0.3, [430.0, 870.0, 1410.0], 0.35)
        + metallic_partials(n, tail * 0.85, 0.38, [310.0, 640.0, 1020.0], 0.3)
        + metallic_partials(n, tail + 0.05, 0.4, [220.0, 480.0], 0.25)
    )
    mix = pad_to(voiced, n) + scrapes + wind_down + clangs
    return fade(peak_normalize(mix, 0.85), 0.004, 0.16)


def synthesize_shoot(rng: np.random.Generator) -> np.ndarray:
    """Short laser zap — fires every ~2 s in a fight, so it stays wordless."""
    duration = 0.28
    n = int(duration * SR)
    t = np.arange(n) / SR
    sweep = np.sin(2.0 * math.pi * (1680.0 + 2200.0 * t) * t) * np.exp(-t / 0.09)
    hiss = fft_bandpass(rng.standard_normal(n), 2400.0, 7800.0, order=2) * np.exp(-t / 0.06)
    click = metallic_partials(n, 0.0, 0.04, [2100.0, 3400.0], 0.35)
    thump = np.sin(2.0 * math.pi * 120.0 * t) * np.exp(-t / 0.03) * 0.4
    mix = sweep * 0.7 + hiss * 0.45 + click + thump
    return fade(peak_normalize(mix, 0.9), 0.001, 0.05)


def spectral_centroid(signal: np.ndarray) -> float:
    spec = np.abs(np.fft.rfft(signal))
    freqs = np.fft.rfftfreq(len(signal), 1.0 / SR)
    mag = float(np.sum(spec)) or 1.0
    return float(np.sum(freqs * spec) / mag)


def clip_name(base: str, take: int) -> str:
    """`ambient`, `ambient_2`, `ambient_3`... (matches the Flutterwing multi-clip naming)."""
    return base if take == 0 else f"{base}_{take + 1}"


def build_clips(voice: str, seed: int) -> list[tuple[str, np.ndarray]]:
    rng = np.random.default_rng(seed)
    jobs: list[tuple[str, np.ndarray]] = []
    for take in range(len(EXTERMINATE_LINES)):
        jobs.append((clip_name("exterminate", take), synthesize_exterminate(voice, take)))
    for take in range(len(AMBIENT_LINES)):
        jobs.append((clip_name("ambient", take), synthesize_ambient(voice, take, rng)))
    for take in range(len(HURT_LINES)):
        jobs.append((clip_name("hurt", take), synthesize_hurt(voice, take, rng)))
    for take in range(len(DEATH_LINES)):
        jobs.append((clip_name("death", take), synthesize_death(voice, take, rng)))
    jobs.append(("shoot", synthesize_shoot(rng)))
    return jobs


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--out-dir",
        type=Path,
        default=find_dwm_root()
        / "src/client/resources/assets/dwm/sounds/entity/dalek",
    )
    parser.add_argument("--seed", type=int, default=1963)
    parser.add_argument(
        "--voice",
        default=DEFAULT_VOICE,
        help="Flite voice (run `flite -lv`); default %(default)s",
    )
    parser.add_argument(
        "--wav",
        action="store_true",
        help="write .wav instead of .ogg (for auditioning with afplay)",
    )
    args = parser.parse_args()

    out_dir: Path = args.out_dir
    out_dir.mkdir(parents=True, exist_ok=True)

    try:
        jobs = build_clips(args.voice, args.seed)
    except RuntimeError as err:
        print(err, file=sys.stderr)
        return 1

    with tempfile.TemporaryDirectory(prefix="dalek_sfx_") as tmp:
        tmp_dir = Path(tmp)
        for name, samples in jobs:
            if args.wav:
                out_path = out_dir / f"{name}.wav"
                write_wav(out_path, samples)
            else:
                wav_path = tmp_dir / f"{name}.wav"
                out_path = out_dir / f"{name}.ogg"
                write_wav(wav_path, samples)
                wav_to_ogg(wav_path, out_path)
            centroid = spectral_centroid(samples)
            print(
                f"Wrote {out_path} ({len(samples) / SR:.2f}s) "
                f"centroid={centroid:.0f}Hz peak={float(np.max(np.abs(samples))):.2f}"
            )
    return 0


if __name__ == "__main__":
    sys.exit(main())
