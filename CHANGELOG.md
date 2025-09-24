> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>

# Changelog
## Added
- [command_permission] feature: now will detect the change of `requirement` (if it's changed at runtime by other mods), and `auto re-wrap` it.
  - Before: If some other mods modifies the `requirements` of a command, you have to issue `/reload` to re-wrap it.
  - Now: This will be detected, and the target command will be re-wrapped automatically immediately (No need to wait until the first use).

## 🐛 Fixed
- **[command_bundle] & [command_alias]**
  - Hot-reload will forget the public command defined before when re-defining it.
    - Before: When re-defining a public command, you have to issue `/reload` command to hot-reload the new requirement.
    - Now: You can simply use `/fuji reload` command, to re-fine the command.
