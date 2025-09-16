> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases


## Changelog

- [rtp] Enhance
  - feature: new `async_chunk_loading_timeout_ticks`, to specify the `waiting ticks before aborting this attempt`.
  - feature: support full `async chunk loading` and `async chunk generation`, making it `lag free`.
- [command_menu] feature: now `/command-menu open` command will open the `target menu` in `1 tick later`. (Making it easier to handle the `opening and closing` of `nested menus`)
- [command_toolbox.itemname] feature: new `/itemname {set|reset}` command, to modify the `custom name` of item in hand.