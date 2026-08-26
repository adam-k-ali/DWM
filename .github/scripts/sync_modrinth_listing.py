#!/usr/bin/env python3
"""Sync Modrinth project listing (+ optional content disclosures) from metadata files.

Used by Sync Modrinth Project / Sync Modrinth Screenplay workflows.
"""

from __future__ import annotations

import argparse
import json
import os
import sys
import urllib.error
import urllib.request
from pathlib import Path
from typing import Any

USER_AGENT = "adam-k-ali/DWM (https://github.com/adam-k-ali/DWM)"
ALLOWED_AI_USES = frozenset({"code", "assets", "text", "functionality"})


def _require_nonblank_str(value: Any, label: str) -> str:
    if not isinstance(value, str) or not value.strip():
        raise SystemExit(f"{label} must be a non-blank string")
    return value.strip()


def _require_str_list(value: Any, label: str) -> list[str]:
    if not isinstance(value, list) or not all(isinstance(item, str) for item in value):
        raise SystemExit(f"{label} must be a list of strings")
    return value


def _parse_disclosures(meta: dict[str, Any], meta_path: Path) -> dict[str, Any] | None:
    if "disclosures" not in meta:
        return None

    disclosures = meta["disclosures"]
    if not isinstance(disclosures, dict):
        raise SystemExit(f"{meta_path} disclosures must be an object")

    set_items = disclosures.get("set", [])
    remove_items = disclosures.get("remove", [])

    if not isinstance(set_items, list):
        raise SystemExit(f"{meta_path} disclosures.set must be a list")
    if not isinstance(remove_items, list) or not all(
        isinstance(item, str) and item.strip() for item in remove_items
    ):
        raise SystemExit(f"{meta_path} disclosures.remove must be a list of non-blank strings")

    normalized_set: list[dict[str, Any]] = []
    for index, item in enumerate(set_items):
        if not isinstance(item, dict):
            raise SystemExit(f"{meta_path} disclosures.set[{index}] must be an object")
        disclosure_type = item.get("type")
        if not isinstance(disclosure_type, str) or not disclosure_type.strip():
            raise SystemExit(f"{meta_path} disclosures.set[{index}].type must be a non-blank string")

        payload: dict[str, Any] = {"type": disclosure_type.strip()}

        if "note" in item and item["note"] is not None:
            if not isinstance(item["note"], str):
                raise SystemExit(f"{meta_path} disclosures.set[{index}].note must be a string")
            payload["note"] = item["note"]

        if disclosure_type.strip() == "ai_content":
            uses = item.get("uses")
            if not isinstance(uses, list) or not uses:
                raise SystemExit(
                    f"{meta_path} disclosures.set[{index}].uses must be a non-empty list for ai_content"
                )
            if not all(isinstance(use, str) for use in uses):
                raise SystemExit(f"{meta_path} disclosures.set[{index}].uses must be strings")
            unknown = sorted({use for use in uses if use not in ALLOWED_AI_USES})
            if unknown:
                raise SystemExit(
                    f"{meta_path} disclosures.set[{index}].uses has unknown values: {', '.join(unknown)}"
                )
            payload["uses"] = sorted(set(uses))

        normalized_set.append(payload)

    if not normalized_set and not remove_items:
        raise SystemExit(f"{meta_path} disclosures must set or remove at least one disclosure")

    return {
        "set": normalized_set,
        "remove": [item.strip() for item in remove_items],
    }


def _request_json(
    method: str,
    url: str,
    token: str,
    payload: dict[str, Any] | None,
    *,
    dry_run: bool,
) -> int:
    body = None if payload is None else json.dumps(payload).encode("utf-8")
    if dry_run:
        print(f"DRY RUN {method} {url}")
        if payload is not None:
            print(json.dumps(payload, indent=2, ensure_ascii=False))
        return 204

    request = urllib.request.Request(
        url,
        data=body,
        method=method,
        headers={
            "Authorization": token,
            "Content-Type": "application/json",
            "User-Agent": USER_AGENT,
        },
    )
    try:
        with urllib.request.urlopen(request) as response:
            status = response.status
            response.read()
            return status
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        print(detail, file=sys.stderr)
        raise SystemExit(f"Modrinth {method} {url} failed with HTTP {exc.code}") from exc


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--project-id", required=True, help="Modrinth project id")
    parser.add_argument("--meta", required=True, type=Path, help="Path to modrinth.json")
    parser.add_argument("--body", required=True, type=Path, help="Path to modrinth-body.md")
    parser.add_argument(
        "--require-discord",
        action="store_true",
        help="Require discord_url in metadata and PATCH it",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Validate and print payloads without calling Modrinth",
    )
    args = parser.parse_args()

    if not args.meta.is_file():
        raise SystemExit(f"Missing {args.meta}")
    if not args.body.is_file():
        raise SystemExit(f"Missing {args.body}")

    meta = json.loads(args.meta.read_text(encoding="utf-8"))
    required = ["description", "categories", "additional_categories"]
    if args.require_discord:
        required.append("discord_url")
    missing = [key for key in required if key not in meta]
    if missing:
        raise SystemExit(f"{args.meta} missing keys: {', '.join(missing)}")

    description = _require_nonblank_str(meta["description"], "description")
    categories = _require_str_list(meta["categories"], "categories")
    additional_categories = _require_str_list(
        meta["additional_categories"], "additional_categories"
    )
    body = args.body.read_text(encoding="utf-8").strip()
    if not body:
        raise SystemExit(f"{args.body} must be non-blank")

    listing_payload: dict[str, Any] = {
        "description": description,
        "body": body,
        "categories": categories,
        "additional_categories": additional_categories,
    }
    if args.require_discord:
        listing_payload["discord_url"] = _require_nonblank_str(meta["discord_url"], "discord_url")

    disclosures_payload = _parse_disclosures(meta, args.meta)

    token = os.environ.get("MODRINTH_TOKEN", "")
    if not args.dry_run and not token:
        raise SystemExit("MODRINTH_TOKEN secret is not set")

    listing_status = _request_json(
        "PATCH",
        f"https://api.modrinth.com/v2/project/{args.project_id}",
        token,
        listing_payload,
        dry_run=args.dry_run,
    )
    if listing_status != 204:
        raise SystemExit(f"Expected HTTP 204 from listing PATCH, got {listing_status}")
    print(f"Updated Modrinth project listing for {args.project_id}")

    if disclosures_payload is not None:
        disclosure_status = _request_json(
            "PATCH",
            f"https://api.modrinth.com/v3/project/{args.project_id}/disclosures",
            token,
            disclosures_payload,
            dry_run=args.dry_run,
        )
        if disclosure_status != 204:
            raise SystemExit(
                f"Expected HTTP 204 from disclosures PATCH, got {disclosure_status}"
            )
        print(f"Updated Modrinth disclosures for {args.project_id}")


if __name__ == "__main__":
    main()
