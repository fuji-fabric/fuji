> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>

# 📑 Changelog

## ✨ Added
- **[command_permission]**
  - Now automatically detects when the `requirement` of a command is changed at runtime by other mods.
    - **Before:** If another mod modified the requirement, you had to run `/reload` to re-wrap the command.
    - **Now:** Changes are detected instantly, and the command is re-wrapped automatically (no waiting until first use).

## 🐛 Fixed
- **[command_bundle] & [command_alias]**
  - Fixed an issue where hot-reloading would forget previously defined public commands when re-defining them.
    - **Before:** Re-defining a public command required running `/reload` to load the new requirement.
    - **Now:** Just run `/fuji reload` to refine the command immediately.  
