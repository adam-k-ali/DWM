# DWM Release Policy

See also: [Docs Index](./index.md), [Branding Guidelines](./branding-guidelines.md)

This document defines when we cut a release, what “enough” means, how versions are named, and the distribution checklist (GitHub, Modrinth, Discord).

## Cadence (hybrid)

- Aim for a **player-facing release every 2–4 weeks** when there is enough value since the last published tag.
- Ship **hotfixes anytime** for severe issues (see below).
- Do not cut a calendar release with no player-facing value.

## Versioning

Public release id and git tag:

```text
v{minecraft_version}-{mod_version}
```

Example: `v1.21.4-1.2.0` → Minecraft `1.21.4`, mod `1.2.0`.

| Field | Source |
| --- | --- |
| `minecraft_version` | [`gradle.properties`](../gradle.properties) |
| `mod_version` | [`gradle.properties`](../gradle.properties) (SemVer piece only) |
| Player changelog + promos | [`version.json`](../version.json) (source for GitHub Release notes) |

### SemVer meaning for `mod_version`

- **patch** — bugfixes / polish; no intentional behavior change players rely on
- **minor** — new player-facing features or content families (default for hybrid drops)
- **major** — breaking world/save/API changes or a deliberate reset

## What is “enough” for a scheduled release

Cut a hybrid release when **any** of these landed since the last published tag **and** the quality bar passes:

- New **stable** player-facing feature or interaction
- New/expanded **content family** players can use in survival
- Gameplay-impacting bugfix (crashes, desync, travel/door/console breakage)
- Minecraft / dependency support bump that players need

**Not enough alone** (hold for the next hybrid window unless bundled with the above):

- Docs, CI, refactors, tooling
- Experimental / config-gated-only work that stays disabled
- Dependency bumps with no player-visible effect

### Hotfix anytime

Ship a patch immediately for:

- Crash on load
- Data loss
- Soft-lock
- Severe multiplayer desync

## Quality bar before cut

- `./gradlew test` and `./gradlew build` green (includes `checkVersionSync`)
- Player-facing notes filled in for the new entry under `version.json` → `{minecraft_version}` → `{mod_version}`
- `promos.latest` and `promos.recommended` match `{minecraft_version}-{mod_version}` (use `./gradlew syncVersionJson` at cut time)
- Experimental features remain clearly labeled in docs and configs
- Release notes describe **shipped behavior only** (no roadmap fluff)

### Between releases

- Keep `mod_version` and `version.json` promos on the **last published** release until you intentionally cut the next one.
- Do not bump promos on routine `main` merges; the release workflow publishes a GitHub Release only on version tags.

## Source of truth

- **[`version.json`](../version.json)** is the only release-notes and promo channel (GitHub Release body).
- Do not maintain a separate changelog file; dual ledgers drift.

## Distribution checklist

1. On `main`, bump `mod_version` in `gradle.properties` (and `minecraft_version` if needed).
2. Run `./gradlew syncVersionJson`, then fill `added` / `changed` / `removed` for the new version in `version.json`.
3. Confirm `./gradlew build` is green.
4. Commit, merge to `main`, tag `v{minecraft_version}-{mod_version}`, and push the tag.
5. Confirm the **Release** GitHub Actions workflow succeeds:
   - GitHub Release with remapped JAR (+ sources JAR)
   - Release notes generated from `version.json`
6. Upload the remapped JAR from the GitHub Release to **Modrinth** (correct Minecraft + Fabric loaders).
7. Post in Discord **`#releases`**: short summary of shipped behavior + Modrinth link.

Modrinth upload and the Discord `#releases` post remain **manual** in this policy. Automating them (Modrinth token / Discord webhook) is a follow-up.

## CI overview

| Workflow | Trigger | Purpose |
| --- | --- | --- |
| [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) | `pull_request`, push to `main` | `./gradlew build` (compile + unit tests + version sync check) |
| [`.github/workflows/release.yml`](../.github/workflows/release.yml) | push of tags `v*` | Build, publish GitHub Release artifacts and notes from `version.json` |

CircleCI is retired; do not add draft GitHub releases on every `main` merge.
