> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/
  
- **[command_menu] feature: now we have an `online menu editor`, which greatly helps you design a menu. (Thanks to @Hitnam)**
  - You can use the editor in https://fuji-command-menu-editor-k4k4.vercel.app/
- [motd] feature: improvements to `motd` module.
  - feature: now allow to customize the `players info` area. (You can customize the `max players`, `online players` and `hover text`.)
  - feature: optimize the `default config file` for this module. (Now it provide the fancy gradient colors in the default example)
- [tab] feature: optimize the `defualt config file` for this module. (Now it provide a better color schema in the default example)
- [fuji] feature: now allows the `console` to execute `/fuji {reload/about/debug}` commands.
- [core] feature: improvements to `language` system.
  - feature: increase the number of supported languages from `30` to all `128` languages. (even if the language is not built-in, users can now still use it.)
  - **feature: the `language files` now will be reloaded by `/fuji reload` command, regardless of whether the `language` module is enabled or not.**
  - fix: when `language` module is enabled, and a player joins with an `unsupported language` the first time, the console should not be spammed with exceptions.
- [docs] feature: improvements to `document` system.
  - feature: add `click prompt` for `core` module in module details GUI.