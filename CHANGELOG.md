> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/
  
- **[world] feature: new command `/world import`, to `import` an external `dimension directory`.**
- **[world] feature: new command `/world {load/unload}`, to `load` or `unload` a `runtime dimension`.**
- [world] feature: improve the safe location finder when deleting a dimension.
- [world] fix: the `/fuji reload` should `hot reload` the `difficulty`, `weather` and `time` of a dimension.
- [world.border] fix: ensure the `client-side world border` is synced after teleport.
- (MC 1.20~1.20.1) [world] fix: Should not spam the console with `failed to load random sequence salt...` when loading a runtime dimension. (This didn't actually affect or break things, but it spams the console.)
- [world] feature: now support the `debug world` features for `debug world` world preset. (If you create a debug world using `/world create debug minecraft:overworld --worldPresetType DEBUG_ALL_BLOCK_STATES`)
- [core] feature: improve the `style` of `doc strings`.
  - feature: now will colorize the `command` using `orange color`.
  - feature: now will use non-italic font to display `cold blcok`.
  - feature: now will highlight the `heading block`.
