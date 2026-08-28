# Feature: Stattenheim Remote

See also: [Docs Index](./index.md), [TARDIS Exterior Block](./feature-tardis-block.md)

## Product Intent
Give the TARDIS owner a pocket remote that calls their ship to them, with a visible materialisation fade.

## Player Outcomes
- Craft a Stattenheim remote and keep it in the hotbar.
- Sneak-right-click the ground to summon their owned TARDIS to that spot.
- See the exterior fade in as it materialises.

## Implemented Now
- Craftable `stattenheim_remote` (gold ingot, iron ingots, redstone, ender pearl).
- Sneak + use on a block summons the caller's owned TARDIS (`ownerUuid`).
- Landing is the cell on the clicked face, door facing the player, precise (no scatter / coordinate locks).
- Parked TARDISes dematerialise, skip the in-flight lever wait, then materialise at the click site. Demat spends artron using the same formula as the console lever (10 same-world, 30 to change dimension). An already in-flight TARDIS materialises at the click site for free.
- Exterior shell fades out during dematerialisation and fades in during materialisation (console travel included).
- Summon slams both door leaves shut immediately (exterior + interior); they stay closed through landing. Travel itself does not auto-close or auto-open doors.

## How It Works In-Game
1. Craft the remote (or take it from Tools & Utilities).
2. Own a TARDIS (first enter, or ops `/tardis claim`).
3. Sneak and right-click the ground where you want it.
4. The doors slam shut, then the TARDIS dematerialises from its current exterior and rematerialises at the click, fading in.

Failure overlays: no owned TARDIS, already travelling, cannot land here, circuit broken (found Type 40 with a broken remote-summon circuit — smoke puff at the click), not enough artron (same 10 / 30 cost as console demat; empty tank uses `Artron reserves: empty`).

Repair the remote-summon circuit by using a **Remote Summon Circuit** while a Stattenheim remote is in the other hand (owner only; consumes unless creative). Clicking a console control with that item does not summon.

## Known Constraints
- The remote is bound by player ownership, not item NBT — anyone holding it summons *their* TARDIS.
- Found worldgen TARDISes start with a broken remote-summon circuit until the owner installs `circuit_remote_summon` with a remote in the other hand.
- Collision stays solid during the fade. BOTI is skipped while the shell is translucent.
- Non-sneak use does nothing.

## Future Opportunities
- Companion-shared remotes bound to a specific TARDIS UUID.
