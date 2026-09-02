---
name: refine-github-issue
description: >-
  Refine under-planned GitHub issues for DWM (and Screenplay in this repo) into
  build-ready tickets. Use when asked to flesh out, refine, or plan a GitHub
  issue (DWM-NNN, E-NNN, #123, or issue URL): update the description via gh, list
  files to create or change, reconcile related issues and comments, and
  optionally set Project Status to Ready after confirmation. Documentation-only
  — no mod/harness code changes unless the user explicitly asks.
---

# Refine GitHub Issue (DWM)

## Keywords

refine issue, flesh out ticket, under-planned, build-ready, DWM-, E-, GitHub issue, GitHub Projects, acceptance criteria, scope refinement

## Overview

Transform an under-refined GitHub issue into a **build-ready** ticket for this monorepo: reconcile comments and related issues, explore the codebase read-only, present a plan for approval, then update the issue **body** via `gh`.

**Output:** updated GitHub issue description (and Project Status only if that run’s confirmation includes it). Not Java, assets, or YAML implementation.

**Read references on demand:**

| Reference | When |
|-----------|------|
| [description-template.md](references/description-template.md) | Writing the final issue markdown |
| [github.md](references/github.md) | Fetching, resolving IDs, saving bodies, project fields |

**Do not duplicate live repo knowledge in this skill.** Link to [AGENTS.md](../../../AGENTS.md) and read the codebase at refinement time. For test-layer choices, link (do not copy) [fabric-gametest](../fabric-gametest/SKILL.md), [minecraft-mcp-verify](../minecraft-mcp-verify/SKILL.md), and [dwm/src/screenplayTests/AGENTS.md](../../../dwm/src/screenplayTests/AGENTS.md).

Gold-standard ticket body: [DWM-061](https://github.com/adam-k-ali/DWM/issues/202). Gold-standard epic: [E-006](https://github.com/adam-k-ali/DWM/issues/195).

---

## Required inputs

1. **Issue identifier** — `DWM-061`, `E-006`, `#202`, or `https://github.com/adam-k-ali/DWM/issues/202`
2. **Reason / intent** — why this should be build-ready now
3. **Optional:** scope limits, merge vs full replace (default: **merge**), whether to set Status → Ready

If the identifier is missing, ask once before proceeding.

---

## Hard constraints

| Allowed | Forbidden |
|---------|-----------|
| Read-only repo exploration (`Read`, `Grep`, `Glob`, readonly subagents) | Branch, commit, push, or PR **for refinement** |
| `gh issue` / `gh project` per [github.md](references/github.md) | Edit mod/harness source, datagen, installs |
| Temp files under `/tmp/` for `--body-file` | Paste secrets, `.env` values, or credentials into issues |

**Default writes:** **description (body) only.** Do not change `title`, labels, assignees, milestone, Size, or Priority unless the user explicitly asks.

**Status:** In the Phase 3 summary, recommend `Ready` when the ticket looks build-ready and Project Status is currently `Backlog`. Apply `gh project item-edit` **only if that run’s confirmation includes it**.

**Security:** refer to configuration and env vars **by name** only.

Do not invent overlay strings or file paths — mark **Inferred** or look them up. Do not create new issues unless the user asks after the refine plan.

---

## Workflow

Follow these phases in order. **Present the refinement plan (Phase 3) and wait for user confirmation** before writing to GitHub — unless the user already said to proceed.

### Phase 1 — Intake

1. Extract identifier. Resolve `DWM-NNN` / `E-NNN` to a GitHub number (see [github.md](references/github.md)). Default repo: `adam-k-ali/DWM`.
2. `gh issue view` with JSON fields in github.md. On failure, report and **stop** (do not invent content).
3. Read comments — capture scope changes (product direction often lives here).
4. Fetch **parent epic** and **blocking/related** issues linked in the body. Note closed or superseded tickets; reconcile stale class names, overlay copy, or feature-doc plans.
5. Confirm [Project #7](https://github.com/users/adam-k-ali/projects/7) membership via `projectItems`. If missing, note it in the plan; `gh project item-add` only if the user asks.

### Phase 2 — Codebase reconnaissance (read-only)

1. Read [AGENTS.md](../../../AGENTS.md). Identify DWM vs Screenplay vs client-only vs data-driven vs test-only.
2. Read `dwm/docs/feature-*.md` (and [differentiation-strategy.md](../../../dwm/docs/differentiation-strategy.md)) when the ticket names a player-facing system.
3. Search the **live codebase** using the issue title, item IDs, overlay strings, and feature names. Find analogous items, blocks, payloads, GameTests, and Screenplay YAML. Let findings drive the file list — **no hardcoded path checklist**.
4. In the ticket, label findings **Verified in codebase** vs **Inferred**.
5. Pick the Gradle wrapper (`./dwm/gradlew` vs `./screenplay/gradlew`) and test layers (JUnit / GameTest / Screenplay / MCP) from live evidence.

### Phase 3 — Present refinement plan (approval gate)

Summarize for the user:

- Scope in/out and comment-driven changes
- Key **Decisions made** (especially overrides to stale planning)
- Related-issue reconciliation
- Files / packages to touch (main vs client)
- Test plan (which Gradle tasks)
- Visuals: cite existing HUD/client/docs/assets; screenshots only if the user asked (no HTML mockup pipeline)
- **Status → Ready?** recommend yes/no and current Status
- Open questions

**Wait for confirmation** before Phases 4–5. Incorporate feedback iteratively.

### Phase 4 — Write description

Use [description-template.md](references/description-template.md). Ticket sections vs epic variant are listed there.

**Merge policy:** preserve useful existing content; de-duplicate; integrate comment-driven changes unless the user asked for full replace.

Keep the existing header convention (`Epic` / `Priority` / `Blocked by`, then `---`, then `## Why`).

Write the body to a temp file; `gh issue edit N --repo adam-k-ali/DWM --body-file …`.

### Phase 5 — Verify and report

1. `gh issue view` — confirm the body landed.
2. If Ready was confirmed this run, set Status per [github.md](references/github.md) and re-check `projectItems`.
3. Reply with: issue link, short summary of changes, 2–3 key repo paths used. Do not paste the full description unless asked.

---

## When NOT to use this skill

- Implementing the ticket (use normal dev workflow)
- Creating new issues from scratch without an existing GitHub issue (unless the user asks to refine after creation)
- Backlog work in Linear/Jira

---

## Quick reference

**Primary flow:** Intake → Recon → Plan (approve) → Write → Verify (+ optional Ready)

**Board:** [users/adam-k-ali/projects/7](https://github.com/users/adam-k-ali/projects/7) — Status `Backlog` / `Ready` / `In progress` / `Done` / `Cancelled`; Priority `P0`–`P3`

**Template:** [description-template.md](references/description-template.md)

**gh recipes:** [github.md](references/github.md)
