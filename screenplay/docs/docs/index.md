# Screenplay

Real-client Minecraft tests with CI screenshots — built for humans and agents.

Screenplay boots the **real Minecraft client**, drives deterministic YAML scenarios (UI clicks, world actions, commands), and captures **screenshots and JUnit reports** you can inspect in CI or feed to coding agents.

## Why not GameTest alone?

Fabric GameTests (and similar server harnesses) are excellent for headless, server-authoritative logic. They do not exercise the full client: title screens, widgets, create-world flows, or what the player actually sees.

Screenplay fills that gap with a Playwright-style client harness — scriptable, repeatable, and artifact-rich.

## Features

- YAML scenarios and composite commands (`type: test` / `type: command`)
- Widget and world primitives (click, assertVisible, createWorld, walkUntil, useItem, and more)
- JUnit XML reports, step timing metrics, diagnostics dumps
- Screenshot capture for CI and agent review
- `xvfb` display mode for headless Linux CI
- Gradle plugin (`com.adamkali.screenplay`) on the [Plugin Portal](https://plugins.gradle.org/plugin/com.adamkali.screenplay)
- **Fabric**, **Forge**, and **NeoForge** loader artifacts

## Get started

1. Follow the [Quick start](quickstart.md) to wire the plugin and run your first scenario.
2. Browse [Commands available](reference/commands.md) for the full API reference.
3. See [Running tests](running-tests.md) for display modes, timeouts, and CI outputs.

## Loaders

| Loader | Artifact |
| --- | --- |
| Fabric | `screenplay-fabric` |
| Forge | `screenplay-forge` |
| NeoForge | `screenplay-neoforge` |
