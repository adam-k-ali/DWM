# Dalek Laser Gun

A hand-held version of the Dalek's gun-stick, with a pistol grip.

- **Use:** right-click to fire a Dalek laser bolt (same bolt and sound as a Dalek's shot).
- **Cooldown:** 1 second (20 ticks).
- **Damage:** 4 per hit. The bolt does not hit the shooter and is attributed to the player. Player bolts
  hurt Daleks and Dalek mutants; Dalek-fired bolts still pass through other Daleks and mutants.
- **Durability / ammo:** none.
- **Recipe:** shaped — steel ingots, redstone, a diamond and a lever.
- **Creative tab:** Combat.

Thals can wield the gun during [Dalek/Thal battles](./feature-dalek-thal-battles.md); their bolts hurt Daleks but not Thals.

Tuning lives in `LaserGunLogic`; the item is `LaserGunItem`. Player-fired bolts use a fixed damage value
and a non-mob damage source in `DalekLaserEntity`; Dalek-fired bolts are unchanged.
