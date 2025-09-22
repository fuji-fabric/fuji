> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>


## Changelog

- [home] feature: new `/home` root command, to teleport to any of the player's homes.
- [command_rewrite] feature: pre-compile the `regex` for better performance.
- [cleaner] fix: a possible deadlock while iterating the entities in a world.
- [nametag] Improves
  - feature: a better implementation to handle the `virtual nametag entity` sync.
  - fix: the `nametag entity` de-sync after the `/skin` command.
  - feature: improve the `nametag hiding` mechanism.
    - Before: switch from `virtual nametag entity` to `vanilla nametag` when a player is `sneaking` or `invisible`.
    - Now: hide the `virtual nametag entity` when a player is `sneaking` or `invisible`.
  - feature: improve the `responsiveness` of `nametag hiding and showing`.