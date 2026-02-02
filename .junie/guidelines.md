# Project Guidelines: Doctor Who Mod (DWM)

## Project Overview
DWM is a Minecraft mod built for the Fabric loader that brings elements of the "Doctor Who" universe into Minecraft. It features TARDIS mechanics, custom items, blocks, and sounds.

## Technical Stack
- **Game Version:** Minecraft 1.21.x (implied by Fabric Loom 1.9 and Java 21)
- **Mod Loader:** Fabric
- **Language:** Java 21
- **Build System:** Gradle
- **Key Libraries:**
  - Fabric API
  - Cloth Config (Configuration UI)
  - ModMenu (Mod list integration)
  - Mixpanel (Analytics)

## Project Structure
The project uses a split source set approach:
- `src/main/java`: Common mod logic, blocks, items, networking, and TARDIS data management.
- `src/client/java`: Client-only logic, including rendering (models, states), GUI, and client-side networking.
- `src/test/java`: JUnit tests for TARDIS logic and networking.
- `src/main/resources`: Assets (lang, data, structures, worldgen) and Fabric metadata.
- `src/client/resources`: Client-side assets (models, textures, blockstates, sounds).

## Key Components
- `DWMMain`: Main entry point for the mod.
- `TardisDataLoader`: Manages saving and loading of TARDIS data.
- `DWMConfig`: Handles mod configuration via Cloth Config.
- `ServerPayloadTypeRegistry`: Manages custom networking packets.

## Development Guidelines
- **Code Style:** Follow standard Java naming conventions and match the existing codebase's style.
- **Testing:**
  - Run tests using `./gradlew test`.
  - Use JUnit 5 and Mockito for unit testing.
  - Use Game Tests for integration testing.
    - Run using `./gradlew runGametestServer`
    - Placed in `src/gametest/java`
    - You need to register test entry points in `fabric.mod.json`
- **Build:** Build the project using `./gradlew build`.
- **Data Generation:** The project uses Fabric's Data Generation API. Run `./gradlew runDatagen` if changes to data-driven files are needed.
