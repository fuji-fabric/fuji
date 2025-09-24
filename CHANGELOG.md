> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>

# Changelog

## 🚀 Added
- **[command_bundle]**
  - Introduced an `enable` property for each bundle command — making configuration more flexible.
- **[core]**
  - Improved **command registration points** for better compatibility across modules:
    - `command_alias`
    - `command_bundle`
    - `command_permission`
  - Console now shows a warning when a **command registration override** is detected.
- **[command_event]**
  - Added new server lifecycle events:
    - `on_server_started`
    - `on_server_stopping`

## 🐛 Fixed
- **[command_bundle] & [command_alias]**
  - Hot-reload now properly **unregisters target commands** before reloading.  
