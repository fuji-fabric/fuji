> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>



# Changelog

# Added
- [command_bundle] feature: add `enable` property for each `bundle command`, making it easier to configure.
- [core] feature: fine-tune the `command registration point` for better compatibility, include: `command_alias`, `command_bundle`, `command_permission` modules.
- [core] feature: now will emit a console warning if a `command registration overriding` is detected.

# Fixed
- [command_bundle && command_alias] fix: the `hot-reload` feature should `un-register` the target command.
