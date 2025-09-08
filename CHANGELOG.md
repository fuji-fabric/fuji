> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
 
## Changelog

### [command_spy]
- **Enhancements**
  - Reworked the configuration system to use a **rule-based configuration** model, improving flexibility and readability.  
    *(Existing configurations will be migrated automatically.)*
    - Added `enable` option to easily toggle individual rules.
    - Added `acceptSilentCommand` option to respect vanilla silent command behavior.
    - Added `acceptPlayerCommandSource` and `acceptServerCommandSource` options for fine-grained control of command sources.
    - Added `notifyPlayersWithLevelPermission` option to notify eligible players in-game.
  - Improved performance by pre-compiling regex patterns.
  - Adjusted event timing for better compatibility with other mods.

- **Documentation**
  - Added detailed documentation on how the module works.

### [placeholder]
- **Documentation**
  - Added documentation for the `%fuji:date%` placeholder.
