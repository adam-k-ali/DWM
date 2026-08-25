# Local TARDIS SFX fixtures (analysis only)

This directory holds a **local golden reference** used to compare against
generated travel loops. WAV/MP3 goldens are **not** packaged into the mod and
must **not** be committed (see root `.gitignore`).

`baked_vworp_targets.npz` is committed analysis-derived spectral/envelope
targets used by `generate_tardis_travel_sfx.py` (not reference audio).

## Fetch the golden

```bash
tools/.venv/bin/python tools/fetch_tardis_ref.py
```

This downloads a publicly posted materialisation clip for analysis and writes
`tools/fixtures/tardis_ref.wav`. Do not redistribute that file with the mod.

## Compare generated vs golden

```bash
tools/.venv/bin/python tools/generate_tardis_travel_sfx.py
tools/.venv/bin/python tools/compare_tardis_sfx.py
```

The compare report includes a **similarity** score (0–100) aggregating log-mel
timbre, RMS envelope shape, centroid trajectory/bloom, band energy, crest,
fundamental peak, and (on loops) vworp period.

Or after generate with a report:

```bash
tools/.venv/bin/python tools/generate_tardis_travel_sfx.py \
  --validate-ref tools/fixtures/tardis_ref.wav \
  --compare-report tools/fixtures/compare_out
```

Outputs land in `tools/fixtures/compare_out/` (markdown + PNGs). Optional A/B:

```bash
tools/.venv/bin/python tools/compare_tardis_sfx.py --play-ab
```
