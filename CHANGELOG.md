> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

## Changelog

**In this version, the command system undergoes a full code review, and many legacy bugs are fixed.**
**Besides that, a new `permission.json` file is introduced, to support the permission configuration, without the `LuckPerms` mod. (See below)**

### Core
- **Feature:** Improved property naming when inspecting *argument types* in the Inspector GUI.
- **Feature:** Added `permission.json` file to configure the **default permission level** for each Fuji command.
  - **Before:** Default permission levels were hard-coded.
  - **Now:** Permission levels are stored in `permission.json`, which can be edited and reloaded with `/reload`.
  - Especially useful in single-player worlds without the *LuckPerms* mod.
- **Feature:** Inspector now displays the **documentation string** for `config handler` objects.
- **Fix:** When installed client-side, the `CleanTTLMapJob` is now correctly re-registered after rejoining a single-player world.

### Back
- **Fix:** `/back {push|clear}` command suggestions are no longer shown if the player lacks permission to use them.

### Command Permission
- **Fix:** `/reload` now immediately wraps newly registered commands in the `command_permission` module, ensuring the client command tree updates properly.
  - Previously, lazy wrapping delayed this process until a command was used, causing potential client-side command tree desync. (After the `/reload` command, the users need to re-join the server, to re-fresh the client-side command tree)

### Command Bundle
- **Fix:** User-defined variable values are now injected correctly even if other variables share a common name prefix.
