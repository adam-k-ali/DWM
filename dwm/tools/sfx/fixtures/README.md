# Local TARDIS SFX fixtures (analysis only)

This directory holds a **local golden reference** used to compare against
generated travel loops. WAV/MP3 goldens are **not** packaged into the mod and
must **not** be committed (see root `.gitignore`).

`baked_vworp_targets.npz` is committed analysis-derived spectral/envelope
targets used by `generate-tardis-travel-sfx` (not reference audio).

## Fetch the golden

```bash
poetry -C dwm/tools/sfx run fetch-tardis-ref
```

This downloads a publicly posted materialisation clip for analysis and writes
`fixtures/tardis_ref.wav`. Do not redistribute that file with the mod.

## Compare generated vs golden

```bash
poetry -C dwm/tools/sfx run generate-tardis-travel-sfx
poetry -C dwm/tools/sfx run compare-tardis-sfx
```

The compare report includes a **similarity** score (0–100) aggregating log-mel
timbre, RMS envelope shape, centroid trajectory/bloom, band energy, crest,
fundamental peak, and (on loops) vworp period.

Or after generate with a report:

```bash
poetry -C dwm/tools/sfx run generate-tardis-travel-sfx \
  --validate-ref dwm/tools/sfx/fixtures/tardis_ref.wav \
  --compare-report dwm/tools/sfx/fixtures/compare_out
```

Outputs land in `fixtures/compare_out/` (markdown + PNGs). Optional A/B:

```bash
poetry -C dwm/tools/sfx run compare-tardis-sfx --play-ab
```
