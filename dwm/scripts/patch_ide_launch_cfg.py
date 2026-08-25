#!/usr/bin/env python3
"""Ensure Loom launch.cfg classPathGroups include IDE bin/ outputs.

VS Code / Eclipse Java compile to dwm/bin/{main,client}, while Loom's
generateDLIConfig only lists build/classes and build/resources. Without the
bin/ entries Fabric may not treat main+client as one development mod.
"""

from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CFG = ROOT / ".gradle" / "loom-cache" / "launch.cfg"
PREFIX = "fabric.classPathGroups="


def main() -> None:
    if not CFG.is_file():
        raise SystemExit(f"missing {CFG}; run ./gradlew configureClientLaunch first")

    text = CFG.read_text()
    bins = [str(ROOT / "bin" / "main"), str(ROOT / "bin" / "client")]
    out: list[str] = []
    for line in text.splitlines():
        raw = line[1:] if line.startswith("\t") else line
        indent = "\t" if line.startswith("\t") else ""
        if raw.startswith(PREFIX):
            parts = [p for p in raw.split("=", 1)[1].split(":") if p]
            for path in bins:
                if path not in parts:
                    parts.append(path)
            out.append(f"{indent}{PREFIX}{':'.join(parts)}")
        else:
            out.append(line)

    new_text = "\n".join(out)
    if text.endswith("\n"):
        new_text += "\n"
    CFG.write_text(new_text)


if __name__ == "__main__":
    main()
