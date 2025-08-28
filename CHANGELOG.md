> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases


## Changelog

- [command_toolbox.apply_velocity] feature: new module to provide `/apply-velocity` command.
- [command_toolbox.{burn/freeze}] feature: `/burn` and `/freeze` commands now support to select a collection of entities as target.
- [home] improvements
  - feature: new `/home rename <old-name> <new-name>` command.
  - feature: re-open the Home GUI after the `right click`, for convenience.
- [kit] improvements
  - feature: new `/kit preview <player> <kit>` command, to pre-view the contents of a kit in GUI.
- [command_toolbox.more] feature: new optional argument `--oversize` for `/more` command.
  - Example: `/more --oversize true` will set the item count to 64, even for items like `diamond sword` or `ender peral`.
