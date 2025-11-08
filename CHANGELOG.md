> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>


# 📑 Changelog

## Added
- [command_meta.run] Now the `/run as console <command>` will pass the return value whatever it is, instead of always defaulting to `SUCCESS`.


## Fixed
- [command_bundle] Now define a bundle command with empty definition will do nothing and return success, instead of throwing an exception.