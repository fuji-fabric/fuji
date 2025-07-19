> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/
  
- [command_cooldown] improvements for `command_cooldown` module:
  - docs: provide more doc strings and examples to describe how to use this module.
  - feature: new `/command-cooldown try-use` command. (Now you can define a `default command instance` in the config file, and `test` it with `/command-cooldown try-use` command. It provides a simple way to define a named-cooldown.)
  - feature: improve the `error text` for `command cooldown placeholders`.
- docs: more document for modules.
  - [command_bundle] docs: provide the detailed document for the DSL and more examples.
  - [command_menu] docs: provide definition for menu and slots, and how to design a nested menu.
- [core] docs: improve the doc string compiler.
  - feature: now it will highlight the `vanilla Minecraft target selector`.
