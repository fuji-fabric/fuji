> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases

# Changelog

- [core] feature: improvements to `permission.json` file.
  - feature: auto clean the invalid keys in `permission.json` file, for clarity.
  - feature: sort the `command path` alphabetically, making it easier to read.
- [command_cooldown] feature: enhanced `/command-cooldown list` command, now it also list the `unnamed cooldown` in details.
- [core] feature: improve the `game profile` fetch function.
  - feature: greatly improve the `player skull` responsiveness of `deathlog GUI`, `balance-top GUI`, `warnings GUI`, `about GUI`
  - feature: now the `/fuji inspect languages` GUI will display the player skull with the skin used by the player.
- [deathlog] feature: now will display the `player skull` instead of `skeleton skull` in `/deathlog` GUI.
- [tpa] improvements
  - feature: new `/tpa gui` command.
  - feature: new `/tpa` command as alias to `/tpa gui`.
- [command_menu] feature: new `other_indexes` option for `slot descriptor`.
  - Now you can specify `other indexes` for a `slot`, except the primary `index`.
  - Making it easier to `mirror` a slot, and fill the GUI space.