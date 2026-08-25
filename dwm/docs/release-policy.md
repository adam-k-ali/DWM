# DWM Release Policy

See also: [Docs Index](./index.md), [Branding Guidelines](./branding-guidelines.md)

This document defines when we cut a release, what “enough” means, how versions are named, and the distribution checklist (GitHub, Modrinth, CurseForge, Discord).

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
| Player changelog + promos | [`version.json`](../version.json) (source for GitHub Release notes, Modrinth/CurseForge changelogs, and Discord) |

Each per-version entry under `version.json` → `{minecraft_version}` → `{mod_version}` includes:

| Field | Purpose |
| --- | --- |
| `summary` | Short player-facing blurb (required, non-blank). Leads the GitHub Release body and Modrinth/CurseForge changelogs; used as the Discord embed description. |
| `added` / `changed` / `removed` | Detailed changelog bullets for GitHub, Modrinth, and CurseForge. |

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

- `./dwm/gradlew test` and `./dwm/gradlew build` green (includes `checkVersionSync`)
- Player-facing notes filled in for the new entry under `version.json` → `{minecraft_version}` → `{mod_version}`: non-blank `summary` plus `added` / `changed` / `removed` as needed
- `promos.latest` and `promos.recommended` match `{minecraft_version}-{mod_version}` (use `./dwm/gradlew syncVersionJson` at cut time)
- Experimental features remain clearly labeled in docs and configs
- Release notes describe **shipped behavior only** (no roadmap fluff)

### Between releases

- Keep `mod_version` and `version.json` promos on the **last published** release until you intentionally cut the next one.
- Do not bump promos on routine `main` merges; the release workflow publishes a GitHub Release only on version tags.

## Source of truth

- **[`version.json`](../version.json)** is the only release-notes and promo channel (GitHub Release body, Modrinth/CurseForge changelogs, Discord summary).
- Do not maintain a separate changelog file; dual ledgers drift.
- **[`metadata/`](../metadata/)** is the source of truth for the Modrinth **project listing** (`description`, long-form `body`, categories, Discord URL). Edit those files, then run the **Sync Modrinth Project** workflow; listing updates are manual and are not part of the tag Release workflow. CurseForge has no official listing PATCH API — update the CurseForge project page description and categories by hand when they change.

## Distribution checklist

1. On `main`, bump `mod_version` in `gradle.properties` (and `minecraft_version` if needed).
2. Run `./dwm/gradlew syncVersionJson`, then fill `summary` and `added` / `changed` / `removed` for the new version in `version.json`.
3. Confirm `./dwm/gradlew build` is green.
4. Commit, merge to `main`, then create and push tag `v{minecraft_version}-{mod_version}` (manually, or via the **Create Release Tag** workflow, which tags `promos.latest` from `version.json` on `main` if that tag is missing).
5. Confirm the **Release** GitHub Actions workflow succeeds:
   - GitHub Release with remapped JAR (+ sources JAR) and notes from `version.json` (`summary` + detailed lists)
   - Modrinth version upload (Fabric + Minecraft from the tag; Fabric API dependency)
   - CurseForge file upload (project `355957`; Fabric + Client/Server + Minecraft from the tag; Fabric API required dependency)
   - Discord `#releases` embed (summary + Modrinth and CurseForge links)

### Required GitHub Actions secrets

| Secret | Purpose |
| --- | --- |
| `MODRINTH_TOKEN` | Modrinth personal access token with `VERSION_CREATE` (Release) and `PROJECT_WRITE` (Sync Modrinth Project) |
| `CURSEFORGE_TOKEN` | CurseForge authors upload token ([API Tokens](https://console.curseforge.com/#/api-tokens)); account must be able to upload to project `355957` |
| `DISCORD_WEBHOOK_URL` | Incoming webhook for Discord `#releases` |

## CI overview

| Workflow | Trigger | Purpose |
| --- | --- | --- |
| [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml) | `pull_request`, push to `main` | `./dwm/gradlew build` and `./screenplay/gradlew build` (compile + unit tests + version sync check) |
| [`.github/workflows/create-release-tag.yml`](../../.github/workflows/create-release-tag.yml) | `workflow_dispatch` | Create and push `v*` tag from `version.json` `promos.latest` if missing; then dispatch Release |
| [`.github/workflows/release.yml`](../../.github/workflows/release.yml) | push of tags `v*`, or `workflow_dispatch` | Build; publish GitHub Release, Modrinth version, CurseForge file, and Discord announcement from `version.json` |
| [`.github/workflows/sync-modrinth-project.yml`](../../.github/workflows/sync-modrinth-project.yml) | `workflow_dispatch` | PATCH Modrinth project listing from [`metadata/`](../metadata/) (Modrinth only; CurseForge listing stays manual) |

CircleCI is retired; do not add draft GitHub releases on every `main` merge.

## Screenplay releases

Screenplay (Modrinth project `RdazTKdM`, slug `screenplay`) is published separately from DWM. Do not use DWM `v*` tags for Screenplay.

Public release id and git tag:

```text
screenplay-v{screenplay_version}
```

Example: `screenplay-v1.0.0+26.2` → Gradle `screenplay_version` `1.0.0+26.2` (Minecraft from `minecraft_version` / the `+26.2` suffix).

| Field | Source |
| --- | --- |
| `screenplay_version` | [`screenplay/gradle.properties`](../../screenplay/gradle.properties) |
| Changelog + promos | [`screenplay/metadata/version.json`](../../screenplay/metadata/version.json) |
| Listing drafts | [`screenplay/metadata/`](../../screenplay/metadata/) (`modrinth.json` + `modrinth-body.md`; no `discord_url`) |

### Screenplay distribution checklist

1. On `main`, bump `screenplay_version` in `screenplay/gradle.properties` (and `screenplay/loaders/gradle.properties` when needed). Keep [`gradle/libs.versions.toml`](../../gradle/libs.versions.toml) aligned.
2. Fill `summary` and `added` / `changed` / `removed` for the new version in `screenplay/metadata/version.json`, and set `promos.latest` / `promos.recommended`.
3. Confirm `./screenplay/gradlew build` and `./screenplay/gradlew -p loaders :forge:build :neoforge:build` are green.
4. Commit, merge to `main`, then create and push tag `screenplay-v{screenplay_version}` (manually, or via **Create Screenplay Release Tag**).
5. Confirm **Release Screenplay** succeeds:
   - GitHub Release with Fabric, Forge, and NeoForge jars and notes from `screenplay/metadata/version.json`
   - Modrinth multi-loader version upload to project `RdazTKdM` (Fabric API optional dependency)
6. Optionally run **Sync Modrinth Screenplay** to PATCH the listing from `screenplay/metadata/` (Discord stays blank).

Uses the same `MODRINTH_TOKEN` secret as DWM (`VERSION_CREATE` for releases, `PROJECT_WRITE` for listing sync). No CurseForge or Discord announce for Screenplay.

| Workflow | Trigger | Purpose |
| --- | --- | --- |
| [`.github/workflows/create-screenplay-release-tag.yml`](../../.github/workflows/create-screenplay-release-tag.yml) | `workflow_dispatch` | Create and push `screenplay-v*` from `screenplay/metadata/version.json` `promos.latest` if missing; then dispatch Release Screenplay |
| [`.github/workflows/release-screenplay.yml`](../../.github/workflows/release-screenplay.yml) | push of tags `screenplay-v*`, or `workflow_dispatch` | Build loader jars; publish GitHub Release and Modrinth version |
| [`.github/workflows/sync-modrinth-screenplay.yml`](../../.github/workflows/sync-modrinth-screenplay.yml) | `workflow_dispatch` | PATCH Modrinth Screenplay listing from [`screenplay/metadata/`](../../screenplay/metadata/) |
