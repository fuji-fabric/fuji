> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [world] feature: improve the `/world list` command.
  - feature: now sends the `loaded dimensions` in a compat-style using chat.
  - feature: now will display the `un-loaded dimensions` in the config.
- [world] fix: should not dupe the dimension descriptor entry in config, when trying to create a runtime dimension with dimension type `overworld_caves`.
- [world] fix: now `/world create` command use the `identifier` argument type for `dimension type` input, instead of greedy string. (It allows you to specify `optional arguments` at the tail.)
- [core] feature: improve the guide for new users.
  - feature: make the `echo` module enabled by default in the default config. (Its commands are required by many other modules)
  - feature: improve the `print_user_guide_in_console` option on server startup.
- [core] feature: improve the exception handlers.
- [fuji] feature: display the prefix string `fuji:` when inspecting fuji placeholders using `/fuji inspect placeholders`.
  - feature: now will report the `json syntax error` to the in-game command source, when using `/fuji reload` command.
  - feature: make the exception message more detailed.