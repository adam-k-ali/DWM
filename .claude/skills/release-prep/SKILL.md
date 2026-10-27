---
name: release-prep
description: >-
  Prepare release metadata for a new DWM mod version. Use when asked to cut or
  prepare a release, bump mod_version, write release notes, or update
  version.json / Modrinth metadata: bumps gradle.properties, syncs promos via
  Gradle, fills the changelog entry from shipped changes, verifies, and opens a
  PR. Never tags, pushes tags, or triggers release workflows.
---

# Release Prep (DWM)

## Overview

Prepare the metadata PR for a release, following [dwm/docs/release-policy.md](../../../dwm/docs/release-policy.md) (read it first; this skill does not duplicate it).

**Files touched** (the last release, `chore(dwm): cut v26.2-1.7.0 release notes`, changed exactly these):

| File | Change |
| --- | --- |
| `dwm/gradle.properties` | `mod_version` (and `minecraft_version` only if the user asks) |
| `dwm/version.json` | new `{minecraft_version}` → `{mod_version}` entry + promos |
| `dwm/metadata/modrinth.json`, `dwm/metadata/modrinth-body.md` | only if headline features changed |

**Out of scope:** creating/pushing the `v{mc}-{mod}` tag, dispatching **Create Release Tag** / **Release** / **Sync Modrinth Project** workflows. Do those only on explicit instruction.

## Workflow

1. **Establish the release**
   - Read `minecraft_version` and `mod_version` from `dwm/gradle.properties`; list the last tag with `git tag --sort=-v:refname | head -3` (format `v{mc}-{mod}`).
   - Confirm the target version with the user if not given. Suggest the SemVer bump from the policy: patch = fixes/polish, minor = new player-facing features or content (default), major = breaking world/save/API.
   - Require a clean working tree. Branch from up-to-date `main` as `chore/version-{mod}-release-notes`.

2. **Check the release is warranted** against the policy's "enough" list. Docs/CI/refactor/tooling-only or config-gated-off work is not enough; hotfix exceptions (crash on load, data loss, soft-lock, severe desync) release anytime. Flag it to the user if the bar is not met.

3. **Gather shipped changes**
   - `git log <last-tag>..HEAD --oneline --merges` and `git log <last-tag>..HEAD --oneline` for the detail.
   - Cross-check merged PRs: `gh pr list --repo adam-k-ali/DWM --state merged --search "merged:>=<last-tag-date>"`.
   - Keep only **player-visible, shipped** behaviour. Skip experimental/disabled features and roadmap talk. When a commit's effect is unclear, read the code/docs under `dwm/docs/` rather than guessing.

4. **Bump** `mod_version` in `dwm/gradle.properties` with the Edit tool (never `sed -i` on macOS). If `minecraft_version` changes, keep `gradle/libs.versions.toml` aligned.

5. **Sync**: `./dwm/gradlew syncVersionJson`. It prepends an empty stub for the new version and sets `promos.latest` / `promos.recommended`. Never hand-edit promos.

6. **Fill the entry** in `dwm/version.json` under `{mc}` → `{mod}` using Edit:
   - `summary`: required, non-blank, one player-facing sentence (leads GitHub/Modrinth/CurseForge notes and the Discord embed). Match prior entries' tone, e.g. "Land on Skaro — five hostile biomes, … — plus …".
   - `added` / `changed` / `removed`: bullets with the established prefixes: `Block: `, `Item: `, `Entity: `, `Biome: `, `Dimension: `, `Misc: `. Mirror the phrasing of the previous entry. Leave empty arrays rather than removing keys.
   - Keep two-space JSON formatting and a trailing newline.

7. **Modrinth listing** (optional): update `dwm/metadata/modrinth.json` / `modrinth-body.md` only when the release adds a headline feature or changes categories. Mention to the user that the listing needs the **Sync Modrinth Project** workflow and that the CurseForge page is manual.

8. **Verify**
   - `./dwm/gradlew checkVersionSync` (fails on blank summary, missing entry, or promo mismatch).
   - `./dwm/gradlew test`; add `./dwm/gradlew build` before handoff.
   - Report any failure verbatim and fix the root cause.

9. **Hand off**
   - Commit atomically: `chore(dwm): cut v{mc}-{mod} release notes` (Conventional Commits; include attribution lines from the session).
   - Open the PR with the three sections from `.github/pull_request_template.md`; label per AGENTS.md (`enhancement` for a feature release, `bug` for a hotfix).
   - Tell the user the remaining human steps: merge to `main` → create tag `v{mc}-{mod}` (manually or via **Create Release Tag**) → confirm the **Release** workflow publishes GitHub, Modrinth, CurseForge, Discord.

## Guardrails

- Do not bump `mod_version` or promos outside an intentional release; routine merges leave them on the last published release.
- `version.json` is the only changelog; never add a `CHANGELOG.md`.
- Don't change dependency versions or unrelated files.
- Ask before pushing, opening the PR, or anything that triggers publishing.
