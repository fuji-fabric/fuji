> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>

# Changelog

## 🐛 Fixed
- **[command_bundle] & [command_alias]**
  - Hot-reload now properly will forget the public command defined before when re-defining it.
    - Before: When re-defining a public command, you have to issue `/reload` command to hot-reload the new requirement.
    - Now: You can simply use `/fuji reload` command, to re-fine the command.
