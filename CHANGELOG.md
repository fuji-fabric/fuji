> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>


# 📑 Changelog

## ✨ Added

- **[command_bundle]**  
  Bundle commands now use the **last command’s return value** instead of always defaulting to `SUCCESS`.  
  This allows a bundle command to **rewrite a predicate command**, as a shortcut.