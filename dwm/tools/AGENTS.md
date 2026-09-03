# AGENTS.md

## Scope
This file applies to `dwm/tools/` — offline Python scripts for TARDIS travel SFX and related analysis. These are **not** invoked by Gradle or CI.

## Local Context
Scripts synthesize and validate `.ogg` travel loops from spectral targets. Generated game assets are written into `src/client/resources/assets/dwm/sounds/` (or paths documented in each script). Reference audio for analysis stays local and gitignored.

## Commands
- Create venv (once): `python3 -m venv tools/.venv && tools/.venv/bin/pip install -r tools/requirements.txt`
- Fetch local golden reference (analysis only, gitignored): `tools/.venv/bin/python tools/fetch_tardis_ref.py`
- Generate travel SFX: `tools/.venv/bin/python tools/generate_tardis_travel_sfx.py`
- Compare against golden: `tools/.venv/bin/python tools/compare_tardis_sfx.py`
- Flutterwing SFX: `tools/.venv/bin/python tools/generate_flutterwing_sfx.py`
- Mewing Dog SFX: `tools/.venv/bin/python tools/generate_mewing_dog_sfx.py`
- Dalek SFX: `tools/.venv/bin/python tools/generate_dalek_sfx.py`

See `tools/fixtures/README.md` for validate/compare report options.

## Conventions
- Run scripts from the **repo root** unless a script documents otherwise.
- `tools/fixtures/baked_vworp_targets.npz` is committed (analysis targets); WAV/MP3 goldens under `tools/fixtures/` are **not** committed.
- After regenerating OGGs, smoke in-game or rely on existing sound-related tests; there is no automated audio gate in `./dwm/gradlew build`.
- Do not redistribute downloaded reference clips — analysis/local dev only.

## Common Pitfalls
- Do not commit `tools/.venv/` or fetched `tardis_ref.wav`.
- Matplotlib/compare outputs under `tools/fixtures/compare_out/` are local reports — commit only if intentionally updating checked-in fixtures.
- Python version may differ from Java 25; the venv is independent of the Gradle toolchain.
