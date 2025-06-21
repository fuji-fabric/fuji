> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- feature: the new `command_menu` module. (Inspired by `DeluxeMenus` bukkit plugin)
  - feature: new command `/command-menu open <player> <menu-name>`.
  - feature: new command `/command-menu close <player>`.
  - feature: new command event `onShiftAndSwapHandsEvent`.
  - feature: full-featured with `nested menus`, `placeholders`, `different click types` and `slot requirements`.
- [predicate] fix: all the `predicate` commands should not send the `boolean output value` as feedback, to avoid `message spam`.
 