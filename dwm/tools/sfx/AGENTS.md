# AGENTS.md

## Scope
This file applies to `dwm/tools/sfx/` — offline TARDIS travel SFX and related entity SFX generation/analysis. Not invoked by Gradle or CI.

## Local Context
Scripts synthesize and validate `.ogg` travel loops from spectral targets. Generated game assets are written into `dwm/src/client/resources/assets/dwm/sounds/` (or paths documented in each script). Reference audio for analysis stays local and gitignored under `fixtures/`.

## Commands (from repo root)

```bash
poetry -C dwm/tools/sfx install
poetry -C dwm/tools/sfx run fetch-tardis-ref
poetry -C dwm/tools/sfx run generate-tardis-travel-sfx
poetry -C dwm/tools/sfx run compare-tardis-sfx
poetry -C dwm/tools/sfx run generate-flutterwing-sfx
poetry -C dwm/tools/sfx run generate-mewing-dog-sfx
poetry -C dwm/tools/sfx run generate-dalek-sfx
(cd dwm/tools/sfx && poetry run python -m unittest discover -s tests)
```

See `fixtures/README.md` for validate/compare report options.

## Conventions
- Install with Poetry (`virtualenvs.in-project = true` → `sfx/.venv/`).
- Package import root is `dwm_sfx`.
- `fixtures/baked_vworp_targets.npz` is committed (analysis targets); WAV/MP3 goldens under `fixtures/` are **not** committed.
- After regenerating OGGs, smoke in-game or rely on existing sound-related tests; there is no automated audio gate in `./dwm/gradlew build`.
- Do not redistribute downloaded reference clips — analysis/local dev only.

## Common Pitfalls
- Do not commit `sfx/.venv/` or fetched `tardis_ref.wav`.
- Matplotlib/compare outputs under `fixtures/compare_out/` are local reports — commit only if intentionally updating checked-in fixtures.
- Python version may differ from Java 25; the venv is independent of the Gradle toolchain.
- Requires `ffmpeg` on `PATH` for WAV→OGG and reference fetch.
