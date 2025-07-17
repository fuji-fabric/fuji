> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- feature: improve the guide for new users.
  - feature: make the `echo` module enabled by default in the default config. (Its commands are required by many other modules)
  - feature: improve the `print_user_guide_in_console` option on server startup.
- feature: display the prefix string `fuji:` when inspecting fuji placeholders using `/fuji inspect placeholders`.
- feature: improve the exception handlers.
  - feature: now will report the `json syntax error` to the in-game command source, when using `/fuji reload` command.
  - feature: make the exception message more detailed.