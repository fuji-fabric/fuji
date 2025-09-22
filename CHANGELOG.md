> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>

# Changelog

## Added
- **[home]** Introduced the `/home` root command, allowing players to teleport to any of their homes.
- **[command_rewrite]** Pre-compilation of `regex` patterns for improved performance.
- **[nametag]**
  - **Implemented a more robust synchronization mechanism for `virtual nametag entities`.**
  - Enhanced the `nametag hiding` system:
    - **Before:** Switched from `virtual nametag entity` to the `vanilla nametag` when a player was `sneaking` or `invisible`.
    - **Now:** Directly hides the `virtual nametag entity` when a player is `sneaking` or `invisible`.
  - Improved responsiveness when hiding and showing nametags.
- **[document]** Sidebar entries are now displayed in alphabetical order.

## Fixed
- **[cleaner]** Resolved a potential deadlock occurring while iterating over entities in a world.
- **[nametag]**
  - Addressed `nametag entity` desynchronization after executing the `/skin` command.
- **[jail]** Ensured that a new jail patrol job is scheduled immediately after a jail is created via the `/jail create` command.
