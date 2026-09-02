# Description template (GitHub markdown)

Use this skeleton when writing the refined issue description. Replace placeholders; omit sections that do not apply. Keep prose concise — bullets and tables over long paragraphs.

**Merge policy:** integrate with existing description content unless the user asked for a full replace.

**Issue links:** `[DWM-061](https://github.com/adam-k-ali/DWM/issues/202)` — stable ID plus GitHub number.

Gold standard: [DWM-061](https://github.com/adam-k-ali/DWM/issues/202). Epic gold standard: [E-006](https://github.com/adam-k-ali/DWM/issues/195).

---

## Section guide

| Section | Include when |
|---------|----------------|
| Header (Epic / Priority / Blocked by) | Always on tickets; epics may omit Epic and Blocked by |
| Why | Always |
| Scope | Always on tickets — explicit in/out with links to sibling tickets |
| Decisions made | Scope changed from original plan, comments, or canceled deps |
| Behaviour / player contract | Player-visible verbs, overlays, refuse tables, HUD |
| Data / networking | Payloads, data components, persistence, sidedness |
| Files to change / add | Always — paths from live reconnaissance |
| Tests | Always when behaviour ships — which Gradle tasks |
| Docs | Player-visible behaviour documented under `dwm/docs/` |
| Acceptance criteria | Always on tickets — testable checkboxes |
| Exit criteria | Epics |
| Ticket table | Epics |
| Dependencies | Blocked by, related, or superseded issues |

Optional: mermaid, overlay tables, Screenplay file tables — only when they reduce ambiguity.

Do **not** include Expo routes, GraphQL, Apollo containers, or Storybook.

---

## Ticket template

```markdown
**Epic:** #195 (E-006 — Survival progression loops)
**Priority:** P2
**Blocked by:** #199 (DWM-058), #201 (DWM-060)

---

## Why

<!-- Product/player value. Why this ticket exists now. -->

## Scope

**In scope (this ticket):**

* <!-- bullet -->

**Out of scope (other tickets):**

* <!-- bullet with issue link -->

## Decisions made

<!-- Numbered. Call out overrides to stale planning, closed blockers, comment-driven simplifications. -->

1. **<!-- decision -->** — <!-- rationale + link to comment/issue if applicable -->

## Behaviour

<!-- Player contract. Overlays, refuse table, HUD, who may act. Omit if infra-only. -->

| Situation | Result |
|-----------|--------|
| <!-- --> | <!-- overlay or world change --> |

## Data / networking

<!-- C2S/S2C payloads, data components, NBT/Gson fields. Note main vs client. -->

## Files to change / add

* **Add** `<!-- path -->` — <!-- one-line why -->
* **Edit** `<!-- path -->` — <!-- one-line why -->

Mark uncertain paths **Inferred**; verified paths need no label.

## Tests

* **JUnit** (`./dwm/gradlew test`): <!-- -->
* **GameTest** (`./dwm/gradlew runGametest`): <!-- or N/A -->
* **Screenplay** (`./dwm/gradlew runScreenplay -Pscreenplay=<stem>`): <!-- or N/A -->
* **Datagen** (`./dwm/gradlew runDatagen`): <!-- or N/A -->

Use `./screenplay/gradlew` when the ticket is harness-only.

## Docs

* <!-- `dwm/docs/feature-….md` or N/A — no new player-facing docs -->

## Acceptance criteria

- [ ] <!-- testable outcome -->
- [ ] <!-- testable outcome -->
- [ ] <!-- named Gradle tasks the implementer must run -->

## Dependencies

* **Blocked by:** [DWM-xxx](https://github.com/adam-k-ali/DWM/issues/N) — <!-- why -->
* **Related:** [DWM-xxx](https://github.com/adam-k-ali/DWM/issues/N) — <!-- handoff boundary -->
* **Superseded planning:** <!-- closed issues, dropped approaches -->
```

---

## Epic template

```markdown
**Priority:** P1

---

# E-NNN — <!-- title -->

<!-- One-paragraph outcome. -->

## Why

<!-- Why this epic exists. Point at dwm/docs when relevant. -->

## Exit criteria

- <!-- player-visible or world-state outcome -->

## Defaults (do not re-litigate)

- <!-- shipped policy the children must not reopen -->

## Tickets

| ID | Title | Priority |
|----|-------|----------|
| [DWM-xxx](https://github.com/adam-k-ali/DWM/issues/N) | <!-- --> | P1 |

## Depends on

- <!-- other epics / closed prerequisites -->

## Out of scope

- <!-- explicit non-goals -->
```

---

## Writing tips

- Prefer **file paths** over pasted source blocks.
- Mark **Verified in codebase** vs **Inferred** when uncertain.
- Acceptance criteria must be **testable** — avoid "works correctly" or "looks good".
- Visual tickets: cite existing client HUD/docs/asset paths. Do not attach HTML mockups unless the user asked for screenshots.
- For iOS-first / Expo notes: this is a Minecraft Fabric monorepo — do not carry those sections over.
