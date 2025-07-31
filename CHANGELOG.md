> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [warning] feature: add `confirm` optional argument to `/warning clear` command.
- [world] feature: now will fallback  to the `flat chunk generator` if there is no available chunk generator for the specified `dimension type`. (Makes the `/world create cave minecraft:overworld_caves` command work)
- [view] fix: prevent placing items into the `saddle` slot and `body` slots when using `/view inv <player>` command (These slots technically work but cause confusion, as they are not used for a player entity.)
- [core] feature: new `core.formatter.date_formatter` option allows users to customize the `date format`.
- [core] feature: explicitly specify the `UTF-8` encoding for all input streams. (To prevent the un-expected encoding is used for config files in some platforms.)
- [core] build: introduce the `error prone` checking framework in building flow, to check the possible errors at compile-time automatically.
- [core] fix: apply the `text parser input fixer` for MC version in the range `[1.20, 1.20.4)`, addressing an issue where `custom color enclosing tags` like `</#FF0000>` were not functioning correctly.
