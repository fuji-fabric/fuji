> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [fuji] feature: connect the `/fuji inspect modules` command with other `inspection commands`.
  - feature: now you can `click to jump` to the `configs`, `commands`, `permissions`, `metas`, `placeholders` and `argument types` provided by `this module`.
  - feature: now adds the `core` as a `dummy module` in `/fuji inspect modules` command, for convenience.
- [fuji] feature: now will display `color boxes` when you `click` a `module` in `/fuji inspect modules` command. (The colorbox can be `example`, `tips`, `info`, `warning` and `danger` messages)
- [core] fix: should remember the `parent GUI` when using the `search function` in `paged GUI`. (This makes the GUI interaction more convenient, and the possibility for `recursive search`.)
- [fuji] fix: should not display the `static` fields in `/fuji inspect configurations` command. (To hides the `not interested` fields.)
- [fuji] feature: now will use `different items` and `glowing visual effect` to represent `argument types` from `vanilla Minecraft` and `fuji`. (For `/fuji inspect argument-types` command)
