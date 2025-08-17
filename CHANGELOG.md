> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

## Changelog

- [command_schedule] improvements and fixes.
  - **fix: support specifying `multiple cron expressions` for a `job`.**
  - feature: rename fields in the config for clarity. (The old config schema will be migrated automatically)
  - feature: optimize the `default config` for this module, provide an intuitive default example, and make it easier to understand.
  - docs: add documentation explaining how this module works.
- [afk] feature: optimize the `default config` file.
- [command_meta.when_online] improvements
  - feature: add a new `/when-online` command as an alias for the `/when-online list` command.
  - docs: add documentation for commands.
