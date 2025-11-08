> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>

# 📑 Changelog

## 🆕 Added
- **[command_meta.run]**  
  `/run as console <command>` now returns the actual command result, rather than always defaulting to `SUCCESS`.

- **[command_toolbox.nickname]**  
  Introduced a new option: `nickname_constraints`.

## 🐞 Fixed
- **[command_bundle]**  
  Defining a bundle command with an empty definition no longer throws an exception.  
  It now safely does nothing and returns `SUCCESS` instead.
