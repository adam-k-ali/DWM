"""Unit tests for the Dalek voice effects chain (no flite or ffmpeg needed).

Run: (cd dwm/tools/sfx && poetry run python -m unittest discover -s tests)
"""

from __future__ import annotations

import unittest

import numpy as np

from dwm_sfx.generate_dalek_sfx import SR, VOICE_BAND_HZ, dalek_voice_fx, trim_silence


def _tone(freq: float, seconds: float, amp: float = 0.5) -> np.ndarray:
    t = np.arange(int(seconds * SR)) / SR
    return amp * np.sin(2.0 * np.pi * freq * t)


def _band_energy_ratio(signal: np.ndarray, low: float, high: float) -> float:
    spectrum = np.abs(np.fft.rfft(signal)) ** 2
    freqs = np.fft.rfftfreq(len(signal), 1.0 / SR)
    in_band = spectrum[(freqs >= low) & (freqs <= high)].sum()
    return float(in_band / spectrum.sum())


class TrimSilenceTest(unittest.TestCase):
    def test_cuts_leading_and_trailing_silence(self) -> None:
        body = _tone(300.0, 0.2)
        padded = np.concatenate([np.zeros(SR // 4), body, np.zeros(SR // 4)])
        trimmed = trim_silence(padded)
        self.assertLess(len(trimmed), len(padded))
        self.assertGreaterEqual(len(trimmed), len(body))

    def test_all_silence_is_returned_unchanged(self) -> None:
        silent = np.zeros(1000)
        self.assertEqual(len(trim_silence(silent)), 1000)


class DalekVoiceFxTest(unittest.TestCase):
    def test_output_peak_is_bounded(self) -> None:
        out = dalek_voice_fx(_tone(220.0, 0.5, amp=0.9))
        self.assertGreater(len(out), 0)
        self.assertLessEqual(float(np.max(np.abs(out))), 1.0)
        self.assertGreater(float(np.max(np.abs(out))), 0.3)

    def test_energy_moves_into_voice_band(self) -> None:
        low_rumble = _tone(60.0, 0.5) + _tone(1000.0, 0.5, amp=0.1)
        before = _band_energy_ratio(low_rumble, *VOICE_BAND_HZ)
        after = _band_energy_ratio(dalek_voice_fx(low_rumble), *VOICE_BAND_HZ)
        self.assertGreater(after, before)

    def test_is_deterministic(self) -> None:
        source = _tone(180.0, 0.4)
        np.testing.assert_array_equal(dalek_voice_fx(source), dalek_voice_fx(source))

    def test_tiny_input_does_not_crash(self) -> None:
        self.assertEqual(len(dalek_voice_fx(np.zeros(3))), 3)


if __name__ == "__main__":
    unittest.main()
