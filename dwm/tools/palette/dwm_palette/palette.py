"""Palette and product loading (seed + profile → expanded roles).

Offline tooling only — not invoked by Gradle or CI.
"""

from __future__ import annotations

import io
import json
import re
import zipfile
from functools import lru_cache
from pathlib import Path
from typing import Any

import numpy as np

from dwm_palette.profiles import expand_ramp, get_profile
from dwm_palette.recolor import HEX_RE, luminance, parse_hex

# Product templates under assets/dwm/textures/…, or vanilla study ids:
#   minecraft:block/<name>.png  → Loom minecraft-client.jar
MINECRAFT_TEMPLATE_RE = re.compile(
    r"^minecraft:(block|item)/([a-z0-9_./]+\.png)$"
)


def load_palette(path: Path) -> dict[str, Any]:
    """Load a palette JSON (seed + profile) and expand to step roles."""
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        raise ValueError(f"{path}: root must be a JSON object")

    family_id = data.get("family_id")
    display_name = data.get("display_name")
    seed = data.get("seed")
    profile_id = data.get("profile")
    if not isinstance(family_id, str) or not family_id:
        raise ValueError(f"{path}: family_id must be a non-empty string")
    if not isinstance(display_name, str) or not display_name:
        raise ValueError(f"{path}: display_name must be a non-empty string")
    if not isinstance(seed, str) or not HEX_RE.match(seed):
        raise ValueError(f"{path}: seed must be #RRGGBB, got {seed!r}")
    if not isinstance(profile_id, str) or not profile_id:
        raise ValueError(f"{path}: profile must be a non-empty string")

    # Validate profile exists early.
    get_profile(profile_id)

    map_color = data.get("map_color")
    if map_color is not None and not isinstance(map_color, str):
        raise ValueError(f"{path}: map_color must be a string when present")
    notes = data.get("notes", "")
    if notes is None:
        notes = ""
    if not isinstance(notes, str):
        raise ValueError(f"{path}: notes must be a string when present")

    roles = expand_ramp(seed.upper(), profile_id)
    return {
        "family_id": family_id,
        "display_name": display_name,
        "map_color": map_color,
        "notes": notes,
        "seed": seed.upper(),
        "profile": profile_id,
        "source_path": path,
        "roles": roles,
    }


def palette_hexes(palette: dict[str, Any]) -> list[str]:
    """Return expanded step hexes sorted dark→light by luminance."""
    hexes = [entry["hex"] for entry in palette["roles"]]
    if not hexes:
        raise ValueError("palette has no roles")
    return sorted(hexes, key=lambda h: luminance(parse_hex(h)))


def palette_source_dict(palette: dict[str, Any]) -> dict[str, Any]:
    """Build the authoring JSON object (seed + profile, not expanded roles)."""
    out: dict[str, Any] = {
        "family_id": palette["family_id"],
        "display_name": palette["display_name"],
    }
    if palette.get("map_color"):
        out["map_color"] = palette["map_color"]
    if palette.get("notes"):
        out["notes"] = palette["notes"]
    out["seed"] = palette["seed"]
    out["profile"] = palette["profile"]
    return out


def save_palette_json(palette: dict[str, Any], path: Path) -> None:
    """Write authoring palette JSON (seed + profile)."""
    path.parent.mkdir(parents=True, exist_ok=True)
    text = json.dumps(palette_source_dict(palette), indent=2) + "\n"
    path.write_text(text, encoding="utf-8")


def load_products(path: Path) -> list[dict[str, Any]]:
    """Load products.json and return the products list."""
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        raise ValueError(f"{path}: root must be a JSON object")
    products = data.get("products")
    if not isinstance(products, list) or not products:
        raise ValueError(f"{path}: products must be a non-empty list")
    normalized: list[dict[str, Any]] = []
    for i, entry in enumerate(products):
        if not isinstance(entry, dict):
            raise ValueError(f"{path}: products[{i}] must be an object")
        pid = entry.get("id")
        archetype = entry.get("archetype")
        template = entry.get("template")
        host = entry.get("host")
        mineral = entry.get("mineral")
        if not isinstance(pid, str) or not pid:
            raise ValueError(f"{path}: products[{i}].id must be a non-empty string")
        if not isinstance(archetype, str) or not archetype:
            raise ValueError(f"{path}: products[{i}].archetype must be a non-empty string")
        if not isinstance(template, str) or not template:
            raise ValueError(f"{path}: products[{i}].template must be a non-empty string")
        if not isinstance(host, str) or not host:
            raise ValueError(f"{path}: products[{i}].host must be a non-empty string")
        if not isinstance(mineral, str) or not mineral:
            raise ValueError(f"{path}: products[{i}].mineral must be a non-empty string")
        normalized.append(
            {
                "id": pid,
                "archetype": archetype,
                "template": template,
                "host": host,
                "mineral": mineral,
            }
        )
    return normalized


def find_product(products: list[dict[str, Any]], product_id: str) -> dict[str, Any]:
    for product in products:
        if product["id"] == product_id:
            return product
    raise ValueError(f"unknown product {product_id!r}")


def resolve_product_palettes(
    product: dict[str, Any], palettes_dir: Path
) -> tuple[dict[str, Any], dict[str, Any]]:
    """Load host and mineral palettes for a product from *palettes_dir*."""
    host_path = palettes_dir / f"{product['host']}.json"
    mineral_path = palettes_dir / f"{product['mineral']}.json"
    if not host_path.is_file():
        raise FileNotFoundError(f"host palette not found: {host_path}")
    if not mineral_path.is_file():
        raise FileNotFoundError(f"mineral palette not found: {mineral_path}")
    return load_palette(host_path), load_palette(mineral_path)


def read_minecraft_version(dwm_root: Path) -> str:
    """Read ``minecraft_version`` from ``dwm/gradle.properties``."""
    props = dwm_root / "gradle.properties"
    if not props.is_file():
        raise FileNotFoundError(f"gradle.properties not found: {props}")
    for line in props.read_text(encoding="utf-8").splitlines():
        stripped = line.strip()
        if stripped.startswith("minecraft_version="):
            value = stripped.split("=", 1)[1].strip()
            if value:
                return value
    raise ValueError(f"{props}: missing minecraft_version=")


def minecraft_client_jar(dwm_root: Path) -> Path:
    """Path to Fabric Loom's ``minecraft-client.jar`` for this DWM Minecraft version."""
    version = read_minecraft_version(dwm_root)
    jar = (
        Path.home()
        / ".gradle"
        / "caches"
        / "fabric-loom"
        / version
        / "minecraft-client.jar"
    )
    if not jar.is_file():
        raise FileNotFoundError(
            f"minecraft-client.jar not found at {jar} "
            f"(run ./dwm/gradlew genSources or a Loom task to populate the cache)"
        )
    return jar


def is_minecraft_template(template: str) -> bool:
    return bool(MINECRAFT_TEMPLATE_RE.match(template))


def minecraft_jar_entry(template: str) -> str:
    """Map ``minecraft:block/foo.png`` → ``assets/minecraft/textures/block/foo.png``."""
    match = MINECRAFT_TEMPLATE_RE.match(template)
    if not match:
        raise ValueError(
            f"not a minecraft: template id: {template!r} "
            "(expected minecraft:block/<name>.png or minecraft:item/<name>.png)"
        )
    kind, name = match.group(1), match.group(2)
    return f"assets/minecraft/textures/{kind}/{name}"


@lru_cache(maxsize=16)
def _load_png_from_jar(jar_path: str, entry: str) -> bytes:
    with zipfile.ZipFile(jar_path) as zf:
        try:
            return zf.read(entry)
        except KeyError as exc:
            raise FileNotFoundError(
                f"{entry} not found in {jar_path}"
            ) from exc


def load_minecraft_rgb(dwm_root: Path, template: str) -> np.ndarray:
    """Load a vanilla texture from the Loom client jar as HxWx3 uint8."""
    from PIL import Image

    entry = minecraft_jar_entry(template)
    jar = minecraft_client_jar(dwm_root)
    raw = _load_png_from_jar(str(jar), entry)
    with Image.open(io.BytesIO(raw)) as img:
        rgb = img.convert("RGB")
        return np.asarray(rgb, dtype=np.uint8)


def texture_path(dwm_root: Path, template: str) -> Path:
    """Resolve a DWM-owned product template under assets/dwm/textures/.

    Vanilla ``minecraft:…`` ids are not filesystem paths — use
    :func:`load_template_rgb` / :func:`resolve_template` instead.
    """
    if is_minecraft_template(template):
        raise ValueError(
            f"template {template!r} is a vanilla study id; "
            "use load_template_rgb() or resolve_template()"
        )
    return (
        dwm_root
        / "src"
        / "client"
        / "resources"
        / "assets"
        / "dwm"
        / "textures"
        / template
    )


def load_template_rgb(dwm_root: Path, template: str) -> np.ndarray:
    """Load a product template RGB array (DWM path or vanilla jar entry)."""
    from dwm_palette.recolor import load_rgb_image

    if is_minecraft_template(template):
        return load_minecraft_rgb(dwm_root, template)
    path = texture_path(dwm_root, template)
    if not path.is_file():
        raise FileNotFoundError(f"template not found: {path}")
    return load_rgb_image(path)


def resolve_template(dwm_root: Path, template: str) -> str | Path:
    """Return a display/label path for *template* (jar entry or DWM Path)."""
    if is_minecraft_template(template):
        return minecraft_jar_entry(template)
    return texture_path(dwm_root, template)


def vanilla_stone_host_colours(dwm_root: Path) -> frozenset[tuple[int, int, int]]:
    """Unique RGB triples from vanilla ``stone.png`` (ore host classifier)."""
    from dwm_palette.recolor import host_colour_set

    stone = load_minecraft_rgb(dwm_root, "minecraft:block/stone.png")
    return host_colour_set(stone)


def default_ore_template_from_products(
    dwm_root: Path, product_id: str = "azbantium_ore"
) -> str:
    """Return the ore template id/path string from products.json."""
    products = load_products(dwm_root / "docs" / "palettes" / "products.json")
    product = find_product(products, product_id)
    return product["template"]
