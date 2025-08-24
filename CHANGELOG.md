> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/
> 
## Changelog

### [command_advice]
**Improvements and Fixes**
- **Fix:** Compatibility with Minecraft versions [1.20, 1.20.2].
- **Feature:** Optimized the default configuration schema. (Make it easier to understand.)
- **Feature:** Pre-compiled regex patterns to improve performance.
- **Feature:** Added new `CANCEL_WITH_FAILURE` command advice type. (Useful for return value consumers)
- **Feature:** Introduced `enable` property for `command advice` configuration. (Easier to switch)

---

### [core]
- **Feature:** The `--silent` optional argument now supports vanilla Minecraft feedback. (You can now use `--silent true` to suppress both the `fuji command feedbacks` and `vanilla Minecraft command feedbacks`.)
  - **Example:**
    - `/run as fake-op @s --silent true particle minecraft:heart ~ ~1 ~ 0.6 0.6 0.6 0 20 force %player:name%`
    - `/run as fake-op @s --silent false particle minecraft:heart ~ ~1 ~ 0.6 0.6 0.6 0 20 force %player:name%`

- **Feature:** The `/fuji reload` command can now regenerate the default configuration without requiring a server restart.
  - **Before:** Deleting configuration files would cause `/fuji reload` to rewrite in-memory data back to disk.
  - **Now:** Deleting configuration files and running `/fuji reload` will regenerate a fresh default configuration.

- **Fix:** The `command descriptor` can now correctly unregister an old command descriptor, even if the new one uses a different command pattern.
  - This mainly affects the `hot-reload` functionality in the `command_bundle` module.

---

### [command_alias]
**Improvements and Bug Fixes**
- **Feature:** Support for hot-reloading configuration via `/fuji reload` or `/reload`.
- **Feature:** Inspect all registered alias commands with `/fuji inspect fuji-commands` or `/fuji`.
- **Feature:** Improved console logging when registering alias commands.
- **Feature:** Enhanced error handling when the specified target command does not exist.
- **Feature:** New `requirement` property for `command alias` configuration.
  - **Before:** Requirements were implicitly inferred from the target command.
  - **Now:** Requirements can be explicitly specified, making them clear.
- **Feature:** New `document` property for `command alias` configuration, allowing documentation strings for each alias command.
- **Feature:** New `enable` property for `command alias` configuration, allowing easier toggling of commands.
- **Feature:** Added `/command-alias list` command to inspect all registered alias commands.  
