> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

### Changelog

- **[command_cooldown]**
  - Added a confirmation prompt when executing the `/command-cooldown delete` command.

- **[core]**
  - Enhanced command suggestion and validation for the `duration` argument type.

- **[warning]**
  - **New command:** `/warning create-temp` for issuing temporal warnings.
  - **New event hooks:** Added support for defining commands for `on_permanent_warning_created` and `on_temporal_warning_created` events.
  - **Warning reminders:** Added automatic notifications to warned players upon joining the server.
  - **New placeholders:**
    - `%fuji:last_warning_created_date%`
    - `%fuji:last_warning_creator_name%`
    - `%fuji:last_warning_expiration_date%`
    - `%fuji:last_warning_reason%`  
      These display details of the most recent warning for a player.  
  - **Improved document** 
    - Improved the `default config file`.
    - Added new `examples` in the module inspector.
