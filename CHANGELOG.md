> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

# Changelog

- **[command_advice]** improvements and fixes
    - **feature:** optimize the default config schema.
    - **fix:** make it working in MC version [1.20, 1.20.2].
    - **feature:** Pre-compile the regex pattern to improve performance.
    - **feature:** New `CANCEL_WITH_FAILURE` command advice type.
    - **feature:** New `enable` property for `command advice` configuration.
- **[core] feature:** now the `--silent` optional argument supports the `vanilla Minecraft feedback`.
  - Example: 
    - `/run as fake-op @s --silent true particle minecraft:heart ~ ~1 ~ 0.6 0.6 0.6 0 20 force %player:name%`
    - `/run as fake-op @s --silent false particle minecraft:heart ~ ~1 ~ 0.6 0.6 0.6 0 20 force %player:name%`
- **[core] feature**: now the `/fuji reload` command can re-generate the default config without the server restart.
  - Before: If you delete the files in disk, the `/fuji reload` will re-write the `in-memory data` into the disk.
  - Now: You can delete the files in disk, and issue `/fuji reload` command to re-generate a default one.
- **[core] fix:** the `command descriptor` should be able to `un-register` an old command descriptor, even the new one has different command pattern compared to the old one. (This mainly affects the `hot-reload` feature in `command_bundle` module.)
