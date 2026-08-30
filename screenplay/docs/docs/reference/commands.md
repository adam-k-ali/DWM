# Commands available

Built-in Screenplay steps and the shipped composite command. Each entry links
to a full reference page with parameters and YAML examples.

- [assertAndClick](commands/assertAndClick.md) — Assert a selector is visible, then click it (composite).
- [assertVisible](commands/assertVisible.md) — Wait until a matching widget or screen is visible.
- [captureScreenshot](commands/captureScreenshot.md) — Capture the current framebuffer to a PNG (optional compare vs CI baselines).
- [click](commands/click.md) — Wait for an active matching widget and left-click its center.
- [closeScreen](commands/closeScreen.md) — Close the current GUI / container screen.
- [createWorld](commands/createWorld.md) — Create a local world with the given settings (deterministic seed default) and wait until loaded.
- [debugScreen](commands/debugScreen.md) — Log the current screen class and every visible widget.
- [keyboardInput](commands/keyboardInput.md) — Type text into the focused edit box.
- [launchGame](commands/launchGame.md) — Wait until the title screen is ready.
- [interactWithEntity](commands/interactWithEntity.md) — Right-click interact with an in-world entity (crosshair or nearest).
- [lookAt](commands/lookAt.md) — Aim the camera by yaw/pitch or at block coordinates.
- [openInventory](commands/openInventory.md) — Open the player inventory GUI.
- [pressKey](commands/pressKey.md) — Press a keyboard key (keybinds, Escape, arrows).
- [runCommand](commands/runCommand.md) — Send an in-game slash command as the local player.
- [selectHotbar](commands/selectHotbar.md) — Select hotbar slot `0`–`8` and sync to the server.
- [setSneaking](commands/setSneaking.md) — Hold or release the client's sneak key.
- [startVanillaServer](commands/startVanillaServer.md) — Launch Mojang’s dedicated server as a child process.
- [useItem](commands/useItem.md) — Use the main-hand item on a block or in air.
- [waitTicks](commands/waitTicks.md) — Wait until the client world advances by N ticks.
- [waitUntil](commands/waitUntil.md) — Poll until a visibility, holding, or block condition is true.
- [walkUntil](commands/walkUntil.md) — Hold forward until a block position or dimension is reached.

See also: [Selectors](selectors.md) · [Writing scenarios](../writing-scenarios.md) · [Gradle plugin](gradle-plugin.md)
