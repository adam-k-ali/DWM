"""
Shared analysis helpers for TARDIS travel SFX: load, resample, metrics, plots.

Used by compare_tardis_sfx.py and generate_tardis_travel_sfx.py.
Golden reference audio is analysis-only and must never be packaged into the mod.
"""

from __future__ import annotations

import math
import subprocess
import wave
from dataclasses import dataclass
from pathlib import Path

import numpy as np

SR = 44100
EXPECTED_VWORP_S = 1.75


@dataclass(frozen=True)
class MetricRow:
    name: str
    ours: float
    ref: float
    unit: str
    verdict: str
    detail: str


def load_audio_mono(path: Path) -> tuple[np.ndarray, int]:
    """Load mono float64 samples from WAV, or decode via ffmpeg for mp3/ogg."""
    suffix = path.suffix.lower()
    if suffix == ".wav":
        with wave.open(str(path), "rb") as wf:
            nch, sr, nf = wf.getnchannels(), wf.getframerate(), wf.getnframes()
            raw = np.frombuffer(wf.readframes(nf), dtype=np.int16).astype(np.float64)
            mono = raw.reshape(-1, nch).mean(axis=1) / 32768.0
        return mono, sr

    tmp = path.with_suffix(".tmp_decode.wav")
    try:
        subprocess.run(
            ["ffmpeg", "-y", "-i", str(path), str(tmp)],
            check=True,
            capture_output=True,
        )
        return load_audio_mono(tmp)
    finally:
        if tmp.exists():
            tmp.unlink()


def resample_linear(x: np.ndarray, src_sr: int, dst_sr: int = SR) -> np.ndarray:
    if src_sr == dst_sr:
        return x.astype(np.float64)
    target_n = max(1, int(round(len(x) * dst_sr / src_sr)))
    return np.interp(np.linspace(0, len(x) - 1, target_n), np.arange(len(x)), x).astype(np.float64)


def peak_normalize(x: np.ndarray, peak: float = 0.95) -> np.ndarray:
    m = np.max(np.abs(x)) or 1.0
    return x / m * peak


def rms(x: np.ndarray) -> float:
    return float(np.sqrt(np.mean(x * x)))


def ref_analysis_slice(x: np.ndarray, sr: int) -> np.ndarray:
    """Mid slice of a long take — avoids silent lead-in / landing tail."""
    dur = len(x) / sr
    if dur <= 6.0:
        return x
    return x[int(2.0 * sr) : int(min(dur, 8.0) * sr)]


def one_vworp_slice(x: np.ndarray, sr: int, start_s: float | None = None) -> np.ndarray:
    """Extract ~one vworp for aligned A/B and trajectory plots."""
    n = int(EXPECTED_VWORP_S * sr)
    if len(x) <= n:
        return x
    if start_s is None:
        # Snap to a pulse boundary on seamless 2× loops; avoid straddling two vworps.
        dur = len(x) / sr
        n_pulses = max(1, int(round(dur / EXPECTED_VWORP_S)))
        if abs(dur - n_pulses * EXPECTED_VWORP_S) < 0.08 and n_pulses >= 2:
            start_s = EXPECTED_VWORP_S * (n_pulses // 2)  # second pulse often cleaner
        else:
            start_s = max(0.0, (dur - EXPECTED_VWORP_S) * 0.35)
    start = int(start_s * sr)
    start = max(0, min(start, len(x) - n))
    return x[start : start + n]


def spectral_report(samples: np.ndarray, sample_rate: int = SR) -> dict[str, float]:
    spec = np.abs(np.fft.rfft(samples * np.hanning(len(samples)))) ** 2
    freqs = np.fft.rfftfreq(len(samples), 1.0 / sample_rate)
    tot = float(spec.sum()) or 1.0
    band = lambda lo, hi: float(spec[(freqs >= lo) & (freqs < hi)].sum() / tot)
    # spectral flatness in hiss band (near 0 = tonal/peaky, near 1 = noise-like)
    hiss_mask = (freqs >= 2000) & (freqs <= 8000)
    hiss = spec[hiss_mask] + 1e-20
    flat = float(np.exp(np.mean(np.log(hiss))) / np.mean(hiss)) if np.any(hiss_mask) else 0.0
    # peakiness in string band: energy near top magnitude peaks
    voice_mask = (freqs >= 80) & (freqs <= 800)
    voice = spec[voice_mask].copy()
    voice_freqs = freqs[voice_mask]
    peakiness = 0.0
    if voice.size:
        order = np.argsort(voice)[::-1][:8]
        peak_e = 0.0
        for idx in order:
            f = voice_freqs[idx]
            peak_e += float(spec[(freqs >= f - 8) & (freqs <= f + 8)].sum())
        peakiness = peak_e / tot
    return {
        "centroid_hz": float((freqs * spec).sum() / tot),
        "hf_gt_3k": float(spec[freqs >= 3000].sum() / tot),
        "hf_gt_5k": float(spec[freqs >= 5000].sum() / tot),
        "band_80_200": band(80, 200),
        "band_200_500": band(200, 500),
        "band_500_1500": band(500, 1500),
        "band_1500_4000": band(1500, 4000),
        "flatness_2k_8k": flat,
        "peakiness_80_800": peakiness,
        "crest": float(np.max(np.abs(samples)) / (rms(samples) + 1e-12)),
        "rms": rms(samples),
        "duration_s": len(samples) / sample_rate,
    }


def vworp_period_s(samples: np.ndarray, sample_rate: int = SR) -> tuple[float, float]:
    """Centroid-trajectory autocorr period and peak correlation in 1.2–2.2s."""
    nfft = 4096
    hop = 1024
    if len(samples) < nfft * 4:
        return float("nan"), float("nan")
    cents: list[float] = []
    for i in range(0, len(samples) - nfft, hop):
        frame = samples[i : i + nfft] * np.hanning(nfft)
        sp = np.abs(np.fft.rfft(frame)) ** 2
        fr = np.fft.rfftfreq(nfft, 1.0 / sample_rate)
        sp[fr < 50] = 0
        tot = float(sp.sum()) or 1.0
        cents.append(float((fr * sp).sum() / tot))
    sig = np.asarray(cents, dtype=np.float64)
    sig = sig - sig.mean()
    ac = np.correlate(sig, sig, mode="full")
    ac = ac[len(ac) // 2 :]
    ac = ac / (ac[0] or 1.0)
    env_sr = 1.0 / (hop / sample_rate)
    i0 = int(1.2 * env_sr)
    i1 = min(len(ac) - 1, int(2.2 * env_sr))
    if i1 <= i0:
        return float("nan"), float("nan")
    lag = i0 + int(np.argmax(ac[i0 : i1 + 1]))
    return lag / env_sr, float(ac[lag])


def stft_centroid_trajectory(
    samples: np.ndarray, sample_rate: int = SR, nfft: int = 4096, hop: int = 1024
) -> tuple[np.ndarray, np.ndarray]:
    times: list[float] = []
    cents: list[float] = []
    for i in range(0, max(1, len(samples) - nfft), hop):
        frame = samples[i : i + nfft] * np.hanning(nfft)
        sp = np.abs(np.fft.rfft(frame)) ** 2
        fr = np.fft.rfftfreq(nfft, 1.0 / sample_rate)
        sp[fr < 40] = 0
        tot = float(sp.sum()) or 1.0
        times.append(i / sample_rate)
        cents.append(float((fr * sp).sum() / tot))
    return np.asarray(times), np.asarray(cents)


def pulse_gap_ratio(samples: np.ndarray, sample_rate: int = SR, pulse_s: float = EXPECTED_VWORP_S) -> float:
    """Mean RMS of first 25% of each pulse / last 25% — higher = clearer gaps."""
    pulse_n = int(pulse_s * sample_rate)
    if pulse_n < 8 or len(samples) < pulse_n:
        return float("nan")
    ratios: list[float] = []
    for start in range(0, len(samples) - pulse_n + 1, pulse_n):
        seg = samples[start : start + pulse_n]
        head = rms(seg[: pulse_n // 4])
        tail = rms(seg[-(pulse_n // 4) :]) + 1e-12
        ratios.append(head / tail)
    return float(np.mean(ratios)) if ratios else float("nan")


@dataclass(frozen=True)
class SimilarityBreakdown:
    """0–100 aggregate similarity vs golden, plus per-component scores in [0, 1]."""

    score: float
    mel: float
    envelope: float
    bands: float
    centroid: float
    crest: float
    period: float
    trajectory: float
    peak: float


def _proximity(ours: float, ref: float, scale: float) -> float:
    """1 when equal; decays toward 0 as |ours−ref| grows past ``scale``."""
    if math.isnan(ours) or math.isnan(ref):
        return float("nan")
    return float(math.exp(-abs(ours - ref) / max(scale, 1e-9)))


def _pearson(a: np.ndarray, b: np.ndarray) -> float:
    if a.size < 4 or b.size < 4:
        return float("nan")
    n = min(a.size, b.size)
    x = a[:n].astype(np.float64)
    y = b[:n].astype(np.float64)
    x = x - x.mean()
    y = y - y.mean()
    denom = float(np.sqrt(np.sum(x * x) * np.sum(y * y)))
    if denom < 1e-20:
        return float("nan")
    return float(np.sum(x * y) / denom)


def _corr_to_unit(corr: float) -> float:
    if math.isnan(corr):
        return float("nan")
    return float(np.clip((corr + 1.0) * 0.5, 0.0, 1.0))


def _aligned_vworp_pair(
    ours: np.ndarray, ref: np.ndarray, sr: int = SR
) -> tuple[np.ndarray, np.ndarray]:
    """Peak-normalised one-vworp slices for timbre/envelope compares."""
    ours_p = peak_normalize(one_vworp_slice(ours, sr))
    ref_src = ref_analysis_slice(ref, sr)
    # Long golden takes: prefer the mid analysis pulse. Short cuts: use whole clip.
    if len(ref_src) > int(EXPECTED_VWORP_S * sr * 1.15):
        ref_p = peak_normalize(one_vworp_slice(ref_src, sr, start_s=2.0))
    else:
        ref_p = peak_normalize(one_vworp_slice(ref_src, sr, start_s=0.0))
    n = min(len(ours_p), len(ref_p))
    return ours_p[:n], ref_p[:n]


def _hz_to_mel(freq: np.ndarray | float) -> np.ndarray | float:
    return 2595.0 * np.log10(1.0 + np.asarray(freq) / 700.0)


def _mel_to_hz(mel: np.ndarray | float) -> np.ndarray | float:
    return 700.0 * (10.0 ** (np.asarray(mel) / 2595.0) - 1.0)


def _mel_filterbank(
    n_fft: int,
    sample_rate: int,
    n_mels: int = 40,
    fmin: float = 80.0,
    fmax: float = 4000.0,
) -> np.ndarray:
    """Triangular mel filterbank (n_mels × n_fft//2+1)."""
    n_bins = n_fft // 2 + 1
    m_min = float(_hz_to_mel(fmin))
    m_max = float(_hz_to_mel(fmax))
    m_pts = np.linspace(m_min, m_max, n_mels + 2)
    f_pts = np.asarray(_mel_to_hz(m_pts), dtype=np.float64)
    bins = np.floor((n_fft + 1) * f_pts / sample_rate).astype(int)
    bins = np.clip(bins, 0, n_bins - 1)
    fb = np.zeros((n_mels, n_bins), dtype=np.float64)
    for i in range(n_mels):
        left, center, right = bins[i], bins[i + 1], bins[i + 2]
        if center <= left:
            center = min(left + 1, n_bins - 1)
        if right <= center:
            right = min(center + 1, n_bins - 1)
        for j in range(left, center):
            fb[i, j] = (j - left) / max(center - left, 1)
        for j in range(center, right):
            fb[i, j] = (right - j) / max(right - center, 1)
    # Normalise filters so each has unit area (avoid mel-band loudness bias).
    sums = fb.sum(axis=1, keepdims=True)
    sums[sums < 1e-12] = 1.0
    return fb / sums


def log_mel_spectrogram(
    samples: np.ndarray,
    sample_rate: int = SR,
    *,
    n_fft: int = 1024,
    hop: int = 256,
    n_mels: int = 40,
    fmin: float = 80.0,
    fmax: float = 4000.0,
) -> np.ndarray:
    """Log-mel spectrogram (n_mels × n_frames)."""
    if len(samples) < n_fft:
        samples = np.pad(samples, (0, n_fft - len(samples)))
    window = np.hanning(n_fft)
    fb = _mel_filterbank(n_fft, sample_rate, n_mels=n_mels, fmin=fmin, fmax=fmax)
    frames: list[np.ndarray] = []
    for i in range(0, len(samples) - n_fft + 1, hop):
        frame = samples[i : i + n_fft] * window
        power = np.abs(np.fft.rfft(frame)) ** 2
        mel = fb @ power
        frames.append(np.log1p(mel * 1e6))
    if not frames:
        return np.zeros((n_mels, 1), dtype=np.float64)
    return np.column_stack(frames)


def mel_similarity(ours: np.ndarray, ref: np.ndarray, sr: int = SR) -> float:
    """
    Timbre similarity from log-mel spectrograms on one aligned vworp.

    Mean per-frame cosine similarity in [0, 1] (negative cosines clamped).
    """
    ours_p, ref_p = _aligned_vworp_pair(ours, ref, sr)
    mo = log_mel_spectrogram(ours_p, sr)
    mr = log_mel_spectrogram(ref_p, sr)
    n_frames = min(mo.shape[1], mr.shape[1])
    if n_frames < 2:
        return float("nan")
    mo = mo[:, :n_frames]
    mr = mr[:, :n_frames]
    # Per-frame cosine after mean-centering each frame (brightness-invariant-ish).
    sims: list[float] = []
    for i in range(n_frames):
        a = mo[:, i] - mo[:, i].mean()
        b = mr[:, i] - mr[:, i].mean()
        denom = float(np.sqrt(np.sum(a * a) * np.sum(b * b)))
        if denom < 1e-12:
            continue
        sims.append(float(np.sum(a * b) / denom))
    if not sims:
        return float("nan")
    return float(np.clip(np.mean(sims), 0.0, 1.0))


def rms_envelope(samples: np.ndarray, sample_rate: int = SR, win_s: float = 0.02) -> np.ndarray:
    win = max(1, int(win_s * sample_rate))
    kernel = np.ones(win, dtype=np.float64) / win
    return np.convolve(np.abs(samples), kernel, mode="same")


def envelope_similarity(ours: np.ndarray, ref: np.ndarray, sr: int = SR) -> float:
    """RMS-envelope Pearson + time-of-peak proximity on one aligned vworp."""
    ours_p, ref_p = _aligned_vworp_pair(ours, ref, sr)
    eo = rms_envelope(ours_p, sr)
    er = rms_envelope(ref_p, sr)
    shape = _corr_to_unit(_pearson(eo, er))
    peak_o = float(np.argmax(eo)) / sr
    peak_r = float(np.argmax(er)) / sr
    peak = _proximity(peak_o, peak_r, scale=0.35)
    if math.isnan(shape) and math.isnan(peak):
        return float("nan")
    if math.isnan(shape):
        return peak
    if math.isnan(peak):
        return shape
    return 0.7 * shape + 0.3 * peak


def dominant_peak_hz(
    samples: np.ndarray,
    sample_rate: int = SR,
    lo_hz: float = 80.0,
    hi_hz: float = 250.0,
) -> float:
    """Frequency of strongest magnitude peak in the fundamental band."""
    if len(samples) < 16:
        return float("nan")
    spec = np.abs(np.fft.rfft(samples * np.hanning(len(samples))))
    freqs = np.fft.rfftfreq(len(samples), 1.0 / sample_rate)
    mask = (freqs >= lo_hz) & (freqs <= hi_hz)
    if not np.any(mask):
        return float("nan")
    idx = int(np.argmax(spec[mask]))
    return float(freqs[mask][idx])


def peak_similarity(ours: np.ndarray, ref: np.ndarray, sr: int = SR) -> float:
    """How close the dominant ~80–250 Hz peak is to golden (identity of the scrape body)."""
    ours_p, ref_p = _aligned_vworp_pair(ours, ref, sr)
    return _proximity(dominant_peak_hz(ours_p, sr), dominant_peak_hz(ref_p, sr), scale=40.0)


def centroid_trajectory_similarity(
    ours: np.ndarray, ref: np.ndarray, sr: int = SR
) -> float:
    """
    Brightness-motion similarity: Pearson shape + absolute bloom-peak proximity.

    Shape alone was too generous when both rise mid-pulse but to different Hz.
    """
    ours_p, ref_p = _aligned_vworp_pair(ours, ref, sr)
    _, oc = stft_centroid_trajectory(ours_p, sr)
    _, rc = stft_centroid_trajectory(ref_p, sr)
    shape = _corr_to_unit(_pearson(oc, rc))
    bloom = _proximity(float(np.max(oc)), float(np.max(rc)), scale=250.0)
    if math.isnan(shape) and math.isnan(bloom):
        return float("nan")
    if math.isnan(shape):
        return bloom
    if math.isnan(bloom):
        return shape
    return 0.55 * shape + 0.45 * bloom


def similarity_score(ours: np.ndarray, ref: np.ndarray, sr: int = SR) -> SimilarityBreakdown:
    """
    Weighted 0–100 similarity vs golden (timbre + dynamics + coarse spectrum).

    Components (weights):
      mel 30%, envelope 20%, centroid traj/bloom 15%, bands 15%,
      crest 8%, fundamental peak 7%, period 5% (skipped on single-pulse clips).
    """
    o = spectral_report(ours, sr)
    r = spectral_report(ref, sr)

    band_keys = ("band_80_200", "band_200_500", "band_500_1500", "band_1500_4000")
    band_l1 = sum(abs(o[k] - r[k]) for k in band_keys)
    bands = float(np.clip(1.0 - band_l1 / 2.0, 0.0, 1.0))

    centroid = _proximity(o["centroid_hz"], r["centroid_hz"], scale=120.0)
    crest = _proximity(o["crest"], r["crest"], scale=2.0)

    # Period needs repeating structure; skip for solo ~1.75s cuts.
    min_loop = int(2.5 * sr)
    if len(ours) >= min_loop and len(ref) >= min_loop:
        op, _ = vworp_period_s(ours, sr)
        rp, _ = vworp_period_s(ref, sr)
        if math.isnan(rp):
            rp = EXPECTED_VWORP_S
        period = _proximity(op, rp, scale=0.25)
    else:
        period = float("nan")

    mel = mel_similarity(ours, ref, sr)
    envelope = envelope_similarity(ours, ref, sr)
    trajectory = centroid_trajectory_similarity(ours, ref, sr)
    peak = peak_similarity(ours, ref, sr)

    parts = {
        "mel": (mel, 0.30),
        "envelope": (envelope, 0.20),
        "trajectory": (trajectory, 0.15),
        "bands": (bands, 0.15),
        "crest": (crest, 0.08),
        "peak": (peak, 0.07),
        "period": (period, 0.05),
    }
    num = 0.0
    den = 0.0
    for val, weight in parts.values():
        if math.isnan(val):
            continue
        num += val * weight
        den += weight
    score = 100.0 * (num / den) if den > 0 else float("nan")

    def _finite(v: float) -> float:
        return 0.0 if math.isnan(v) else float(v)

    return SimilarityBreakdown(
        score=score,
        mel=_finite(mel),
        envelope=_finite(envelope),
        bands=_finite(bands),
        centroid=_finite(centroid),
        crest=_finite(crest),
        period=_finite(period),
        trajectory=_finite(trajectory),
        peak=_finite(peak),
    )


def similarity_detail(sim: SimilarityBreakdown) -> str:
    """Compact component string for CLI / metric notes."""
    per = f"{sim.period:.2f}" if sim.period > 0 else "n/a"
    return (
        f"mel={sim.mel:.2f} env={sim.envelope:.2f} traj={sim.trajectory:.2f} "
        f"bands={sim.bands:.2f} crest={sim.crest:.2f} peak={sim.peak:.2f} per={per}"
    )


def verdict_similarity(score: float) -> tuple[str, str]:
    if math.isnan(score):
        return "WARN", "similarity not measurable"
    if score < 55.0:
        return "FAIL", "far from golden"
    if score < 75.0:
        return "WARN", "moderately close to golden"
    return "OK", "close to golden"


def verdict_hiss(hf5: float, flat: float) -> tuple[str, str]:
    if hf5 > 0.002 or flat > 0.35:
        return "FAIL", "too noisy / hissy"
    if hf5 > 0.001 or flat > 0.25:
        return "WARN", "slight HF / flatness elevation"
    return "OK", "HF hiss under control"


def verdict_period(period: float) -> tuple[str, str]:
    if math.isnan(period):
        return "WARN", "period not measurable"
    err = abs(period - EXPECTED_VWORP_S) / EXPECTED_VWORP_S
    if err > 0.25:
        return "FAIL", f"vworp period off (want ~{EXPECTED_VWORP_S:.2f}s)"
    if err > 0.15:
        return "WARN", "vworp period slightly off"
    return "OK", "vworp period in range"


def verdict_centroid(c: float, ref_c: float) -> tuple[str, str]:
    if c < 300:
        return "FAIL", "too dark / bassy"
    if c > 750:
        return "FAIL", "too bright"
    if abs(c - ref_c) > 180:
        return "WARN", "centroid far from golden"
    return "OK", "brightness in band"


def verdict_peakiness(p: float) -> tuple[str, str]:
    # golden is strongly peaky; pure noise continuum is low
    if p < 0.15:
        return "FAIL", "too noise-like (not modal/metallic)"
    if p < 0.25:
        return "WARN", "less peaky than classic string partials"
    return "OK", "peaky / modal"


def build_metric_rows(ours: np.ndarray, ref: np.ndarray, sr: int = SR) -> list[MetricRow]:
    o = spectral_report(ours, sr)
    r = spectral_report(ref, sr)
    op, oac = vworp_period_s(ours, sr)
    rp, rac = vworp_period_s(ref, sr)
    ogap = pulse_gap_ratio(ours, sr)
    rgap = pulse_gap_ratio(ref, sr)
    sim = similarity_score(ours, ref, sr)

    rows: list[MetricRow] = []

    v, d = verdict_similarity(sim.score)
    detail = f"{d} ({similarity_detail(sim)})"
    rows.append(MetricRow("similarity", sim.score, 100.0, "/100", v, detail))

    v, d = verdict_hiss(o["hf_gt_5k"], o["flatness_2k_8k"])
    rows.append(MetricRow("hf>5k", o["hf_gt_5k"] * 100, r["hf_gt_5k"] * 100, "%", v, d))
    rows.append(
        MetricRow("flatness 2–8k", o["flatness_2k_8k"], r["flatness_2k_8k"], "", v, d)
    )

    v, d = verdict_period(op)
    rows.append(MetricRow("vworp period", op, rp if not math.isnan(rp) else EXPECTED_VWORP_S, "s", v, d))
    rows.append(MetricRow("period autocorr", oac, rac, "", "INFO", "higher = clearer periodicity"))

    v, d = verdict_centroid(o["centroid_hz"], r["centroid_hz"])
    rows.append(MetricRow("centroid", o["centroid_hz"], r["centroid_hz"], "Hz", v, d))

    v, d = verdict_peakiness(o["peakiness_80_800"])
    rows.append(MetricRow("peakiness 80–800", o["peakiness_80_800"], r["peakiness_80_800"], "", v, d))

    rows.append(MetricRow("band 80–200", o["band_80_200"] * 100, r["band_80_200"] * 100, "%", "INFO", ""))
    rows.append(MetricRow("band 200–500", o["band_200_500"] * 100, r["band_200_500"] * 100, "%", "INFO", ""))
    rows.append(MetricRow("band 500–1500", o["band_500_1500"] * 100, r["band_500_1500"] * 100, "%", "INFO", ""))
    rows.append(MetricRow("crest", o["crest"], r["crest"], "x", "INFO", "higher = punchier gaps"))
    rows.append(MetricRow("pulse head/tail RMS", ogap, rgap, "x", "INFO", "higher = clearer vworp gaps"))
    return rows


def hard_gate_failures(ours: np.ndarray, ref: np.ndarray, sr: int = SR) -> list[str]:
    """Strict gates used by the generator --validate-ref path."""
    o = spectral_report(ours, sr)
    r = spectral_report(ref, sr)
    period, _ = vworp_period_s(ours, sr)
    failures: list[str] = []
    if o["hf_gt_5k"] > 0.002:
        failures.append(f"hiss: hf>5k={o['hf_gt_5k']*100:.2f}% (ref {r['hf_gt_5k']*100:.2f}%, want <0.2%)")
    if o["hf_gt_3k"] > 0.04:
        failures.append(f"hiss: hf>3k={o['hf_gt_3k']*100:.2f}% (ref {r['hf_gt_3k']*100:.2f}%, want <4%)")
    if not (320.0 <= o["centroid_hz"] <= 650.0):
        failures.append(f"centroid={o['centroid_hz']:.0f}Hz (ref {r['centroid_hz']:.0f}Hz, want 320–650)")
    if o["band_80_200"] < 0.22:
        failures.append(
            f"band 80–200Hz={o['band_80_200']*100:.1f}% (ref {r['band_80_200']*100:.1f}%, want ≥22%)"
        )
    if o["band_500_1500"] < 0.10:
        failures.append(
            f"band 500–1500Hz={o['band_500_1500']*100:.1f}% (ref {r['band_500_1500']*100:.1f}%, want ≥10%)"
        )
    if not math.isnan(period):
        err = abs(period - EXPECTED_VWORP_S) / EXPECTED_VWORP_S
        if err > 0.25:
            failures.append(f"vworp period={period:.2f}s (want ~{EXPECTED_VWORP_S:.2f}s ±25%)")
    return failures


def format_metric_table(rows: list[MetricRow]) -> str:
    lines = [
        "| metric | ours | golden | verdict | note |",
        "|---|---:|---:|---|---|",
    ]
    for row in rows:
        ours = _fmt(row.ours, row.unit)
        ref = _fmt(row.ref, row.unit)
        lines.append(f"| {row.name} | {ours} | {ref} | {row.verdict} | {row.detail} |")
    return "\n".join(lines)


def _fmt(v: float, unit: str) -> str:
    if v is None or (isinstance(v, float) and math.isnan(v)):
        return "n/a"
    if unit == "%":
        return f"{v:.2f}%"
    if unit == "/100":
        return f"{v:.1f}"
    if unit == "Hz":
        return f"{v:.0f} Hz"
    if unit == "s":
        return f"{v:.2f}s"
    if unit == "x":
        return f"{v:.2f}x"
    return f"{v:.3f}"


def write_compare_plots(
    ours: np.ndarray,
    ref: np.ndarray,
    out_dir: Path,
    sr: int = SR,
    label: str = "demat",
) -> list[Path]:
    """Write envelope + spectrogram + centroid trajectory PNGs. Returns paths."""
    import matplotlib

    matplotlib.use("Agg")
    import matplotlib.pyplot as plt

    out_dir.mkdir(parents=True, exist_ok=True)
    paths: list[Path] = []

    ours_p = peak_normalize(one_vworp_slice(ours, sr))
    ref_p = peak_normalize(one_vworp_slice(ref_analysis_slice(ref, sr), sr, start_s=2.0))
    # match lengths
    n = min(len(ours_p), len(ref_p))
    ours_p, ref_p = ours_p[:n], ref_p[:n]
    t = np.arange(n) / sr

    # Envelope
    fig, axes = plt.subplots(2, 1, figsize=(10, 5), sharex=True)
    win = max(1, int(0.02 * sr))
    kernel = np.ones(win) / win
    axes[0].plot(t, np.convolve(np.abs(ref_p), kernel, mode="same"), color="#1f4e79", label="golden")
    axes[0].set_ylabel("env")
    axes[0].set_title(f"{label}: RMS envelope (1 vworp, peak-normalised)")
    axes[0].legend(loc="upper right")
    axes[1].plot(t, np.convolve(np.abs(ours_p), kernel, mode="same"), color="#b85c38", label="ours")
    axes[1].set_xlabel("time (s)")
    axes[1].set_ylabel("env")
    axes[1].legend(loc="upper right")
    fig.tight_layout()
    p = out_dir / f"{label}_envelope.png"
    fig.savefig(p, dpi=120)
    plt.close(fig)
    paths.append(p)

    # Spectrograms 0–4 kHz
    fig, axes = plt.subplots(2, 1, figsize=(10, 6), sharex=True)
    for ax, sig, title, cmap in (
        (axes[0], ref_p, "golden", "magma"),
        (axes[1], ours_p, "ours", "magma"),
    ):
        ax.specgram(sig, NFFT=1024, Fs=sr, noverlap=768, cmap=cmap)
        ax.set_ylim(0, 4000)
        ax.set_ylabel("Hz")
        ax.set_title(f"{label}: spectrogram — {title} (0–4 kHz)")
    axes[1].set_xlabel("time (s)")
    fig.tight_layout()
    p = out_dir / f"{label}_spectrogram.png"
    fig.savefig(p, dpi=120)
    plt.close(fig)
    paths.append(p)

    # Centroid trajectory
    tr_t, tr_c = stft_centroid_trajectory(ref_p, sr)
    to_t, to_c = stft_centroid_trajectory(ours_p, sr)
    fig, ax = plt.subplots(figsize=(10, 3.5))
    ax.plot(tr_t, tr_c, color="#1f4e79", label="golden")
    ax.plot(to_t, to_c, color="#b85c38", label="ours")
    ax.set_xlabel("time (s)")
    ax.set_ylabel("centroid (Hz)")
    ax.set_title(f"{label}: spectral centroid over one vworp")
    ax.legend(loc="upper right")
    fig.tight_layout()
    p = out_dir / f"{label}_centroid.png"
    fig.savefig(p, dpi=120)
    plt.close(fig)
    paths.append(p)

    # Band bars
    o = spectral_report(ours_p, sr)
    r = spectral_report(ref_p, sr)
    bands = ["80–200", "200–500", "500–1500", "1500–4000"]
    keys = ["band_80_200", "band_200_500", "band_500_1500", "band_1500_4000"]
    x = np.arange(len(bands))
    fig, ax = plt.subplots(figsize=(8, 3.5))
    ax.bar(x - 0.18, [r[k] * 100 for k in keys], width=0.36, label="golden", color="#1f4e79")
    ax.bar(x + 0.18, [o[k] * 100 for k in keys], width=0.36, label="ours", color="#b85c38")
    ax.set_xticks(x)
    ax.set_xticklabels(bands)
    ax.set_ylabel("% energy")
    ax.set_title(f"{label}: band energy share")
    ax.legend(loc="upper right")
    fig.tight_layout()
    p = out_dir / f"{label}_bands.png"
    fig.savefig(p, dpi=120)
    plt.close(fig)
    paths.append(p)

    return paths


def write_ab_wav(ours: np.ndarray, ref: np.ndarray, path: Path, sr: int = SR) -> Path:
    """Loudness-matched A/B: ours → silence → golden → silence → ours."""
    ours_p = peak_normalize(one_vworp_slice(ours, sr))
    ref_p = peak_normalize(one_vworp_slice(ref_analysis_slice(ref, sr), sr, start_s=2.0))
    n = min(len(ours_p), len(ref_p))
    ours_p, ref_p = ours_p[:n], ref_p[:n]
    gap = np.zeros(int(0.35 * sr))
    seq = np.concatenate([ours_p, gap, ref_p, gap, ours_p])
    path.parent.mkdir(parents=True, exist_ok=True)
    mono = np.clip(seq, -1.0, 1.0)
    pcm = (mono * 32767.0).astype(np.int16)
    stereo = np.empty(pcm.size * 2, dtype=np.int16)
    stereo[0::2] = pcm
    stereo[1::2] = pcm
    with wave.open(str(path), "wb") as wf:
        wf.setnchannels(2)
        wf.setsampwidth(2)
        wf.setframerate(sr)
        wf.writeframes(stereo.tobytes())
    return path


def write_markdown_report(
    rows: list[MetricRow],
    plot_paths: list[Path],
    out_path: Path,
    *,
    ours_path: Path,
    ref_path: Path,
) -> Path:
    fails = [r for r in rows if r.verdict == "FAIL"]
    warns = [r for r in rows if r.verdict == "WARN"]
    sim_rows = [r for r in rows if r.name == "similarity"]
    sim_line = (
        f", similarity **{sim_rows[0].ours:.1f}/100**"
        if sim_rows and not math.isnan(sim_rows[0].ours)
        else ""
    )
    lines = [
        "# TARDIS SFX compare",
        "",
        f"- ours: `{ours_path}`",
        f"- golden: `{ref_path}` (analysis only — not packaged)",
        f"- summary: **{len(fails)} FAIL**, **{len(warns)} WARN**{sim_line}",
        "",
        "## Metrics",
        "",
        format_metric_table(rows),
        "",
        "## Plots",
        "",
    ]
    for p in plot_paths:
        lines.append(f"- `{p.name}`")
        lines.append("")
        lines.append(f"![{p.name}]({p.name})")
        lines.append("")
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return out_path
