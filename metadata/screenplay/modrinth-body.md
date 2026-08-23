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

Agents can run `./gradlew runScreenplay` / `runScreenplayTests`, read `report.xml` / `metrics.json` / screenshots, and iterate without a human babysitting a GUI. Deterministic options, wiped saves, and stable scenario IDs keep runs reproducible.

## Quick start (Fabric)

1. Apply the plugin and depend on the Fabric artifact:

```groovy
plugins {
    id 'com.adamkali.screenplay' version '<version>'
}

dependencies {
    runtimeOnly "com.adamkali.screenplay:screenplay-fabric:<version>"
}

screenplay {
    loader = 'fabric'
    testsDir = file('src/screenplayTests/resources/tests')
}
```

2. Add a scenario under `tests/`:

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

3. Run it:

```bash
./gradlew runScreenplay -Pscreenplay=createWorld -PscreenplayDisplay=xvfb
```

Forge and NeoForge use the same plugin with `loader = 'forge'` or `loader = 'neoforge'` and the matching `screenplay-forge` / `screenplay-neoforge` artifact.

## Roadmap

Screenplay already covers interactive client scenarios and screenshot capture. Planned expansions include broader scenario coverage and first-class screenshot / visual regression helpers — without changing the YAML-first workflow.

## Loaders

| Loader | Artifact |
| --- | --- |
| Fabric | `screenplay-fabric` |
| Forge | `screenplay-forge` |
| NeoForge | `screenplay-neoforge` |
