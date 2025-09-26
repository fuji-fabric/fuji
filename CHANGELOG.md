> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>

# 📑 Changelog

## ✨ Added
- [core] feature: added `command suggestions` for `greedy command string`, which benefits the following commands: 
  - `/command-cooldown test`
  - `/command-attachment {attach-entity|attach-item|attach-block} ...`
  - `/command-permission describe`
  - `/command_debug`

## 🐛 Fixed
- **[core]**
    - The `--silent` and `--stdout` optional arguments can't be specified for `greedy command string` argument type.
