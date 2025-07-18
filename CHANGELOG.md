> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/
  
- [world.border] fix: ensure the `client-side world border` is synced after teleport.
- [world] fix: the `/fuji reload` should `hot reload` the `difficulty`, `weather` and `time` of a dimension.
- (MC 1.20~1.20.1) [world] fix: Should not spam the console with `failed to load random sequence salt...` when loading a runtime dimension. (This didn't actually affect or break things, but it spams the console.)
- [world] feature: now support the `debug world` features for `debug world` world preset. (If you create a debug world using `/world create debug minecraft:overworld --worldPresetType DEBUG_ALL_BLOCK_STATES`)
