> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>


## Changelog
**NOTE: This is the `re-publish` of `v12.51.0`.**

### [document]
- **Feature:** Introduced a new online documentation site at [https://fuji-fabric.github.io](https://fuji-fabric.github.io).
- **Feature:** Added the `/document build` command to compile Markdown document files.

### [teleport_warmup]
- **Feature:** Admin players can now bypass the teleport warmup.
- **Feature:** Entities with `age <= 3` can now bypass the teleport warmup, improving usability for teleporting immediately after respawn.

### [command_cooldown]
- **Documentation:** Added information regarding the `bypass mechanism`.

### [command_warmup]
- **Documentation:** Added information regarding the `bypass mechanism`.

### [command_spy]
- **Feature:** Added a `document` property for each rule.
- **Feature:** Optimized the default configuration by including an example for the `ignore` property.

### [rto]
- **Fix:** The displaying of `current attempts count`. (Should start with `1` instead of `0`)