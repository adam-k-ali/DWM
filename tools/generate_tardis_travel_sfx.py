#!/usr/bin/env python3
"""
Synthesize seamless TARDIS dematerialise/materialise loop SFX and a landing thud.

Recreates the Radiophonic Workshop technique in spirit (Hodgson): key scrape on
bass piano strings → tape varispeed → feedback/echo; materialise is the reverse
motion; thud is a short landing bang.

Metallic texture is modal/inharmonic + FM + tone×tone ring-mod (not broadband
noise). Reference show SFX is peaky around ~95–200 Hz with ~0% energy above 5 kHz.

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
# One full vworp ≈ 1.75s (centroid autocorr on reference); 2 per seamless loop.
PULSE_SECONDS = 1.75
LOOP_SECONDS = PULSE_SECONDS * 2.0  # 3.5s
THUD_SECONDS = 0.40

# Piano-string-like stiffness (inharmonic partial stretch) — metallic, not harmonic siren.
STRING_B = 0.00045
# Hard ceiling matching reference (hf>5k ≈ 0.05% there).
HF_CUTOFF_HZ = 3600.0


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


def one_pole_lowpass(signal: np.ndarray, coeff: float) -> np.ndarray:
    out = np.empty_like(signal)
    out[0] = signal[0]
    keep = 1.0 - coeff
    for i in range(1, len(signal)):
        out[i] = signal[i] * keep + out[i - 1] * coeff
    return out


def brickwall_lowpass(signal: np.ndarray, cutoff_hz: float) -> np.ndarray:
    """FFT low-pass — kills hiss above the reference's usable band."""
    spec = np.fft.rfft(signal)
    freqs = np.fft.rfftfreq(len(signal), 1.0 / SR)
    # raised-cosine transition ~200 Hz wide
    lo = cutoff_hz - 150.0
    hi = cutoff_hz + 50.0
    gain = np.ones_like(freqs)
    mid = (freqs > lo) & (freqs < hi)
    gain[mid] = 0.5 * (1.0 + np.cos(np.pi * (freqs[mid] - lo) / (hi - lo)))
    gain[freqs >= hi] = 0.0
    return np.fft.irfft(spec * gain, n=len(signal))


def apply_feedback_delay(
    signal: np.ndarray,
    delay_samples: int,
    feedback: float,
    mix: float,
    passes: int = 4,
    lp_coeff: float = 0.78,
) -> np.ndarray:
    """Tape-echo style feedback; recirculating path is darkened (no HF rebuild)."""
    dry = signal.astype(np.float64)
    wet = dry.copy()
    for _ in range(passes):
        delayed = np.zeros_like(wet)
        if delay_samples < len(wet):
            delayed[delay_samples:] = wet[:-delay_samples]
        delayed = one_pole_lowpass(delayed, lp_coeff)
        wet = wet + delayed * feedback
    return (1.0 - mix) * dry + mix * wet


def partial_freq(f0: np.ndarray, n: int, b: float = STRING_B) -> np.ndarray:
    """Stretched partial: f_n = n*f0*sqrt(1 + B*n^2) — metallic string inharmonicity."""
    return n * f0 * np.sqrt(1.0 + b * (n * n))


def modal_string_scrape(f0: np.ndarray, amps: list[float]) -> np.ndarray:
    """Additive inharmonic partials tracking a swept fundamental (key-on-string body)."""
    out = np.zeros_like(f0)
    for n, amp in enumerate(amps, start=1):
        freqs = partial_freq(f0, n)
        phase = 2.0 * np.pi * np.cumsum(freqs) / SR
        # slight per-partial phase offset avoids perfect constructive spikes
        out += np.sin(phase + 0.15 * n) * amp
    return out


def fm_metallic(f0: np.ndarray, index: float, ratio: float = math.sqrt(2.0)) -> np.ndarray:
    """FM with irrational C:M ratio → dense inharmonic sidebands (bell/metal, not hiss)."""
    fc = f0
    fm = f0 * ratio
    phase_m = 2.0 * np.pi * np.cumsum(fm) / SR
    phase_c = 2.0 * np.pi * np.cumsum(fc) / SR + index * np.sin(phase_m)
    return np.sin(phase_c)


def ring_mod_tones(f_a: np.ndarray, f_b: np.ndarray) -> np.ndarray:
    """Tone×tone ring mod — classic metallic sidebands without a noise bed."""
    pa = 2.0 * np.pi * np.cumsum(f_a) / SR
    pb = 2.0 * np.pi * np.cumsum(f_b) / SR
    return np.sin(pa) * np.sin(pb)


def scrape_chatter_am(n: int, rng: np.random.Generator) -> np.ndarray:
    """Sparse friction AM from low-rate irregular pulses — not continuous noise audio."""
    # control-rate irregularity (~30–80 Hz), heavily smoothed → amplitude only
    raw = rng.standard_normal(n)
    env = one_pole_lowpass(np.abs(raw), 0.9985)
    env = one_pole_lowpass(env, 0.997)
    env = env / (np.max(env) or 1.0)
    return 0.72 + 0.28 * env


def vworp_pulse(
    n: int,
    rng: np.random.Generator,
    *,
    start_hz: float,
    end_hz: float,
) -> np.ndarray:
    """One ~1.75s vworp: metallic modal scrape + FM/ring-mod, no hiss bed."""
    f0 = np.geomspace(start_hz, end_hz, n)

    # Partial stack: strong lows (ref ~40% in 80–200) + enough mid for centroid ~500
    amps = [0.58, 0.50, 0.38, 0.26, 0.18, 0.12, 0.07, 0.04]
    scrape = modal_string_scrape(f0, amps)

    # Metallic density without noise: FM + ring-mod sidebands
    scrape += fm_metallic(f0, index=2.8, ratio=math.sqrt(2.0)) * 0.36
    scrape += fm_metallic(f0 * 0.5, index=2.0, ratio=math.sqrt(3.0)) * 0.18
    scrape += ring_mod_tones(partial_freq(f0, 1), partial_freq(f0, 2)) * 0.22
    scrape += ring_mod_tones(partial_freq(f0, 2), partial_freq(f0, 3)) * 0.12
    scrape += ring_mod_tones(partial_freq(f0, 1), partial_freq(f0, 4)) * 0.08

    # Friction as AM only (control-smoothed) — never mixed as an audio noise layer
    scrape *= scrape_chatter_am(n, rng)

    scrape = soft_clip(scrape, drive=1.9)

    env = np.ones(n)
    attack = max(1, int(0.08 * SR))
    release = max(1, int(0.45 * SR))
    if attack + release >= n:
        release = max(1, n // 4)
        attack = max(1, min(attack, n - release - 1))
    env[:attack] = np.linspace(0.0, 1.0, attack) ** 0.7
    env[-release:] *= np.linspace(1.0, 0.08, release) ** 1.4
    body = 0.85 + 0.15 * np.sin(np.pi * np.linspace(0.0, 1.0, n))
    suck = 0.82 + 0.18 * np.linspace(1.0, 0.50, n)
    out = scrape * env * body * suck
    peak = np.max(np.abs(out)) or 1.0
    return out / peak


def rising_whoosh_modal(n: int, rising: bool) -> np.ndarray:
    """Take-off whoosh as a soft modal swell — still tonal/metallic, not noise air."""
    if rising:
        f0 = np.concatenate(
            [np.geomspace(110.0, 260.0, n // 2), np.geomspace(110.0, 260.0, n - n // 2)]
        )
    else:
        f0 = np.concatenate(
            [np.geomspace(260.0, 110.0, n // 2), np.geomspace(260.0, 110.0, n - n // 2)]
        )
    whoosh = modal_string_scrape(f0, [0.45, 0.28, 0.14, 0.06])
    whoosh += fm_metallic(f0, index=1.0, ratio=math.sqrt(2.0)) * 0.10
    half = n // 2
    env = np.zeros(n)
    env[:half] = np.sin(np.pi * np.linspace(0.0, 1.0, half))
    env[half:] = np.sin(np.pi * np.linspace(0.0, 1.0, n - half))
    return whoosh * (0.20 + 0.80 * env)


def make_seamless(signal: np.ndarray, crossfade: int) -> np.ndarray:
    if crossfade <= 0 or crossfade * 2 >= len(signal):
        return signal
    out = signal.copy()
    fade_out = np.linspace(1.0, 0.0, crossfade)
    fade_in = 1.0 - fade_out
    head = out[:crossfade].copy()
    tail = out[-crossfade:].copy()
    out[-crossfade:] = tail * fade_out + head * fade_in
    out[:crossfade] = out[-crossfade:]
    return out


def synthesize_travel_loop(
    rng: np.random.Generator,
    *,
    descending: bool,
) -> np.ndarray:
    n = int(LOOP_SECONDS * SR)
    layers = np.zeros(n, dtype=np.float64)
    pulse_n = int(PULSE_SECONDS * SR)

    # Fundamentals around the reference peak cluster (~95–200 Hz)
    if descending:
        pulse_specs = [(200.0, 95.0), (185.0, 90.0)]
    else:
        pulse_specs = [(95.0, 200.0), (90.0, 185.0)]

    for i, (start_hz, end_hz) in enumerate(pulse_specs):
        start = i * pulse_n
        end = min(n, start + pulse_n)
        layers[start:end] += vworp_pulse(end - start, rng, start_hz=start_hz, end_hz=end_hz)

    t = np.arange(n) / SR
    bed_f0 = np.full(n, 98.0)
    bed = modal_string_scrape(bed_f0, [0.50, 0.32, 0.14, 0.06]) * 0.08
    gate = 0.30 + 0.70 * (0.5 + 0.5 * np.sin(2.0 * np.pi * (1.0 / PULSE_SECONDS) * t - 0.3)) ** 1.5
    layers += bed * gate

    layers += rising_whoosh_modal(n, rising=descending) * 0.30

    layers = apply_feedback_delay(
        layers, delay_samples=int(0.85 * SR), feedback=0.45, mix=0.38, passes=4, lp_coeff=0.80
    )
    layers = apply_feedback_delay(
        layers, delay_samples=int(0.42 * SR), feedback=0.26, mix=0.16, passes=2, lp_coeff=0.78
    )

    pulse_phase = (t / PULSE_SECONDS) * 2.0 * np.pi
    if not descending:
        pulse_phase = pulse_phase + np.pi
    pulse_gate = 0.40 + 0.60 * np.clip(np.cos(pulse_phase) * 0.5 + 0.5, 0.0, 1.0) ** 1.1
    layers *= pulse_gate

    layers = soft_clip(layers, drive=1.2)
    layers = brickwall_lowpass(layers, HF_CUTOFF_HZ)
    peak = np.max(np.abs(layers)) or 1.0
    layers = layers / peak * 0.85
    return make_seamless(layers, crossfade=int(0.06 * SR))


def synthesize_demat_loop(rng: np.random.Generator) -> np.ndarray:
    return synthesize_travel_loop(rng, descending=True)


def synthesize_mat_loop(rng: np.random.Generator) -> np.ndarray:
    return synthesize_travel_loop(rng, descending=False)


def synthesize_thud(rng: np.random.Generator) -> np.ndarray:
    """Short landing bang — mid modal thump, no hiss bed."""
    n = int(THUD_SECONDS * SR)
    t = np.arange(n) / SR
    f0 = np.full(n, 95.0)
    body = modal_string_scrape(f0, [0.7, 0.45, 0.22, 0.1]) * np.exp(-t * 14.0)
    body += fm_metallic(f0, index=1.8, ratio=math.sqrt(2.0)) * 0.2 * np.exp(-t * 22.0)
    attack_n = max(1, int(0.008 * SR))
    click = np.zeros(n)
    # tiny deterministic transient (not ongoing noise)
    click[:attack_n] = np.linspace(1.0, 0.0, attack_n) * rng.choice([-1.0, 1.0])
    thud = soft_clip(body + click * 0.25, drive=1.8)
    thud = brickwall_lowpass(thud, HF_CUTOFF_HZ)
    peak = np.max(np.abs(thud)) or 1.0
    return thud / peak * 0.95


def spectral_report(samples: np.ndarray, sample_rate: int = SR) -> dict[str, float]:
    spec = np.abs(np.fft.rfft(samples * np.hanning(len(samples)))) ** 2
    freqs = np.fft.rfftfreq(len(samples), 1.0 / sample_rate)
    tot = float(spec.sum()) or 1.0
    centroid = float((freqs * spec).sum() / tot)
    return {
        "centroid_hz": centroid,
        "hf_gt_3k": float(spec[freqs >= 3000].sum() / tot),
        "hf_gt_5k": float(spec[freqs >= 5000].sum() / tot),
        "band_80_200": float(spec[(freqs >= 80) & (freqs < 200)].sum() / tot),
        "band_500_1500": float(spec[(freqs >= 500) & (freqs < 1500)].sum() / tot),
        "duration_s": len(samples) / sample_rate,
    }


def validate_against_reference(ours: np.ndarray, ref: np.ndarray, ref_sr: int) -> list[str]:
    """Compare ours to reference traits; return list of failure messages (empty = ok)."""
    # Use a mid slice of the long reference take
    if len(ref) / ref_sr > 6:
        ref = ref[int(2 * ref_sr) : int(8 * ref_sr)]
    if ref_sr != SR:
        # crude resample via FFT length match for spectra only
        target_n = int(len(ref) * SR / ref_sr)
        ref = np.interp(np.linspace(0, len(ref) - 1, target_n), np.arange(len(ref)), ref)

    r = spectral_report(ref)
    o = spectral_report(ours)
    failures: list[str] = []
    # Reference: hf>5k ≈ 0.05%, hf>3k ≈ 2%
    if o["hf_gt_5k"] > 0.002:
        failures.append(f"hiss: hf>5k={o['hf_gt_5k']*100:.2f}% (ref {r['hf_gt_5k']*100:.2f}%, want <0.2%)")
    if o["hf_gt_3k"] > 0.04:
        failures.append(f"hiss: hf>3k={o['hf_gt_3k']*100:.2f}% (ref {r['hf_gt_3k']*100:.2f}%, want <4%)")
    # Centroid near reference ~500 Hz
    if not (320.0 <= o["centroid_hz"] <= 650.0):
        failures.append(f"centroid={o['centroid_hz']:.0f}Hz (ref {r['centroid_hz']:.0f}Hz, want 320–650)")
    # Energy presence in the string-fundamental band
    if o["band_80_200"] < 0.22:
        failures.append(
            f"band 80–200Hz={o['band_80_200']*100:.1f}% (ref {r['band_80_200']*100:.1f}%, want ≥22%)"
        )
    # Mid presence (ref ~20% in 500–1500)
    if o["band_500_1500"] < 0.10:
        failures.append(
            f"band 500–1500Hz={o['band_500_1500']*100:.1f}% (ref {r['band_500_1500']*100:.1f}%, want ≥10%)"
        )
    return failures


def load_wav_mono(path: Path) -> tuple[np.ndarray, int]:
    with wave.open(str(path), "rb") as wf:
        nch, sr, nf = wf.getnchannels(), wf.getframerate(), wf.getnframes()
        raw = np.frombuffer(wf.readframes(nf), dtype=np.int16).astype(np.float64)
        mono = raw.reshape(-1, nch).mean(axis=1) / 32768.0
    return mono, sr


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--out-dir",
        type=Path,
        default=Path(__file__).resolve().parents[1]
        / "src/client/resources/assets/dwm/sounds",
    )
    parser.add_argument("--seed", type=int, default=1963)
    parser.add_argument(
        "--validate-ref",
        type=Path,
        default=None,
        help="Optional WAV/ path used only for spectral validation (never packaged).",
    )
    args = parser.parse_args()

    out_dir: Path = args.out_dir
    out_dir.mkdir(parents=True, exist_ok=True)
    tmp = out_dir / ".tmp_sfx"
    tmp.mkdir(exist_ok=True)

    rng = np.random.default_rng(args.seed)
    demat = synthesize_demat_loop(rng)
    mat = synthesize_mat_loop(rng)
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
        rep = spectral_report(samples)
        print(
            f"Wrote {ogg_path} ({rep['duration_s']:.2f}s) "
            f"centroid={rep['centroid_hz']:.0f}Hz "
            f"hf>5k={rep['hf_gt_5k']*100:.2f}%"
        )

    if args.validate_ref is not None:
        ref_path = args.validate_ref
        if ref_path.suffix.lower() == ".mp3":
            ref_wav = tmp / "validate_ref.wav"
            subprocess.run(
                ["ffmpeg", "-y", "-i", str(ref_path), str(ref_wav)],
                check=True,
                capture_output=True,
            )
            ref, ref_sr = load_wav_mono(ref_wav)
        else:
            ref, ref_sr = load_wav_mono(ref_path)
        failures = validate_against_reference(demat, ref, ref_sr)
        if failures:
            print("VALIDATION FAILED vs reference:", file=sys.stderr)
            for msg in failures:
                print(f"  - {msg}", file=sys.stderr)
            for wav in tmp.glob("*.wav"):
                wav.unlink()
            tmp.rmdir()
            return 1
        print("Validation OK vs reference spectral traits.")

    for wav in tmp.glob("*.wav"):
        wav.unlink()
    tmp.rmdir()
    return 0


if __name__ == "__main__":
    sys.exit(main())
