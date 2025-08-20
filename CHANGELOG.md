> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/


## Changelog

- [core] feature: improved data de-serialization, ensure all data types will generate a default config, if the user mis-delete it.
- [core] feature: enhanced `item string parser`, now you can specify `item with custom NBT data` as the icon in GUi.
  - You can use the same syntax as in `/give <item>` command, specify a custom `player head` in anywhere the item is used: `warp icon`, `head economy item`, `title item`, `works item`, `command menu slot item`...
  - Examples:
    - `/warp set-item city minecraft:player_head[minecraft:profile=Steve]` (Use a custom player head as the icon of a warp)
    - `/send-toast @s --icon "minecraft:player_head[minecraft:profile=Steve]" <rb>Hello World` (Used in toast icon)
    - `/send-dialog @s --yesButtonItem minecraft:player_head[minecraft:profile=Steve] <rb>Hello World`
- [color.anvil] fix: make this module works in `single-player world`, if the user installs this mod in client-side.