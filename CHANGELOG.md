> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases

## Changelog

### [command_advice]
- **Enhancement**
  - Added `accept_player_command_source` and `accept_console_command_source` fields for `matcher` to make filtering command sources easier.
  - **Docs:** Added an example in the default config showing how to exempt a specific command with a specific target.

### [predicate]
- **Feature**
  - `/has-perm?` and `/has-level?` now support `offline player` as a target.

### [command_bundle]
- **Feature**
  - Added a new `/skull <player>` command example in the default config.
  - Added a new `/uuid <player>` command example in the default config.

```json
{
  "document": "This command will give the skull of specified player.",
  "requirement": {
    "level": 4,
    "string": null
  },
  "pattern": "skull <offline-player offline-player-arg>",
  "bundle": [
    "give %player:name% minecraft:player_head[minecraft:profile=$offline-player-arg]"
  ]
}
```

```json
{
  "document": "This command will display the UUID of specified player.",
  "requirement": {
    "level": 4,
    "string": null
  },
  "pattern": "uuid <player target>",
  "bundle": [
    "run as fake-op $target send-message %player:name% <yellow>The UUID of player $target is %fuji:escape player:uuid 2%"
  ]
}
```