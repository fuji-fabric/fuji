> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
 
# Changelog

## Features

- [command_spy] Enhanced the `command spy` module
  - feature: improve the `flexibility` and `readbility` of `configuration`, now using a `rule-based config`. (**The old config will be migrated automatically.**)
    - New `enable` option to switch a `rule` easily.
    - New `acceptSilentCommand` option, to respect vanilla `silent` command feature.
    - New `acceptPlayerCommandSource` and `acceptServerCommandSource` to configure `command source` easily.
    - New `notifyPlayersWithLevelPermission` option, to `notify` online players in-game.
  - feature: improve the `performance` by pre-compile the regex patterns.
  - feature: fine-tune the `event timing` for better `compatibility` with other mods.
  - docs: document how this module works.

