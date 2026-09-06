# Palette — Zeiton

Family id: `zeiton`

Map colour: `TERRACOTTA_ORANGE`

Gallifrey-stone host; emerald/teal mineral veins. Map colour follows the Gallifrey ore host (terracotta orange), not the green veins.

![Zeiton colour swatch](./zeiton-swatch.png)

| Role | Hex | Notes |
|------|-----|-------|
| `host_shadow` | `#5C1F0D` | Gallifrey stone darkest |
| `host_mid` | `#843D1D` | Gallifrey stone primary fill |
| `host_hi` | `#974F27` | Gallifrey stone highlight |
| `vein_shadow` | `#009649` | Emerald vein dark |
| `vein_mid` | `#09AF71` | Teal-green vein fill |
| `vein_hi` | `#72EAC3` | Mint vein highlight |

Source JSON: [`zeiton.json`](./zeiton.json). Regenerate with `poetry -C dwm/tools/palette run generate-family-palette-docs` (from repo root: `--palette dwm/docs/palettes/<id>.json --out-dir dwm/docs/palettes`).
