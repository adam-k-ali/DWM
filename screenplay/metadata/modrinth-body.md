# Screenplay

Real-client Minecraft tests with CI screenshots — built for humans and agents.

**Docs:** [https://adam-k-ali.github.io/DWM/](https://adam-k-ali.github.io/DWM/) (quick start + command API reference)

Screenplay boots the **real Minecraft client**, drives deterministic YAML scenarios (UI clicks, world actions, commands), and captures **screenshots and JUnit reports** you can inspect in CI or feed to coding agents.

## Why not GameTest alone?

Fabric GameTests (and similar server harnesses) are excellent for headless, server-authoritative logic. They do not exercise the full client: title screens, widgets, create-world flows, or what the player actually sees.

Screenplay fills that gap with a Playwright-style client harness — scriptable, repeatable, and artifact-rich.

## Features

- YAML scenarios and composite commands (`type: test` / `type: command`)
- Widget and world primitives (click, assertVisible, createWorld, walkUntil, useItem, …)
- JUnit XML reports, step timing metrics, diagnostics dumps
- Screenshot capture for CI and agent review
- `xvfb` display mode for headless Linux CI
- Gradle plugin (`com.adamkali.screenplay`) for prepare / run / run-all
- **Fabric**, **Forge**, and **NeoForge** loader artifacts

## Agent-friendly

Agents can run `./screenplay/gradlew runScreenplay` / `runScreenplayTests`, read `report.xml` / `metrics.json` / screenshots, and iterate without a human babysitting a GUI. Deterministic options, wiped saves, and stable scenario IDs keep runs reproducible.

## Quick start (Fabric)

1. Apply the plugin next to Loom (no extra dependency line):

```groovy
plugins {
    id 'net.fabricmc.fabric-loom' version '<loom-version>'
    id 'com.adamkali.screenplay' version '<version>'
}
```

2. Add a scenario under `src/screenplayTests/resources/tests/` (or run once and let the plugin write `myFirstTest.yaml`):

```yaml
---
name: Create a flat world
type: test
---
steps:
  - launchGame
  - createWorld:
      worldType: flat
      gameMode: creative
  - captureScreenshot:
      name: world-ready.png
```

3. Run it (windowed by default; add `-Pscreenplay=<id>` when you have more than one test):

```bash
./gradlew runScreenplay
```

Forge and NeoForge use the same plugin; it selects `screenplay-forge` / `screenplay-neoforge` from the loader plugin.

## Roadmap

Screenplay already covers interactive client scenarios, screenshot capture, and
optional pixel compare against CI baselines from `main`. Planned expansions include
broader scenario coverage — without changing the YAML-first workflow.

## Loaders

| Loader | Artifact |
| --- | --- |
| Fabric | `screenplay-fabric` |
| Forge | `screenplay-forge` |
| NeoForge | `screenplay-neoforge` |
