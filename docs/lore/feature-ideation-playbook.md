# Feature Ideation Playbook (Lore -> DWM Features)

See also: [Lore Index](./index.md), [TARDIS and Time Travel Rules](./tardis-and-time-travel-rules.md), [Major Villains and Threat Patterns](./major-villains-and-threat-patterns.md)

This playbook helps AI agents convert Doctor Who lore into practical mod proposals without scope creep.

## 1) Start From Existing DWM Pillars

Anchor ideas to current documented product direction:
- sonic utility interactions,
- TARDIS exterior identity and interaction loops,
- builder-first themed content,
- stable vs experimental clarity.

If an idea does not strengthen one of these, down-rank it.

## 2) Lore-to-Gameplay Translation Rules

Translate lore to **play patterns**, not just references:
- Regeneration -> player state transition/recovery/progression.
- Chameleon circuit -> variant selection/disguise mechanics.
- Time anomalies -> localized rule changes, cooldown windows, or event zones.
- Villains -> distinct encounter mechanics with clear counters.

Avoid direct lore dumps with no mechanical depth.

## 3) Improvement Ideas for Current Features

### Sonic Screwdrivers (Current)
Potential improvements:
- Context-specific feedback package (unique sound/particles/message per target type).
- "Sonic profile" metadata for each Doctor variant (same base utility, different UX flavor).
- Optional progression unlocks for additional interactions while preserving balance.

Lore justification:
- Sonic tools are adaptable and precise, not generic weapons.
- Different Doctors emphasize style and approach differences.

### TARDIS Exterior Block (Current)
Potential improvements:
- Stronger state cues (open/closed/locked visual markers).
- Ownership and permission layer for multiplayer roleplay trust.
- Anchoring interactions to "stabilization" actions (low-friction ritual before key interactions).

Lore justification:
- TARDIS behavior is identity-rich and owner-associated.
- Exterior state is an important narrative signal.

### Chameleon System (Experimental)
Potential improvements:
- Lore framing in UI ("chameleon circuit calibration" language).
- Variant categories by era/theme for easier player choice.
- Failure-state polish (clear error responses when disabled by config or blocked server-side).

Lore justification:
- Chameleon circuits are iconic, imperfect, and often narrative-significant.

## 4) New Feature Candidate Shapes (Small/Medium Scope)

### A) Time Rift Events (Small-Medium)
- Spawn rare world anomalies that create temporary rules (mob behavior shifts, sound cues, visual distortion).
- Reward investigation with themed items/components.

Why it fits lore:
- Doctor Who frequently uses localized temporal instability.

Why it fits DWM:
- Creates reusable interaction loops without requiring full dimension travel.

### B) UNIT Signal Beacon (Small)
- Placeable block that detects nearby anomalies/hostile incursions and provides directional hints.

Why it fits lore:
- UNIT is Earth's organized response to unusual threats.

Why it fits DWM:
- Clear utility object for builders and survival players.

### C) Villain Encounter Modules (Medium)
- Modular encounter templates (Dalek patrol, Cyber conversion event, Angel zone challenge).
- Each module has one clear "read and counter" rule.

Why it fits lore:
- Villains are memorable because they impose specific tactical constraints.

Why it fits DWM:
- Supports curated quality over broad content sprawl.

## 5) Canon-Safe Guardrails

- Keep the Doctor non-playable by default; focus on being a participant in Doctor Who-style events.
- Do not hard-lock ideas to one era unless intended.
- Preserve wonder and tension: combine danger + clever solutions, not pure combat escalation.
- Ensure descriptions distinguish:
  - **canon-inspired**, and
  - **strictly canonical**.

## 6) Proposal Quality Checklist

Before recommending a feature, verify:
1. Is the core loop understandable in under 2 in-game attempts?
2. Does it improve utility, building cohesion, or both?
3. Is the lore reference recognizable to a broad audience?
4. Can behavior be tested with unit tests, integration tests, or GameTests?
5. Does it avoid becoming a disconnected fan-service one-off?
