> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
 

## Changelog

- [command_alias] fix: support to redirect a `literal command` into another `literal command`, like redirect `/wb` to `/workbench`.
  - Before: The redirect target command must have children command nodes.
  - Now: The redirect target command can be a leaf command node.
- [tab] feature: new `enable_header` and `enable_footer` options.
- [command_toolbox.reply] feature: new `/reply set-target <player>` command.
- [command_toolbox.glow] feature: new `/glow <entities>` command, to select a collection of entities as target.
  - Example: `/glow @e[type=pig,distance=..16]`
- [core] feature: new `argument type adapter` for `entities` argument type.
- [command_meta.NOT] feature: new `/NOT` command, to reverse the return value of `SUCCESS` and `FAILURE`.
  - Example: `/IF NOT has-item? <player> minecraft:apple 16 THEN say You don't have 16 apples. ELSE say You have 16 apples.`
- [command_advice] features and bug fixes
  - feature: improve the interactions of `cancellable advices` (`CANCEL_WITH_SUCCESS` and `CANCEL_WITH_FAILUE`) and `non-cancellable advices`. Now the `non-cancellable advice` will not be performed if the target command execution has already been canceled by a `cancellable command advice`.
  - feature: new command advice type: `CANCEL_IF_ANY_SUCCESS` and `CANCEL_IF_ALL_SUCCESS`.