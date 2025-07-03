> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [note] feature: enhance the `note` module.
  - feature: add new commands: `/note {create/list/clear/clear-all/gui}`.
  - feature: add `note rule` to execute `publishment commands`.
- [command_meta.when_online] feature: new module `command_meta.when_online`, to provide the `/when-online <player> <cmd>` command. (Execute the command exactly once when the target player online.)
- [core] feature: now will always `log the console` when failed to execute commands in `command executor`. (Make it easier to debug.)
