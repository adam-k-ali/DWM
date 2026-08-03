#!/usr/bin/env python3
"""
Synthesize seamless TARDIS dematerialise/materialise loop SFX and a landing thud.

Recreates the Radiophonic Workshop technique in spirit (Hodgson): key scrape on
bass piano strings → tape varispeed → feedback/echo; materialise is the reverse
motion; thud is a short landing bang.

Metallic texture is modal/inharmonic + light FM/ring-mod, then shaped with a
baked analysis-derived spectral envelope (tools/fixtures/baked_vworp_targets.npz)
so the loop tracks golden timbre/dynamics without packaging reference audio.

Does not sample or copy BBC media. Requires numpy; writes WAV then ffmpeg → OGG.
"""

from __future__ import annotations

import argparse
import math
import subprocess
import sys
import wave
from functools import lru_cache
from pathlib import Path

import numpy as np

SR = 44100
# One full vworp ≈ 1.75s (centroid autocorr on reference); 2 per seamless loop.
PULSE_SECONDS = 1.75
LOOP_SECONDS = PULSE_SECONDS * 2.0  # 3.5s
THUD_SECONDS = 0.40

# Piano-string-like stiffness (inharmonic partial stretch) — metallic, not harmonic siren.
STRING_B = 0.00045
# Allow 1.5–4 kHz scrape bloom (golden ~8–10% there) while keeping hf>5k ≈ 0.
HF_CUTOFF_HZ = 4800.0

MORPH_N_FFT = 1024
MORPH_HOP = 256
MORPH_STRENGTH = 0.92
BAKED_TARGETS = Path(__file__).resolve().parent / "fixtures" / "baked_vworp_targets.npz"


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
    raw = rng.standard_normal(n)
    env = one_pole_lowpass(np.abs(raw), 0.994)
    env = one_pole_lowpass(env, 0.992)
    env = env / (np.max(env) or 1.0)
    return 0.60 + 0.40 * env


def curved_f0_sweep(start_hz: float, end_hz: float, n: int) -> np.ndarray:
    """Geom sweep: linger near the high end mid-pulse, then settle (classic vworp glide)."""
    u = np.linspace(0.0, 1.0, n)
    if start_hz > end_hz:
        u = 1.0 - (1.0 - u) ** 1.35
    else:
        u = u ** 1.35
    return start_hz * (end_hz / start_hz) ** u


def brightness_bloom(n: int) -> np.ndarray:
    """Mid–late scrape bloom: centroid/HF rise peaking ~1.15–1.25s of a 1.75s pulse."""
    t = np.linspace(0.0, 1.0, n)
    peak = 0.68
    width = 0.38
    x = (t - peak) / width
    bloom = np.exp(-1.7 * (x * x))
    early = np.clip((t - 0.22) / 0.28, 0.0, 1.0)
    return 0.04 + 0.96 * bloom * early


def hf_scrape_layer(f0: np.ndarray, bloom: np.ndarray) -> np.ndarray:
    """Discrete metallic energy in 1.5–4 kHz (golden ~8%), gated by bloom."""
    carrier = np.clip(f0, 70.0, 220.0)
    amps = [0.0, 0.0, 0.0, 0.0, 0.10, 0.18, 0.26, 0.30, 0.26, 0.18, 0.10]
    layer = modal_string_scrape(carrier, amps)
    for hz, amp in ((850.0, 0.40), (1300.0, 0.55), (1950.0, 0.48), (2600.0, 0.30), (3200.0, 0.16)):
        hi = np.full_like(f0, hz)
        layer += modal_string_scrape(hi, [1.0, 0.40, 0.15]) * amp
    layer += fm_metallic(np.full_like(f0, 1500.0), index=1.6, ratio=math.sqrt(2.0)) * 0.18
    layer += ring_mod_tones(np.full_like(f0, 650.0), np.full_like(f0, 1750.0)) * 0.14
    return layer * bloom


@lru_cache(maxsize=1)
def _load_baked_targets() -> tuple[np.ndarray, np.ndarray]:
    """Analysis-derived spectral snaps + RMS envelope (not packaged reference audio)."""
    if not BAKED_TARGETS.exists():
        raise FileNotFoundError(
            f"Missing baked vworp targets: {BAKED_TARGETS}\n"
            "Regenerate with tools/fixtures/golden analysis helpers."
        )
    data = np.load(BAKED_TARGETS)
    return data["snaps"].astype(np.float64), data["env64"].astype(np.float64)


def _rms_envelope(samples: np.ndarray, win_s: float = 0.02) -> np.ndarray:
    win = max(1, int(win_s * SR))
    kernel = np.ones(win, dtype=np.float64) / win
    return np.convolve(np.abs(samples), kernel, mode="same")


def morph_to_baked_spectrum(signal: np.ndarray, strength: float = MORPH_STRENGTH) -> np.ndarray:
    """Replace STFT magnitude toward baked golden shape; keep synth phase."""
    snaps, _ = _load_baked_targets()
    n = len(signal)
    win = np.hanning(MORPH_N_FFT)
    n_frames = 1 + max(0, (n - MORPH_N_FFT) // MORPH_HOP)
    idx = np.linspace(0, len(snaps) - 1, n_frames)
    out = np.zeros(n, dtype=np.float64)
    norm = np.zeros(n, dtype=np.float64)
    for fi, i in enumerate(range(0, n - MORPH_N_FFT + 1, MORPH_HOP)):
        frame = signal[i : i + MORPH_N_FFT] * win
        spec = np.fft.rfft(frame)
        mag = np.abs(spec)
        phase = np.angle(spec)
        target = snaps[int(round(idx[fi]))]
        energy = float(mag.sum()) or 1.0
        mag_n = mag / (float(mag.sum()) or 1.0)
        tgt_n = target / (float(target.sum()) or 1.0)
        blended = ((1.0 - strength) * mag_n + strength * tgt_n) * energy
        frame_o = np.fft.irfft(blended * np.exp(1j * phase), n=MORPH_N_FFT) * win
        out[i : i + MORPH_N_FFT] += frame_o
        norm[i : i + MORPH_N_FFT] += win * win
    norm[norm < 1e-8] = 1.0
    return out / norm


def baked_pulse_envelope(n: int) -> np.ndarray:
    """Interpolate the baked one-vworp RMS envelope to ``n`` samples."""
    _, env64 = _load_baked_targets()
    env = np.interp(np.linspace(0.0, 1.0, n), np.linspace(0.0, 1.0, len(env64)), env64)
    return 0.04 + 0.96 * env


def boundary_pulse_gate(
    n: int, pulse_n: int, *, floor: float = 0.04, fade_s: float = 0.05
) -> np.ndarray:
    """Narrow fades only at pulse edges — preserves mid-pulse envelope, raises crest."""
    gate = np.ones(n, dtype=np.float64)
    fade = max(1, int(fade_s * SR))
    for start in range(0, n, pulse_n):
        end = min(n, start + pulse_n)
        f = min(fade, (end - start) // 3)
        gate[start : start + f] = np.linspace(floor, 1.0, f)
        gate[end - f : end] = np.linspace(1.0, floor, f)
    return gate


def vworp_pulse(
    n: int,
    rng: np.random.Generator,
    *,
    start_hz: float,
    end_hz: float,
) -> np.ndarray:
    """One ~1.75s vworp: modal scrape → baked spectral morph → golden-like envelope."""
    f0 = curved_f0_sweep(start_hz, end_hz, n)
    bloom = brightness_bloom(n)

    scrape = modal_string_scrape(f0, [0.9, 0.65])
    scrape += modal_string_scrape(np.full(n, 95.0), [0.85, 0.4, 0.12]) * 0.4
    scrape += modal_string_scrape(f0, [0.0, 0.0, 0.5, 0.35, 0.22, 0.12]) * (0.2 + 0.8 * bloom)
    scrape += fm_metallic(f0, index=1.8, ratio=math.sqrt(2.0)) * (0.08 + 0.25 * bloom)
    scrape += hf_scrape_layer(f0, bloom) * 1.0
    scrape *= 1.0 - 0.60 * bloom
    scrape *= scrape_chatter_am(n, rng)
    scrape = soft_clip(scrape, drive=1.12)
    scrape /= np.max(np.abs(scrape)) or 1.0

    scrape = morph_to_baked_spectrum(scrape, MORPH_STRENGTH)
    # Reinforce ~95 Hz body (ducked through bloom so centroid can climb).
    scrape += modal_string_scrape(np.full(n, 95.0), [1.0, 0.35, 0.1]) * 0.30 * (1.0 - 0.70 * bloom)
    scrape += hf_scrape_layer(f0, bloom) * 0.15
    scrape += modal_string_scrape(np.full(n, 1600.0), [1.0, 0.4, 0.15]) * 0.12 * bloom
    scrape /= np.max(np.abs(scrape)) or 1.0

    # Flatten carrier RMS then apply baked envelope so dynamics track golden.
    scrape = scrape / (_rms_envelope(scrape) + 1e-4)
    out = scrape * baked_pulse_envelope(n)
    peak = np.max(np.abs(out)) or 1.0
    return out / peak


def rising_whoosh_modal(n: int, rising: bool) -> np.ndarray:
    """Soft take-off swell — low-level only so it doesn't fill pulse gaps."""
    if rising:
        f0 = np.concatenate(
            [np.geomspace(95.0, 180.0, n // 2), np.geomspace(95.0, 180.0, n - n // 2)]
        )
    else:
        f0 = np.concatenate(
            [np.geomspace(180.0, 95.0, n // 2), np.geomspace(180.0, 95.0, n - n // 2)]
        )
    whoosh = modal_string_scrape(f0, [0.55, 0.28, 0.10])
    half = n // 2
    env = np.zeros(n)
    env[:half] = np.sin(np.pi * np.linspace(0.0, 1.0, half))
    env[half:] = np.sin(np.pi * np.linspace(0.0, 1.0, n - half))
    return whoosh * (0.15 + 0.85 * env)


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

    if descending:
        pulse_specs = [(200.0, 95.0), (185.0, 90.0)]
    else:
        pulse_specs = [(95.0, 200.0), (90.0, 185.0)]

    for i, (start_hz, end_hz) in enumerate(pulse_specs):
        start = i * pulse_n
        end = min(n, start + pulse_n)
        layers[start:end] += vworp_pulse(end - start, rng, start_hz=start_hz, end_hz=end_hz)

    layers *= boundary_pulse_gate(n, pulse_n, floor=0.04, fade_s=0.05)
    layers = apply_feedback_delay(
        layers, delay_samples=int(0.70 * SR), feedback=0.10, mix=0.03, passes=2, lp_coeff=0.90
    )
    layers = soft_clip(layers, drive=1.01)
    layers = brickwall_lowpass(layers, HF_CUTOFF_HZ)
    peak = np.max(np.abs(layers)) or 1.0
    layers = layers / peak * 0.85
    return make_seamless(layers, crossfade=int(0.03 * SR))


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


def main() -> int:
    # Shared analysis lives beside this script (tools/).
    tools_dir = Path(__file__).resolve().parent
    sys.path.insert(0, str(tools_dir))
    from tardis_sfx_analysis import (  # noqa: E402
        hard_gate_failures,
        load_audio_mono,
        ref_analysis_slice,
        resample_linear,
        spectral_report,
    )

    default_ref = tools_dir / "fixtures" / "tardis_ref.wav"
    default_compare = tools_dir / "fixtures" / "compare_out"

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
        nargs="?",
        const=default_ref,
        default=None,
        help=(
            "WAV/MP3 used only for spectral hard-gates (never packaged). "
            f"Pass flag alone to use {default_ref}."
        ),
    )
    parser.add_argument(
        "--compare-report",
        type=Path,
        nargs="?",
        const=default_compare,
        default=None,
        help=(
            "Write markdown+PNG compare report under this dir "
            f"(default {default_compare} when flag is passed alone). "
            "Requires a golden --validate-ref or existing fixtures/tardis_ref.wav."
        ),
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

    ref_path: Path | None = args.validate_ref
    if args.compare_report is not None and ref_path is None and default_ref.exists():
        ref_path = default_ref

    exit_code = 0
    if ref_path is not None:
        if not ref_path.exists():
            print(
                f"Missing golden reference: {ref_path}\n"
                f"Run: tools/.venv/bin/python tools/fetch_tardis_ref.py",
                file=sys.stderr,
            )
            exit_code = 2
        else:
            ref, ref_sr = load_audio_mono(ref_path)
            ref = resample_linear(ref_analysis_slice(ref, ref_sr), ref_sr, SR)
            failures = hard_gate_failures(demat, ref, SR)
            if failures:
                print("VALIDATION FAILED vs reference:", file=sys.stderr)
                for msg in failures:
                    print(f"  - {msg}", file=sys.stderr)
                exit_code = 1
            else:
                print("Validation OK vs reference spectral traits.")

            if args.compare_report is not None:
                # Defer to compare CLI for plots/report (keeps generator lean).
                compare = tools_dir / "compare_tardis_sfx.py"
                cmd = [
                    sys.executable,
                    str(compare),
                    "--ref",
                    str(ref_path),
                    "--ours",
                    str(out_dir / "tardis_dematerialise_loop.ogg"),
                    "--mat",
                    str(out_dir / "tardis_materialise_loop.ogg"),
                    "--out-dir",
                    str(args.compare_report),
                ]
                print("Running compare report…")
                subprocess.run(cmd, check=False)

    for wav in tmp.glob("*.wav"):
        wav.unlink()
    if tmp.exists():
        try:
            tmp.rmdir()
        except OSError:
            pass
    return exit_code


if __name__ == "__main__":
    sys.exit(main())
