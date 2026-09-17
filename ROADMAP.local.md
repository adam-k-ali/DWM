## Roadmap

### Phase 0 — Docs that match reality (days)

Do this before any more features. Wrong docs are worse than missing docs.

| Change | Why |
| --- | --- |
| Split **“using Screenplay in your mod”** from **“developing Screenplay in the DWM repo”** | Current quick start mixes both |
| Replace `./screenplay/gradlew` with `./gradlew` | Copy-paste trap |
| Put **windowed local run first**; move xvfb to a CI page | Beginners have a monitor |
| Show a **complete Fabric `build.gradle` + `settings.gradle`**, including the actual repo (Modrinth Maven *or* “plugin not published yet — use includeBuild / GitHub”) | Stop lying by omission |
| Document the classpath/source-set requirement **until Phase 1 deletes it** | This is the #1 silent failure |
| Add a **“if it fails”** page: plugin not found, unknown scenario, no `$DISPLAY`, Java version, MC version | Beginners debug by Googling the error |
| Tab Fabric vs Forge/NeoForge; default Fabric | Most of this audience is Fabric |
| Host/brand the site as Screenplay, not under DWM | Discovery |

Success: a new Fabric example-mod can follow one page and get a screenshot without reading the plugin reference.

---

### Phase 1 — Convention over configuration (highest leverage)

Make the plugin do what DWM’s `build.gradle` does by hand.

**Plugin apply should:**

1. Detect Fabric / Forge / NeoForge (this already works when unambiguous).
2. **Add the matching runtime dependency** (`screenplay-fabric` / forge / neoforge) at a version that matches the plugin. Delete the user’s `runtimeOnly` line from the happy path.
3. **Create a `screenplayTests` source set** (or, better, load YAML from disk — see below) and wire it onto the Screenplay run classpath. Never onto `jar` / `remapJar`.
4. Create the Loom/Forge `screenplayClient` run if missing (partially done).
5. Make `screenplay { }` optional; today’s defaults are already almost enough.
6. If there are no YAML files, **write a starter** `createWorld.yaml` (or `myFirstTest.yaml`) so the first run does something.
7. `runScreenplay` with no `-Pscreenplay`: if one test exists, run it; if several, print their names and how to pick one. Do not default to `createWorld` (that currently hits a **bundled** demo and looks like success while ignoring the user’s files).
8. Fail fast with plain English: Java 25 required, Minecraft version mismatch, tests directory missing, xvfb missing **only when they asked for xvfb**.

**Load YAML from the filesystem, not only the classpath.** Pass `testsDir` as a system property and prefer that over `ClassLoader.getResources("tests")`. That removes source sets, production-jar contamination, and “unknown scenario” when discovery and the client disagree. Keep classpath loading for Screenplay’s own bundled composites (`assertAndClick`).

Success: a Fabric project’s install is:

```groovy
plugins {
    id 'net.fabricmc.fabric-loom' version '…'
    id 'com.adamkali.screenplay' version '1.0.0+26.2'
}
```

```bash
./gradlew runScreenplay
```

---

### Phase 2 — Put the bits where beginners already look

Until this ships, Phase 1 only helps people inside this repo.

| Deliverable | Notes |
| --- | --- |
| Publish the Gradle plugin to **Gradle Plugin Portal** | Same version as the loader jars (`1.0.0+26.2` or a portal-safe alias) |
| Publish loader jars to a **real Maven** (Portal companion, GitHub Packages, or documented Modrinth Maven) | Plugin can then add the dep without extra `repositories {}` |
| A **GitHub Action** `adam-k-ali/screenplay-action` (or a 15-line workflow snippet) | `xvfb`, JDK 25, `runScreenplayTests`, upload `build/screenplay/` |
| Optional: PR / gist against `fabric-example-mod` | This is how Fabric users learn |

Do not ask them to `includeBuild` Screenplay. That is a monorepo technique.

Success: `id 'com.adamkali.screenplay' version '…'` resolves on a clean machine with no `settings.gradle` surgery.

---

### Phase 3 — First-hour usage (writing tests)

Install is only half. Usage should feel like “click the Singleplayer button,” not “learn frontmatter types.”

**Recipes, not a command encyclopedia as the second click:**

1. Create a world and screenshot (already exists — make it the generated starter).
2. Open **my** screen (chest / custom GUI) and `assertVisible` a button.
3. Give an item, look at a block, `useItem`.
4. Fail, then use `debugScreen` to see real widget names.

Lead the writing guide with `debugScreen`. Widget `name` must match narration text exactly; that is the #1 authoring trap. The docs should say: *when a click fails, add `debugScreen` and copy the name from the log.*

**Friendlier runtime errors:** include visible widgets, current screen class, and “did you mean `createWorld` not `createWorld.yaml`?” `diagnostics.txt` is good for agents; the console line is what humans read.

**YAML schema** (`$schema` + json-schema) so VS Code/IntelliJ autocomplete step names and `createWorld` keys. Huge for people who do not memorize the command list.

**IntelliJ:** Loom already exposes a “Screenplay” run config. Document “green play button” before the terminal. Most of this audience never opens a terminal if the IDE can run it.

Defer: ServiceLoader primitives, suites, screenshot baselines, ffmpeg recording. Those are power-user pages.

---

### Phase 4 — CI that is copy-paste, not a research project

After one local green run:

- One workflow file in the docs: JDK 25, `xvfb`, `./gradlew runScreenplayTests -PscreenplayDisplay=xvfb`, upload screenshots + `report.xml`.
- Plugin: on Linux with no `$DISPLAY`, **suggest** xvfb in the error (you already fail fast); optionally auto-select xvfb when `CI=true`.
- Baselines stay optional and later. Do not mention `prepare-screenplay-baselines.sh` on the first CI page.

---

### Phase 5 — Stretch: a path with almost no Gradle

Only after 0–4. This is how they already use mods: jar in `mods/`.

Ideas, in order of sanity:

1. **`gradle init` / example repo** `screenplay-example-mod` — clone and run, no docs archaeology.
2. **In-game “run scenario”** when the Screenplay jar is in the run mods folder and YAML lives in `run/screenplay/` — good for learning, weak for CI.
3. **Architectury / MCDev template checkbox** “add Screenplay” — highest reach, more political/maintenance cost.

Do not lead with this. The plugin path is the product; this is on-ramp marketing.

---

## What not to optimize yet

- More loaders or more primitives — the audience never gets that far.
- Agent/CI baseline compare — already strong; it scares newcomers if it is in the quick start.
- Extending via `ServiceLoader` — keep it, bury it.
- Composite `includeBuild` docs — that is for you, not them.

---

## Suggested order of work

```text
Phase 0  docs truth          (this week)
Phase 1  plugin convention   (next; unblocks everyone including DWM)
Phase 2  publish plugin+jars (so outsiders can Phase 1)
Phase 3  recipes + errors + schema
Phase 4  copy-paste CI action
Phase 5  example repo / template  (optional)
```

Phase 1 without Phase 2 still pays off: DWM can drop the manual source-set/classpath wiring, and the docs stop describing a trap. Phase 2 without Phase 1 just publishes a tool that is still too fiddly.

**One metric:** time from “empty Fabric example-mod” to “PNG of a creative superflat world” for someone who has never heard of Screenplay. Target: under 15 minutes, three files touched (`build.gradle`, maybe `settings.gradle`, one YAML they did not have to invent).