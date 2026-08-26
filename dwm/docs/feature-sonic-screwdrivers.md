# Feature: Sonic Screwdrivers

See also: [Docs Index](./index.md)

## Product Intent

One survival sonic that starts with **Open** and grows through craftable field-mode settings. Doctor variants are casings (looks), not four identical diamond tools.

TARDIS handshake (seal / scan / ping) is **DWM-061** — not this feature. Field modes stay separate from TARDIS verbs.

## Player Outcomes

- Craft a cheap sonic early (iron + redstone torch + glass pane).
- Unlock Shatter / Prime / Disrupt / Shear with cheap setting items.
- Switch modes via the field-mode HUD carousel (sneak-use in air).
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

Hold a setting in one hand and a sonic in the other, then use the setting. Consumes on success. Overlay names the mode. Already installed: overlay, do not consume.

## Selecting a mode

Carousel order: **Open → Shatter → Prime → Disrupt → Shear** (all modes visible; locked ones greyed).

- **Active-mode indicator:** while a sonic is held, a compact Gallifreyan panel in the top-right shows its currently active mode. It hides while the carousel is open.
- **HUD carousel:** sneak-use the sonic **in the air**. A row of target icons appears above the hotbar. Scroll or arrow keys move the highlight; release sneak to activate the focused mode. Locked modes show install hints; releasing on a locked preview keeps the current mode and shows a not-installed overlay. TARDIS actions are not in the carousel.
- **Cancel:** press ESC to close the carousel without changing mode.

Action bar on confirm: `Setting: …`.

Un-sneak use-in-air is reserved for ping (DWM-061).

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

## Capability rules

Same three-path shape as unfinished TARDIS circuits:

- Missing component → fully unlocked
- `/give` / creative tab → fully unlocked (no component)
- Crafted → Open only

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
├── All Settings (GOAL: four installs)
└── first ping (DWM-061 later)
```

Teaching only — each leaf fires once. Creative fully-unlocked sonics do not auto-complete install/cycle/use leaves.

## Known Constraints

- Interactions stay on a curated target set.
- Not every Doctor exterior exists as a sonic casing yet.
- Behaviour is utility-first, not combat-focused.

## Future Opportunities

- TARDIS seal / scan / ping (DWM-061)
- Later content that gates on installed settings (DWM-062)
