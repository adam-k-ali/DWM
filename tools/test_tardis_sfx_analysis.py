#!/usr/bin/env python3
"""Unit tests for TARDIS SFX analysis helpers.

Run: tools/.venv/bin/python tools/test_tardis_sfx_analysis.py
"""

from __future__ import annotations

import math
import sys
import unittest
from pathlib import Path

import numpy as np

TOOLS = Path(__file__).resolve().parent
if str(TOOLS) not in sys.path:
    sys.path.insert(0, str(TOOLS))

from tardis_sfx_analysis import (  # noqa: E402
    SR,
    EXPECTED_VWORP_S,
    _proximity,
    envelope_similarity,
    mel_similarity,
    similarity_score,
    verdict_similarity,
)


def _tone_pulse(f0_start: float, f0_end: float, n: int, bright_late: bool = True) -> np.ndarray:
    """Simple swept tone with optional late HF partial — deterministic fixture."""
    f0 = np.geomspace(f0_start, f0_end, n)
    phase = 2.0 * np.pi * np.cumsum(f0) / SR
    sig = np.sin(phase)
    if bright_late:
        bloom = np.exp(-((np.linspace(0.0, 1.0, n) - 0.7) ** 2) / (2 * 0.12**2))
        hi = np.sin(2.0 * np.pi * np.cumsum(f0 * 8.0) / SR)
        sig = sig * (1.0 - 0.45 * bloom) + 0.55 * hi * bloom
    env = 0.4 + 0.6 * (np.linspace(0.0, 1.0, n) ** 0.8)
    env[: int(0.1 * SR)] *= np.linspace(0.0, 1.0, int(0.1 * SR))
    env[-int(0.25 * SR) :] *= np.linspace(1.0, 0.05, int(0.25 * SR))
    out = sig * env
    return out / (np.max(np.abs(out)) or 1.0)


def _two_pulse_loop(pulse: np.ndarray) -> np.ndarray:
    return np.concatenate([pulse, pulse])


class SimilarityScoreTests(unittest.TestCase):
    def test_proximity_identity_and_decay(self) -> None:
        self.assertAlmostEqual(_proximity(100.0, 100.0, 50.0), 1.0)
        self.assertLess(_proximity(100.0, 150.0, 50.0), 0.5)
        self.assertTrue(math.isnan(_proximity(float("nan"), 1.0, 1.0)))

    def test_identical_signals_score_near_100(self) -> None:
        n = int(EXPECTED_VWORP_S * SR)
        pulse = _tone_pulse(200.0, 95.0, n)
        loop = _two_pulse_loop(pulse)
        sim = similarity_score(loop, loop, SR)
        self.assertGreaterEqual(sim.score, 97.0)
        self.assertAlmostEqual(sim.bands, 1.0, places=5)
        self.assertAlmostEqual(sim.mel, 1.0, places=5)
        self.assertGreaterEqual(sim.envelope, 0.99)

    def test_mel_and_envelope_catch_timbre_mismatch(self) -> None:
        n = int(EXPECTED_VWORP_S * SR)
        ref = _tone_pulse(200.0, 95.0, n, bright_late=True)
        # Same envelope-ish length but totally different spectrum.
        flat = np.sin(2.0 * np.pi * 1200.0 * np.arange(n) / SR)
        flat = flat / (np.max(np.abs(flat)) or 1.0)
        self.assertLess(mel_similarity(flat, ref, SR), 0.55)
        # Flat tone with no build/release should diverge on envelope too.
        self.assertLess(envelope_similarity(flat, ref, SR), 0.85)

    def test_dissimilar_signals_score_lower(self) -> None:
        n = int(EXPECTED_VWORP_S * SR)
        ref = _two_pulse_loop(_tone_pulse(200.0, 95.0, n, bright_late=True))
        flat = np.sin(2.0 * np.pi * 1200.0 * np.arange(len(ref)) / SR)
        flat = flat / (np.max(np.abs(flat)) or 1.0)
        sim = similarity_score(flat, ref, SR)
        # Mel-weighted score should land well below the old coarse-only ~70s.
        self.assertLess(sim.score, 55.0)
        self.assertLess(sim.mel, 0.55)

    def test_verdict_thresholds(self) -> None:
        self.assertEqual(verdict_similarity(90.0)[0], "OK")
        self.assertEqual(verdict_similarity(60.0)[0], "WARN")
        self.assertEqual(verdict_similarity(40.0)[0], "FAIL")


if __name__ == "__main__":
    unittest.main()
