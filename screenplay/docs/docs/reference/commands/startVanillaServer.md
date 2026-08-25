# startVanillaServer

Launch Mojang’s official dedicated-server jar as a child process (no Fabric /
mod loader on the server). Writes offline-mode superflat settings, waits until
`127.0.0.1` accepts TCP connections, and stops the process when the scenario
finishes.

## Parameters

| Parameter | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `port` | int | no | `25565` | Listen port (`1`–`65535`) |

## Usage examples

```yaml
- startVanillaServer
```

```yaml
- startVanillaServer:
    port: 25565
```

## Notes

- This step does **not** connect the client — drive Multiplayer UI separately
  (for example [`assertAndClick`](assertAndClick.md) +
  [`keyboardInput`](keyboardInput.md)).
- Uses a **120 second** timeout floor; `-PscreenplayTimeout` raises it when set
  higher.
- Allowed once per scenario.
- Run directory: `build/screenplay/vanilla-server/` (`server-jar.path`,
  `eula.txt`, `server.properties`, `world/`, `logs/harness.log`).
- Fails if the port is in use or the process dies before ready.

## Related commands

- [launchGame](launchGame.md)
- [keyboardInput](keyboardInput.md)
- [assertAndClick](assertAndClick.md)
