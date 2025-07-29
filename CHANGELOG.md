> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [jail] feature: new `jail` module, to provide an alternative solution to `ban`. 
  - feature: new `/jail` commands.
    - To create/delete/list jails: `/jail {create/delete/list}`
    - To put/un-put a player into/from a jail: `/jail {put/un-put}`
    - To set the position and tp: `/jail {set-position/tp}`
    - To query where the player is in: `/jail where`
  - feature: for each `jail`, you can define the `onJailCommands` and `onUnjailedCommands`.
    - You can integrate with `anti_build` module, to limit the actions of jailed players: `break blocks`, `place blocks`, `interact with item`, `interact with entities` and `interact with blocks`.
  - feature: for each `jail`, you can define the `patrolCommands`.
    - You can restrict the `movement` of jailed players with-in a specified area. (This is the default `patrol commands`.)
    - You can define your own `patrol commands` to do more checks and restrictions.
  - feature: for each `jail`, you can display information using `placeholders`: `jail id`, `jail display name`, `creator name`, `created time`, `specified jail duration`, `remaining jail duration`, `jail dimension`, `jail x`, `jail y`, `jail z`, `jail yaw`, `jail pitch`.
  - feature: for each `jail`, you can define whether to decrease the `remaining jail seconds` when the `prisoner` is `offline`.

