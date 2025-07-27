> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [command_meta.if] feature: new module to provide the `/IF` command, which allows you to express `conditional logic`, such as `if ... then ... else ...`.
- [command_meta.nop] feature: new module to provide the `/nop` command. (It's useful to be combined with `/if` command.)
- [command_debug] feature: now will send the debug info to the command source, instead of the console.
- [command_meta.chain] fix: should not continue to execute the chained commands if the previous command is only `partial success`. (This affects the commands like `/execute if ... if ... if ...` combination.)