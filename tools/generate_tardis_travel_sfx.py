#!/usr/bin/env python3
"""
Synthesize seamless TARDIS dematerialise/materialise/flight loop SFX and a landing thud.

Recreates the Radiophonic Workshop technique in spirit (Hodgson): key scrape on
bass piano strings → tape varispeed → feedback/echo; materialise is the reverse
motion; in-flight is the same vocabulary at higher pitch; thud is a short landing bang.

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
# In-flight: same demat/mat gestures, slightly pitched up; tempo matched (no speedup).
FLIGHT_PITCH = 1.12
FLIGHT_PEAK_LEVEL = 0.22
FLIGHT_TARGET_RMS = 0.042
# Baked loop crossfades — long enough to hide pitch turns at Minecraft hard-restart.
LOOP_CROSSFADE_S = 0.65
FLIGHT_LOOP_CROSSFADE_S = 0.55
LOOP_PULSE_OVERLAP_S = 0.18
LOOP_RESEAM_S = 0.35

# Piano-string-like stiffness (inharmonic partial stretch) — metallic, not harmonic siren.
STRING_B = 0.00045
# Allow 1.5–4 kHz scrape bloom (golden ~8–10% there) while keeping hf>5k ≈ 0.
HF_CUTOFF_HZ = 4800.0
# Pitch-up brightens noise carriers — keep flight under the same HF ceiling.
FLIGHT_HF_CUTOFF_HZ = HF_CUTOFF_HZ

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


def reseam_inplace(signal: np.ndarray, crossfade: int) -> np.ndarray:
    """Blend start into end without trimming — repairs encoder edge damage for looping."""
    if crossfade <= 0 or crossfade * 2 >= len(signal):
        return signal
    out = signal.copy()
    u = np.linspace(0.0, 1.0, crossfade)
    fade_out = np.cos(0.5 * np.pi * u)
    fade_in = np.sin(0.5 * np.pi * u)
    out[-crossfade:] = out[-crossfade:] * fade_out + out[:crossfade] * fade_in
    # Glue a few samples so the hard wrap has no residual step after Vorbis.
    glue = min(48, crossfade // 4)
    if glue >= 2:
        delta = float(out[0] - out[-1])
        ramp = np.linspace(0.0, 1.0, glue)
        out[-glue:] += 0.5 * delta * ramp
        out[:glue] -= 0.5 * delta * ramp[::-1]
    return out


def write_loop_ogg(
    samples: np.ndarray,
    wav_path: Path,
    ogg_path: Path,
    *,
    repair_crossfade_s: float = 0.20,
) -> None:
    """
    Encode a seamless loop, then decode/re-seam/re-encode so Vorbis edge damage
    does not leave a click when Minecraft hard-restarts the decoded buffer.
    """
    write_wav(wav_path, samples)
    wav_to_ogg(wav_path, ogg_path)
    # Late import keeps module import light when only synthesizing in tests.
    tools_dir = Path(__file__).resolve().parent
    if str(tools_dir) not in sys.path:
        sys.path.insert(0, str(tools_dir))
    from tardis_sfx_analysis import load_audio_mono  # noqa: E402

    decoded, dec_sr = load_audio_mono(ogg_path)
    if dec_sr != SR:
        # load_audio_mono may leave native rate; resample linearly if needed.
        n = int(round(len(decoded) * SR / dec_sr))
        decoded = np.interp(
            np.linspace(0.0, 1.0, n, endpoint=False),
            np.linspace(0.0, 1.0, len(decoded), endpoint=False),
            decoded,
        ).astype(np.float64)
    repaired = reseam_inplace(decoded, int(repair_crossfade_s * SR))
    write_wav(wav_path, repaired)
    wav_to_ogg(wav_path, ogg_path)


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


def echo_only(
    signal: np.ndarray,
    delay_samples: int,
    feedback: float,
    passes: int,
    lp_coeff: float,
) -> np.ndarray:
    """Feedback delay minus dry — pure repeats for an echo send."""
    wet = apply_feedback_delay(
        signal,
        delay_samples=delay_samples,
        feedback=feedback,
        mix=1.0,
        passes=passes,
        lp_coeff=lp_coeff,
    )
    return wet - signal


def apply_tape_echo_tail(signal: np.ndarray, pulse_n: int) -> np.ndarray:
    """
    Radiophonic-style decaying repeats that fill each vworp's release.

    Send is mid-pulse weighted (the scrape), so delayed repeats land in the
    quiet tail — body stays mostly dry, end rings instead of chopping.
    """
    n = len(signal)
    send_env = np.zeros(n, dtype=np.float64)
    for start in range(0, n, pulse_n):
        end = min(n, start + pulse_n)
        m = end - start
        t = np.linspace(0.0, 1.0, m)
        # Bell over the scrape body; little send on the silent edges.
        body = np.exp(-((t - 0.52) / 0.28) ** 2)
        send_env[start:end] = body
    send = signal * send_env

    slap = echo_only(send, int(0.30 * SR), feedback=0.42, passes=5, lp_coeff=0.90)
    wash = echo_only(send, int(0.58 * SR), feedback=0.30, passes=3, lp_coeff=0.93)
    wet = slap * 0.32 + wash * 0.18
    # Keep a little wet across the boundary so the next pulse inherits a wash,
    # but taper the very end of the loop buffer via seamless crossfade later.
    return signal + wet


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
    """Geom sweep with continuous motion (avoid parking on one pitch = sine drone)."""
    u = np.linspace(0.0, 1.0, n)
    if start_hz > end_hz:
        # descending: ease into the low body without a long static tail
        u = 1.0 - (1.0 - u) ** 1.15
    else:
        # ascending: keep climbing through the pulse (old u**1.35 parked at end_hz)
        u = u ** 0.85
    return start_hz * (end_hz / start_hz) ** u


def brightness_bloom(n: int) -> np.ndarray:
    """Mid–late scrape bloom: centroid/HF rise peaking ~1.15–1.25s of a 1.75s pulse."""
    t = np.linspace(0.0, 1.0, n)
    peak = 0.66
    width = 0.34
    x = (t - peak) / width
    bloom = np.exp(-1.7 * (x * x))
    early = np.clip((t - 0.22) / 0.28, 0.0, 1.0)
    # Let HF scrape die into the low body (golden last ~20% is low-dominant).
    late = np.clip(1.0 - (t - 0.78) / 0.18, 0.0, 1.0)
    return 0.04 + 0.96 * bloom * early * late


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
    """Replace STFT magnitude toward baked golden shape; keep carrier phase."""
    snaps, _ = _load_baked_targets()
    n = len(signal)
    win = np.hanning(MORPH_N_FFT)
    freqs = np.fft.rfftfreq(MORPH_N_FFT, 1.0 / SR)
    n_frames = 1 + max(0, (n - MORPH_N_FFT) // MORPH_HOP)
    idx = np.linspace(0, len(snaps) - 1, n_frames)
    out = np.zeros(n, dtype=np.float64)
    norm = np.zeros(n, dtype=np.float64)
    # Widen low-frequency peaks so morph doesn't reprint a pure ~95–200 Hz sine.
    smooth_kernel = np.ones(7, dtype=np.float64) / 7.0
    low_mask = freqs < 320.0
    for fi, i in enumerate(range(0, n - MORPH_N_FFT + 1, MORPH_HOP)):
        frame = signal[i : i + MORPH_N_FFT] * win
        spec = np.fft.rfft(frame)
        mag = np.abs(spec)
        phase = np.angle(spec)
        target = snaps[int(round(idx[fi]))].copy()
        smoothed = np.convolve(target, smooth_kernel, mode="same")
        target[low_mask] = 0.35 * target[low_mask] + 0.65 * smoothed[low_mask]
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
    """Interpolate the baked one-vworp RMS envelope to ``n`` samples.

    The baked curve re-swells in the last ~15% then cliffs in one bin. Cap that
    bump so level eases down into the boundary gate instead of jumping then cutting.
    """
    _, env64 = _load_baked_targets()
    env = np.interp(np.linspace(0.0, 1.0, n), np.linspace(0.0, 1.0, len(env64)), env64)
    env = 0.04 + 0.96 * env
    t = np.linspace(0.0, 1.0, n)
    # From ~82%: hold a soft declining ceiling (kill 0.53→0.74 re-swell).
    anchor_t = 0.82
    anchor_i = int(anchor_t * (n - 1))
    anchor = float(env[anchor_i])
    late = t >= anchor_t
    u = (t[late] - anchor_t) / (1.0 - anchor_t)
    # End ceiling ~55% of anchor — gate supplies the final soft landing.
    ceiling = anchor * (0.55 + 0.45 * (0.5 * (1.0 + np.cos(np.pi * u))))
    env = env.copy()
    env[late] = np.minimum(env[late], ceiling)
    return env


def boundary_pulse_gate(
    n: int, pulse_n: int, *, floor: float = 0.015, fade_s: float = 0.20, gate_outer: bool = True
) -> np.ndarray:
    """Cosine fades at pulse edges — ~200ms release so the vworp eases out.

    When ``gate_outer`` is False, skips the loop-buffer start/end fades so
    ``make_seamless`` can own the wrap without fighting a double gate.
    """
    gate = np.ones(n, dtype=np.float64)
    fade = max(1, int(fade_s * SR))
    for start in range(0, n, pulse_n):
        end = min(n, start + pulse_n)
        f = min(fade, (end - start) // 3)
        u_in = np.linspace(0.0, 1.0, f)
        cos_in = floor + (1.0 - floor) * (0.5 - 0.5 * np.cos(np.pi * u_in))
        cos_out = floor + (1.0 - floor) * (0.5 + 0.5 * np.cos(np.pi * u_in))
        if gate_outer or start > 0:
            gate[start : start + f] = cos_in
        if gate_outer or end < n:
            gate[end - f : end] = cos_out
    return gate


def soften_narrow_tones(
    signal: np.ndarray,
    max_peak_ratio: float = 3.5,
    *,
    min_hz: float = 150.0,
) -> np.ndarray:
    """
    Cap overly sharp spectral peaks above ``min_hz`` so a parked partial
    can't read as a sine drone. Leaves the ~95 Hz body region alone.
    """
    n = len(signal)
    win = np.hanning(MORPH_N_FFT)
    freqs = np.fft.rfftfreq(MORPH_N_FFT, 1.0 / SR)
    protect = freqs < min_hz
    out = np.zeros(n, dtype=np.float64)
    norm = np.zeros(n, dtype=np.float64)
    for i in range(0, n - MORPH_N_FFT + 1, MORPH_HOP):
        frame = signal[i : i + MORPH_N_FFT] * win
        spec = np.fft.rfft(frame)
        mag = np.abs(spec)
        phase = np.angle(spec)
        pad = 2
        capped = mag.copy()
        for b in range(pad, len(mag) - pad):
            if protect[b]:
                continue
            neighbourhood = mag[b - pad : b + pad + 1]
            med = float(np.median(neighbourhood))
            if med > 1e-12 and mag[b] > max_peak_ratio * med:
                capped[b] = max_peak_ratio * med
        frame_o = np.fft.irfft(capped * np.exp(1j * phase), n=MORPH_N_FFT) * win
        out[i : i + MORPH_N_FFT] += frame_o
        norm[i : i + MORPH_N_FFT] += win * win
    norm[norm < 1e-8] = 1.0
    result = out / norm
    if n > MORPH_N_FFT:
        result[:MORPH_HOP] = signal[:MORPH_HOP] * 0.5 + result[:MORPH_HOP] * 0.5
        result[-MORPH_HOP:] = signal[-MORPH_HOP:] * 0.5 + result[-MORPH_HOP:] * 0.5
    return result


def duck_steady_lowmid(signal: np.ndarray, rng: np.random.Generator) -> np.ndarray:
    """
    Break parked low tones on materialise: blur + chatter-AM 70–480 Hz.

    The scrape lives above this band; a steady ~100–200 Hz partial was reading
    as a sine underneath.
    """
    n = len(signal)
    win = np.hanning(MORPH_N_FFT)
    freqs = np.fft.rfftfreq(MORPH_N_FFT, 1.0 / SR)
    deep = (freqs >= 70.0) & (freqs < 180.0)
    mid = (freqs >= 180.0) & (freqs <= 400.0)
    kernel = np.ones(9, dtype=np.float64) / 9.0
    chatter = scrape_chatter_am(n, rng)
    out = np.zeros(n, dtype=np.float64)
    norm = np.zeros(n, dtype=np.float64)
    for i in range(0, n - MORPH_N_FFT + 1, MORPH_HOP):
        frame = signal[i : i + MORPH_N_FFT] * win
        spec = np.fft.rfft(frame)
        mag = np.abs(spec).copy()
        phase = np.angle(spec)
        blurred = np.convolve(mag, kernel, mode="same")
        am = float(chatter[min(i + MORPH_N_FFT // 2, n - 1)])
        # De-tone without hollowing: blur peaks + light chatter AM.
        mag[deep] = (0.55 * mag[deep] + 0.45 * blurred[deep]) * (0.70 + 0.25 * am)
        mag[mid] = (0.45 * mag[mid] + 0.55 * blurred[mid]) * (0.60 + 0.30 * am)
        frame_o = np.fft.irfft(mag * np.exp(1j * phase), n=MORPH_N_FFT) * win
        out[i : i + MORPH_N_FFT] += frame_o
        norm[i : i + MORPH_N_FFT] += win * win
    norm[norm < 1e-8] = 1.0
    return out / norm


def vworp_pulse(
    n: int,
    rng: np.random.Generator,
    *,
    start_hz: float,
    end_hz: float,
) -> np.ndarray:
    """
    One ~1.75s vworp: noise carrier + light modal glide → baked spectral morph.

    Materialise previously parked a modal partial near ~200 Hz (heard as a sine
    under the scrape). Noise carrier + low-band ducking removes that drone.
    """
    f0 = curved_f0_sweep(start_hz, end_hz, n)
    bloom = brightness_bloom(n)
    ascending = end_hz > start_hz

    carrier = rng.standard_normal(n)
    carrier = one_pole_lowpass(carrier, 0.85)
    carrier = brickwall_lowpass(carrier, 4500.0)

    # Ascending: no modal (it parked and sang). Descending keeps a light glide.
    if ascending:
        modal_mix = 0.0
        modal = 0.0
    else:
        modal = modal_string_scrape(f0, [0.45, 0.18])
        modal += fm_metallic(f0, index=1.2, ratio=math.sqrt(2.0)) * (0.06 + 0.12 * bloom)
        modal *= scrape_chatter_am(n, rng)
        modal_mix = 0.035

    scrape = carrier + modal * modal_mix
    scrape *= 1.0 - 0.28 * bloom
    scrape = soft_clip(scrape, drive=1.06)
    scrape /= np.max(np.abs(scrape)) or 1.0

    scrape = morph_to_baked_spectrum(scrape, 0.975)
    scrape = soften_narrow_tones(scrape, max_peak_ratio=2.3, min_hz=110.0)
    if ascending:
        scrape = duck_steady_lowmid(scrape, rng)
    nz_hf = brickwall_lowpass(rng.standard_normal(n), 4500.0)
    hf_amt = 0.14 if ascending else 0.08
    scrape += nz_hf * (hf_amt * 0.35 + hf_amt * bloom)
    scrape /= np.max(np.abs(scrape)) or 1.0

    scrape = scrape / (_rms_envelope(scrape) + 1e-4)
    out = scrape * baked_pulse_envelope(n)
    if ascending:
        # Notch strongest 80–250 Hz peaks that read as a sine under the scrape.
        spec = np.fft.rfft(out)
        freqs = np.fft.rfftfreq(len(out), 1.0 / SR)
        mag = np.abs(spec)
        gain = np.ones_like(freqs)
        search = (freqs >= 80.0) & (freqs <= 250.0)
        work = mag.copy()
        work[~search] = 0.0
        for depth, width in ((0.75, 16.0), (0.50, 20.0)):
            if not np.any(work > 0):
                break
            peak_hz = float(freqs[int(np.argmax(work))])
            gain *= 1.0 - depth * np.exp(-0.5 * ((freqs - peak_hz) / width) ** 2)
            work[np.abs(freqs - peak_hz) < 28.0] = 0.0
        out = np.fft.irfft(spec * gain, n=len(out))
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
    """
    Bake a loop-point crossfade for engines that hard-restart the sample (Minecraft).

    Equal-power blend of the tail into the head, then drop the original head so
    playback wraps from the blended end onto the natural continuation. Do not
    RMS-rescale the head — that breaks sample continuity at the wrap and leaves
    a loud/quiet jump after the trim.
    """
    if crossfade <= 0 or crossfade * 2 >= len(signal):
        return signal
    out = signal.copy()
    u = np.linspace(0.0, 1.0, crossfade)
    # Equal-power crossfade (constant summed power for uncorrelated-ish material).
    fade_out = np.cos(0.5 * np.pi * u)
    fade_in = np.sin(0.5 * np.pi * u)
    head = out[:crossfade]
    tail = out[-crossfade:]
    out[-crossfade:] = tail * fade_out + head * fade_in
    # Trim duplicated head — new start is what followed the old head.
    # Continuity: last sample ≈ head[-1], first sample = original[crossfade].
    return out[crossfade:]


def finalize_loop(signal: np.ndarray, crossfade_s: float) -> np.ndarray:
    """Trim-crossfade then in-place reseam — flight-strength wrap for demat/mat too."""
    seamed = make_seamless(signal, crossfade=int(crossfade_s * SR))
    return reseam_inplace(seamed, crossfade=int(LOOP_RESEAM_S * SR))


def pitch_shift(signal: np.ndarray, ratio: float) -> np.ndarray:
    """Raise pitch by resampling (duration shrinks by ``ratio``)."""
    if ratio <= 0.0 or abs(ratio - 1.0) < 1e-6:
        return signal
    n = len(signal)
    new_n = max(2, int(round(n / ratio)))
    t_old = np.linspace(0.0, 1.0, n, endpoint=False)
    t_new = np.linspace(0.0, 1.0, new_n, endpoint=False)
    return np.interp(t_new, t_old, signal).astype(np.float64)


def _cosine_fade(n: int, *, fade_in: bool) -> np.ndarray:
    u = np.linspace(0.0, 1.0, n)
    if fade_in:
        return 0.5 - 0.5 * np.cos(np.pi * u)
    return 0.5 + 0.5 * np.cos(np.pi * u)


def synthesize_travel_loop(
    rng: np.random.Generator,
    *,
    pulse_specs: list[tuple[float, float]],
    pulse_seconds: float = PULSE_SECONDS,
    hf_cutoff: float = HF_CUTOFF_HZ,
    peak_level: float = 0.85,
    seamless: bool = True,
    crossfade_s: float = LOOP_CROSSFADE_S,
    pulse_overlap_s: float = LOOP_PULSE_OVERLAP_S,
) -> np.ndarray:
    pulse_n = int(pulse_seconds * SR)
    n = pulse_n * len(pulse_specs)
    layers = np.zeros(n, dtype=np.float64)
    # Overlap adjacent vworps so the join isn't a hard cut (heard as a click).
    overlap = int(pulse_overlap_s * SR)

    for i, (start_hz, end_hz) in enumerate(pulse_specs):
        if i == 0:
            layers[:pulse_n] = vworp_pulse(pulse_n, rng, start_hz=start_hz, end_hz=end_hz)
            continue
        # Longer pulse so after overlapping the previous tail we still fill to loop end.
        pulse = vworp_pulse(pulse_n + overlap, rng, start_hz=start_hz, end_hz=end_hz)
        join = i * pulse_n
        fade_in = _cosine_fade(overlap, fade_in=True)
        fade_out = 1.0 - fade_in
        layers[join - overlap : join] = (
            layers[join - overlap : join] * fade_out + pulse[:overlap] * fade_in
        )
        layers[join : join + pulse_n] = pulse[overlap : overlap + pulse_n]

    # Interior pulse joins only — outer edges left for make_seamless.
    layers *= boundary_pulse_gate(n, pulse_n, floor=0.55, fade_s=0.04, gate_outer=False)
    layers = apply_tape_echo_tail(layers, pulse_n)
    layers = soft_clip(layers, drive=1.02)
    layers = brickwall_lowpass(layers, hf_cutoff)
    peak = np.max(np.abs(layers)) or 1.0
    layers = layers / peak * peak_level
    if not seamless:
        return layers
    # Long loop-point crossfade + reseam: Minecraft restarts with no engine blend.
    return finalize_loop(layers, crossfade_s)


def synthesize_demat_loop(rng: np.random.Generator) -> np.ndarray:
    """Descending lead + quieter return ascent so the loop wrap pitch matches."""
    return _synthesize_gesture_loop(
        rng,
        lead_specs=(200.0, 95.0),
        return_specs=(95.0, 195.0),
        return_level=0.72,
    )


def synthesize_mat_loop(rng: np.random.Generator) -> np.ndarray:
    """Ascending lead + quieter return descent so the loop wrap pitch matches."""
    return _synthesize_gesture_loop(
        rng,
        lead_specs=(95.0, 200.0),
        return_specs=(195.0, 100.0),
        return_level=0.72,
    )


def _synthesize_gesture_loop(
    rng: np.random.Generator,
    *,
    lead_specs: tuple[float, float],
    return_specs: tuple[float, float],
    return_level: float,
) -> np.ndarray:
    """
    Same wrap strategy as flight (end pitch ≈ start pitch) with the return
    gesture ducked so the lead demat/mat motion still reads as primary.
    """
    raw = synthesize_travel_loop(
        rng,
        pulse_specs=[lead_specs, return_specs],
        seamless=False,
        pulse_overlap_s=LOOP_PULSE_OVERLAP_S,
    )
    pulse_n = int(PULSE_SECONDS * SR)
    # Fade the duck across the overlap so the interior join stays smooth.
    duck = np.ones(len(raw), dtype=np.float64)
    duck[pulse_n:] = return_level
    overlap = int(LOOP_PULSE_OVERLAP_S * SR)
    if overlap > 0 and pulse_n >= overlap:
        ramp = np.linspace(1.0, return_level, overlap)
        duck[pulse_n - overlap : pulse_n] *= ramp
    raw = raw * duck
    peak = float(np.max(np.abs(raw))) or 1.0
    raw = raw / peak * 0.85
    return finalize_loop(raw, LOOP_CROSSFADE_S)



def synthesize_flight_loop(rng: np.random.Generator) -> np.ndarray:
    """Higher-pitched demat+mat vworps for sustained vortex travel."""
    # Synthesize longer first, then pitch-shift by the same ratio so tempo matches
    # demat/mat (resample alone would speed the loop up). Seamless only AFTER
    # pitch-shift — shifting a pre-baked seam reintroduces a wrap click.
    base = synthesize_travel_loop(
        rng,
        pulse_specs=[
            (200.0, 95.0),  # demat-like descent
            (95.0, 200.0),  # mat-like ascent
        ],
        pulse_seconds=PULSE_SECONDS * FLIGHT_PITCH,
        peak_level=0.85,
        seamless=False,
        pulse_overlap_s=LOOP_PULSE_OVERLAP_S,
    )
    shifted = pitch_shift(base, FLIGHT_PITCH)
    # Soften grit from pitching noise carriers; match demat/mat HF ceiling.
    shifted = brickwall_lowpass(shifted, FLIGHT_HF_CUTOFF_HZ)
    shifted = one_pole_lowpass(shifted, 0.72)
    shifted = soft_clip(shifted, drive=1.04)
    # Pitch-up reads louder — level toward materialise RMS/peak, not demat peaks.
    rms = float(np.sqrt(np.mean(shifted**2))) or 1.0
    shifted = shifted * (FLIGHT_TARGET_RMS / rms)
    peak = float(np.max(np.abs(shifted))) or 1.0
    if peak > FLIGHT_PEAK_LEVEL:
        shifted = shifted * (FLIGHT_PEAK_LEVEL / peak)
    return finalize_loop(shifted, FLIGHT_LOOP_CROSSFADE_S)


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
    flight = synthesize_flight_loop(rng)
    thud = synthesize_thud(rng)

    jobs = [
        ("tardis_dematerialise_loop", demat, True),
        ("tardis_materialise_loop", mat, True),
        ("tardis_flight_loop", flight, True),
        ("tardis_materialise_thud", thud, False),
    ]
    for name, samples, is_loop in jobs:
        wav_path = tmp / f"{name}.wav"
        ogg_path = out_dir / f"{name}.ogg"
        if is_loop:
            write_loop_ogg(samples, wav_path, ogg_path)
        else:
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
