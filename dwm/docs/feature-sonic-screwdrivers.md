# Feature: Sonic Screwdrivers

See also: [Docs Index](./index.md)

## Product Intent

One survival sonic that starts with **Open** and grows through craftable field-mode settings. Doctor variants are casings (looks), not four identical diamond tools.

Use the sonic on **your** TARDIS to pair it. Pairing unlocks Seal, Scan, and Ping together — there are no extra setting crafts for those verbs.

## Player Outcomes

- Craft a cheap sonic early (iron + redstone torch + glass pane).
- Unlock Shatter / Prime / Disrupt / Shear with cheap setting items.
- Switch modes via the field-mode HUD carousel (sneak-use in air), including TARDIS modes.
- Pair with your TARDIS, then Seal closed doors, Scan the linked ship, or Ping a cloaked shell.
- Read wrong-setting vs missing-setting overlays on known targets in one attempt.

## Crafting

| Recipe | Result |
|--------|--------|
| Iron + redstone torch + glass pane (shapeless) | `sonic_third_doctor` with **Open only** |
| Redstone + glass pane | `sonic_setting_shatter` |
| Redstone + gunpowder | `sonic_setting_prime` |
| Redstone + slimeball | `sonic_setting_disrupt` |
| Redstone + iron nugget | `sonic_setting_shear` |
| Any `#dwm:sonic_screwdrivers` + matching pane (transmute) | That Doctor casing, **copying** `sonic_state` |

Pane colours: light blue → Second, clear → Third, gray → Fourth, red → Fifth.

Sonics `stacksTo(1)`. Missing `sonic_state` (legacy / `/give` / creative) = all field modes unlocked. Crafted = Open only.

## Installing a setting

Hold a setting in one hand and a sonic in the other, then use the setting. Consumes on success. Overlay names the mode. Already installed: overlay, do not consume. Seal / Scan / Ping cannot be installed from items.

## Selecting a mode

Carousel order: **Open → Shatter → Prime → Disrupt → Shear → Seal → Scan → Ping** (all modes visible; locked ones greyed).

- **Active-mode indicator:** while a sonic is held, a compact Gallifreyan panel in the top-right shows its currently active mode. It hides while the carousel is open.
- **HUD carousel:** sneak-use the sonic **in the air**. A row of target icons appears above the hotbar. Scroll or arrow keys move the highlight; release sneak to activate the focused mode. Locked field modes show recipe hints; locked TARDIS modes show **Use on your TARDIS to pair**. Releasing on a locked preview keeps the current mode and shows a not-installed overlay.
- **Cancel:** press ESC to close the carousel without changing mode.

Action bar on confirm: `Setting: …`.

Unsneak use-in-air with **Ping** selected locates a cloaked TARDIS. Other selected modes `PASS` with no overlay.

## Field modes

| Mode | Targets |
|------|---------|
| Open | Toggle iron doors and iron trapdoors |
| Shatter | Break glass / stained glass / panes with no drops |
| Prime | Prime TNT |
| Disrupt | 1 damage to slimes |
| Shear | Shear sheep when ready |

Unknown blocks: whir only, no overlay. Known targets always get a readable overlay on failure:

- Wrong setting (installed but not selected): `Wrong setting — Needs …`
- Not installed: `Setting not installed — Needs … setting`

## TARDIS handshake

The owner's first use of a crafted sonic on **their** exterior, interior doors, or console unlocks Seal, Scan, and Ping and marks `tardisPaired`. Creative `/give` sonics already have those modes, so the handshake overlay is skipped.

- Strangers and unclaimed ships: `This TARDIS does not recognise you`.
- Bound-key companions keep lock via the key / Panel4 — the sonic does **not** share Seal / Scan / Ping.
- If Seal or Scan is already selected and the target is a door, that action runs on the same click after pairing.

## TARDIS modes

| Mode | Behaviour |
|------|-----------|
| Seal | Toggle lock on the owner's **closed** doors (`Doors locked` / `Doors unlocked` / `Doors must be closed`). Does not open or close doors. |
| Scan | Read-only overlay starting with `Scan:` (exterior environment plus locked / cloaked / travel phase / artron). |
| Ping | Unsneak use-in-air. Cloak must be fitted and engaged; range 32 blocks in the same dimension. Success: `TARDIS located`, owner-only silhouette and particles for 2s, 2s cooldown. Failures: `cloak not fitted` / `cloak not engaged` / `no signal`. |

Paired doors with neither Seal nor Scan selected: `Wrong setting — Needs Seal or Scan`. Console after pairing: whir only.

## Capability rules

Same three-path shape as unfinished TARDIS circuits:

- Missing component → fully unlocked
- `/give` / creative tab → fully unlocked (no component)
- Crafted → Open only until handshake / setting installs

## Advancements (Doctor Who tab)

Hang off obtain sonic (`dwm/sonic_screwdriver`). Do not merge into the TARDIS branch.

```
obtain sonic
├── Knock Knock (Open on iron door)
├── Change the Setting (first real mode change via HUD carousel)
├── Shatter Setting → Through the Glass
├── Prime Setting → Three Two One
├── Disrupt Setting → Unstable Structure
├── Shear Setting → A Close Shave
├── All Settings (GOAL: four crafted installs)
└── TARDIS Located (Ping a cloaked shell)
```

Teaching only — each leaf fires once. Creative fully-unlocked sonics do not auto-complete install/cycle/use leaves. All Settings stays the four crafted installs; Seal / Scan / Ping are not part of that goal.

## Known Constraints

- Interactions stay on a curated target set.
- Not every Doctor exterior exists as a sonic casing yet.
- Behaviour is utility-first, not combat-focused.

## Future Opportunities

- Later content that gates on installed settings (DWM-062)
