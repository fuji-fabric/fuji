> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/



- **[world.border] feature: new `world.border` module, to support `per-dimension world border`.**
- [world] feature: improve the user interface of `/world info` command.
  - feature: now displays the `gamerules` in `hover text`.
  - feature: add new `border` info for this command.
- [world] feature: add `--confirm` argument for `/world {delete/reset}` command.
- [world] feature: add `/world who` command, to query the dimensions each player is in.
  - example: `/world who` to list all dimensions and their players.
  - example: `/world who <dimension>` to list the players of a specified dimension.
  - example: `/world who <player>` to list the dimension of a specified player.
- [world] refactor: now `/world tp` simply teleports to the `target dimension` with the same coordinate, instead of starting a rtp process. (The rtp is likely to failed, and cost too much. The `/world tp` command is an admin-level command for debug, if you really need a user-level rtp command, use `rtp` module.)
- [anti_build] fix: when using in `client-side`, the `place block` and `interact_entity` type will send the feedback message twice.
- [core] fix: make it possible to use `color.sign`, `color.anvil` modules when install the mod in `client side`.
- [core] feature: add new `always_use_built_in_docstrings` option. (To ensure you always sees `the latest version` of doc strings from the version you are using.)

