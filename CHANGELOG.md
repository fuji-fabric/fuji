> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>

# 📑 Changelog

## ✨ Added
- **[command_meta.run]**
    - Added command suggestions for: `/run as {player|console|fake-op}`
- **[command_meta.NOT]**
    - Added command suggestions for `/NOT`
- **[command_meta.delay]**
    - Added command suggestions for `/delay`
- **[command_meta.for_each]**
    - Added command suggestions for `/foreach`
- **[command_meta.when_online]**
    - Added command suggestions for `/when-online`

## 🐛 Fixed
- **[command_cooldown]**
    - Fixed an issue where `unnamed cooldown` rules were not matched in **top-down order** as expected.
