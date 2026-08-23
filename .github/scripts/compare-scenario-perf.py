#!/usr/bin/env python3
"""Compare scenario metrics.json trees against a baseline (advisory; always exits 0)."""

from __future__ import annotations

import argparse
import json
import os
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any


MARKER = "<!-- dwm-scenario-perf -->"
DEFAULT_TOLERANCE = 0.20
DEFAULT_FLOOR_MS = 50.0


@dataclass(frozen=True)
class MetricDelta:
    label: str
    baseline_ms: float | None
    current_ms: float | None
    status: str

    @property
    def delta_ms(self) -> float | None:
        if self.baseline_ms is None or self.current_ms is None:
            return None
        return self.current_ms - self.baseline_ms

    @property
    def delta_pct(self) -> float | None:
        if self.baseline_ms is None or self.current_ms is None:
            return None
        if self.baseline_ms == 0:
            return None if self.current_ms == 0 else float("inf")
        return (self.current_ms - self.baseline_ms) / self.baseline_ms * 100.0


@dataclass(frozen=True)
class ScenarioComparison:
    scenario_id: str
    total: MetricDelta
    steps: list[MetricDelta]


def classify(baseline_ms: float | None, current_ms: float | None, tolerance: float, floor_ms: float) -> str:
    if baseline_ms is None and current_ms is None:
        return "NO BASELINE"
    if baseline_ms is None:
        return "NO BASELINE"
    if current_ms is None:
        return "MISSING"
    delta = current_ms - baseline_ms
    threshold = baseline_ms * (1.0 + tolerance)
    improved_threshold = baseline_ms * (1.0 - tolerance)
    if current_ms > threshold and delta > floor_ms:
        return "REGRESSED"
    if current_ms < improved_threshold and (-delta) > floor_ms:
        return "IMPROVED"
    return "OK"


def load_metrics_tree(root: Path) -> dict[str, dict[str, Any]]:
    metrics: dict[str, dict[str, Any]] = {}
    if not root.exists():
        return metrics
    for path in sorted(root.rglob("metrics.json")):
        with path.open(encoding="utf-8") as handle:
            data = json.load(handle)
        scenario_id = data.get("scenarioId")
        if not isinstance(scenario_id, str) or not scenario_id:
            raise ValueError(f"metrics file missing scenarioId: {path}")
        metrics[scenario_id] = data
    return metrics


def step_map(data: dict[str, Any] | None) -> dict[str, float]:
    if data is None:
        return {}
    steps = data.get("steps") or []
    out: dict[str, float] = {}
    for step in steps:
        name = step.get("name")
        ms = step.get("ms")
        if isinstance(name, str) and isinstance(ms, (int, float)):
            out[name] = float(ms)
    return out


def compare_scenario(
    scenario_id: str,
    baseline: dict[str, Any] | None,
    current: dict[str, Any] | None,
    tolerance: float,
    floor_ms: float,
) -> ScenarioComparison:
    baseline_total = None if baseline is None else float(baseline.get("totalMs", 0.0))
    current_total = None if current is None else float(current.get("totalMs", 0.0))
    total = MetricDelta(
        label="totalMs",
        baseline_ms=baseline_total,
        current_ms=current_total,
        status=classify(baseline_total, current_total, tolerance, floor_ms),
    )

    baseline_steps = step_map(baseline)
    current_steps = step_map(current)
    step_names = sorted(set(baseline_steps) | set(current_steps))
    steps: list[MetricDelta] = []
    for name in step_names:
        b = baseline_steps.get(name)
        c = current_steps.get(name)
        # Only compare steps present in both; unpaired steps are informational NO BASELINE / MISSING
        if b is None and c is not None:
            status = "NO BASELINE"
        elif b is not None and c is None:
            status = "MISSING"
        else:
            status = classify(b, c, tolerance, floor_ms)
        steps.append(MetricDelta(label=name, baseline_ms=b, current_ms=c, status=status))

    return ScenarioComparison(scenario_id=scenario_id, total=total, steps=steps)


def compare_trees(
    current_dir: Path,
    baseline_dir: Path,
    tolerance: float,
    floor_ms: float,
) -> list[ScenarioComparison]:
    current = load_metrics_tree(current_dir)
    baseline = load_metrics_tree(baseline_dir)
    scenario_ids = sorted(set(current) | set(baseline))
    return [
        compare_scenario(scenario_id, baseline.get(scenario_id), current.get(scenario_id), tolerance, floor_ms)
        for scenario_id in scenario_ids
    ]


def format_ms(value: float | None) -> str:
    if value is None:
        return "—"
    if abs(value) >= 1000.0:
        return f"{value / 1000.0:.1f}s"
    return f"{value:.1f}ms"


def format_delta(delta: MetricDelta) -> str:
    if delta.delta_ms is None:
        return "—"
    magnitude = abs(delta.delta_ms)
    if magnitude >= 1000.0:
        abs_part = f"{magnitude / 1000.0:.1f}s"
    else:
        abs_part = f"{magnitude:.1f}ms"
    sign = "+" if delta.delta_ms >= 0 else "-"
    pct = delta.delta_pct
    if pct is None:
        pct_part = ""
    elif pct == float("inf"):
        pct_part = " (n/a%)"
    else:
        pct_sign = "+" if pct >= 0 else ""
        pct_part = f" ({pct_sign}{pct:.0f}%)"
    return f"{sign}{abs_part}{pct_part}"


def render_markdown(
    comparisons: list[ScenarioComparison],
    baseline_label: str,
    tolerance: float,
    floor_ms: float,
) -> str:
    lines: list[str] = [
        MARKER,
        "## Scenario performance vs main",
        "",
        f"Baseline: {baseline_label}",
        "Advisory only — does not fail CI.",
        "",
        f"Regression rule: slower by more than {tolerance:.0%} **and** more than {floor_ms:.0f}ms.",
        "",
        "| Scenario | Baseline | Current | Delta | Status |",
        "| --- | ---: | ---: | ---: | --- |",
    ]
    for comparison in comparisons:
        total = comparison.total
        lines.append(
            f"| {comparison.scenario_id} | {format_ms(total.baseline_ms)} | "
            f"{format_ms(total.current_ms)} | {format_delta(total)} | {total.status} |"
        )

    step_regressions = [
        (comparison.scenario_id, step)
        for comparison in comparisons
        for step in comparison.steps
        if step.status == "REGRESSED"
    ]
    lines.append("")
    if step_regressions:
        lines.append("### Step regressions")
        for scenario_id, step in step_regressions:
            lines.append(
                f"- `{scenario_id}` / `{step.label}`: "
                f"{format_ms(step.baseline_ms)} → {format_ms(step.current_ms)} ({format_delta(step)})"
            )
    else:
        lines.append("### Step regressions")
        lines.append("_None_")
    lines.append("")
    return "\n".join(lines)


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--current-dir", type=Path, required=True)
    parser.add_argument("--baseline-dir", type=Path, required=True)
    parser.add_argument("--tolerance", type=float, default=DEFAULT_TOLERANCE)
    parser.add_argument("--floor-ms", type=float, default=DEFAULT_FLOOR_MS)
    parser.add_argument("--markdown-out", type=Path, required=True)
    parser.add_argument(
        "--baseline-label",
        default="_none yet_",
        help="Human-readable baseline description for the comment header",
    )
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
    comparisons = compare_trees(args.current_dir, args.baseline_dir, args.tolerance, args.floor_ms)
    markdown = render_markdown(comparisons, args.baseline_label, args.tolerance, args.floor_ms)
    args.markdown_out.parent.mkdir(parents=True, exist_ok=True)
    args.markdown_out.write_text(markdown, encoding="utf-8")
    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary_path:
        with Path(summary_path).open("a", encoding="utf-8") as handle:
            handle.write(markdown)
            handle.write("\n")
    print(markdown)
    return 0


if __name__ == "__main__":
    sys.exit(main())
