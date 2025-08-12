> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [core] feature: Improve the `properties naming` when inspecting the `argument types` in the inspector GUI.
- [back] fix: should not display the `/back {push|clear}` command suggestions when the command source has no permission to use them.
- [core] fix: when install this mod in `client side`, the `CleanTTLMapJob` should be registered if the user `re-join` the `single player world`.
- [command_permission] fix: when the user issue the `/reload` command, the newly registered commands should be wrapped by the `command_permission` module immediately, to prevent confusion. 
  - Although wrapping the `newly registered command nodes` in a `lazy manner` still works properly, it may prevent the client-side command tree being updated, which can cause confusion.
  - This can happen if a new command is registered into the server command tree (Via other `mods` or `/reload` command), but it never gets used before, then this `command node` will not be wrapped, until it's actually used. In this case, the client side command tree will not get updated, which may cause confusion.
- [core] feature: new `permission.json` file, to control the `default level permission` for each fuji commands.
  - before this version: the `default level permission` for each fuji command is defined and `hard-coded`.
  - after this version: the `default level permission` for each fuji command is generated into the `permission.json` file. You can modify this file, and use `/reload` to apply your changes.
  - This feature makes it easier to use this mod in `single-player` world, where there is no `Luckperms` mod installed.
