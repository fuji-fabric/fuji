> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases


## Changelog

### [system_message]
- **Enhancement**
  - **Improved the config schema (old config files will be migrated automatically).**
  - **Added support for using contextual player placeholders when modifying text.**
  - **Added support for custom styles when modifying text.**
  - Fine-tuned the mixin to improve `compatibility` with other mods.
  - Introduced a new `enable` property for each rule.
  
The new default config
```json

{
  "rules": [
    {
      "enable": false,
      "document": "Modify the style of player joined text.",
      "is_screen_text": false,
      "translatable_text_key": "multiplayer.player.joined",
      "translatable_text_value": "<green>[+] Player %s joined the server."
    },
    {
      "enable": false,
      "document": "Cancel the sending of player left text.",
      "is_screen_text": false,
      "translatable_text_key": "multiplayer.player.left",
      "translatable_text_value": null
    },
    {
      "enable": false,
      "document": "Modify the player death message.",
      "is_screen_text": false,
      "translatable_text_key": "death.attack.explosion.player",
      "translatable_text_value": "<rainbow>%1$s booooooom because of %2$s"
    },
    {
      "enable": false,
      "document": "Modify the player death message.",
      "is_screen_text": false,
      "translatable_text_key": "death.attack.fall",
      "translatable_text_value": "<rb>%1$s hit the ground too hard"
    },
    {
      "enable": false,
      "document": "Modify the player death message.",
      "is_screen_text": false,
      "translatable_text_key": "death.fell.accident.generic",
      "translatable_text_value": "<rb>%1$s fell from a high place"
    },
    {
      "enable": true,
      "document": "Modify the style of `/seed` command feedback.",
      "is_screen_text": false,
      "translatable_text_key": "commands.seed.success",
      "translatable_text_value": "<rainbow>Seeeeeeeeeeed: %s"
    },
    {
      "enable": false,
      "document": "Modify the text displaying in the whitelist screen.",
      "is_screen_text": true,
      "translatable_text_key": "multiplayer.disconnect.not_whitelisted",
      "translatable_text_value": "<rainbow>Please apply a whitelist first!"
    },
    {
      "enable": false,
      "document": "Modify the text displaying in the ban screen.",
      "is_screen_text": true,
      "translatable_text_key": "multiplayer.disconnect.banned",
      "translatable_text_value": "<red><b><i>You are banned from this server"
    },
    {
      "enable": false,
      "document": "Modify the text displaying in the ban screen.",
      "is_screen_text": true,
      "translatable_text_key": "multiplayer.disconnect.banned.reason",
      "translatable_text_value": "<red><b><i>You are banned from this server<newline><yellow>Reason: %s"
    },
    {
      "enable": false,
      "document": "Modify the text displaying in chest screen.",
      "is_screen_text": true,
      "translatable_text_key": "container.chest",
      "translatable_text_value": "<rb>I see you opening the chest!"
    }
  ],
  "MOD_VERSION": "12.48.0"
}
```