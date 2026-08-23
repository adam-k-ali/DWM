# launchGame

Wait until the title screen is ready.

## Parameters

None.

## Usage examples

```yaml
- launchGame
```

## Notes

- Succeeds when the current screen is `TitleScreen`.
- Typical first step in every scenario.
- Uses the default per-step timeout (30s unless overridden).

## Related commands

- [createWorld](createWorld.md)
- [assertAndClick](assertAndClick.md)
- [startVanillaServer](startVanillaServer.md)
