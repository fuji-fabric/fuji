> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases


# Changelog

## Features

- [core] Introduced a new `event system`.
  - Refined the `injection points` of `events` to enhance `compatibility` with other mods.
  - Implemented on-demand `event registration`, registering only the necessary events.
  - Added the `/fuji inspect events` command to inspect all registered events.
- [core] Added the `/fuji inspect mixins` command to inspect all applied mixins.
- [jail] Improved compatibility between the `afk` and `jail` modules. The `afk` state is now displayed in the `tab list` for `jailed players`.
- [world.border & world.gamerule] Optimized performance of the `world.border` and `world.gamerule` modules.

---

## Fixes

- [core] Corrected behavior of bossbar tickets to ensure they are cancelled `on player damage` when specified.
- [warning] Resolved an issue where the `/warning create` command did not work.
- [placeholder] Fixed the custom formatter in the `%fuji:date [custom format]%` placeholder.