> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- **BREAKING CHANGES** [sit]: refactor: make the `command_toolbox.sit` module into a new standalone module `sit`, and change the configuration schema. (Due to this module is complex enough, and it's likely we will introduce new features into it.)
- ** BREAKING CHANGES** [chat.display] refactor: now use the `/chat display {item/inv/ender}` commands to create and share display GUI. (For a better compatibility with other chat-related mods, like `Styled Chat`, we stop replacing the chat string with display text directly.)
- [chat.history] refactor: a better implementation, with better compatibility and consistency. (Now it can be used with `Styled Chat` mod, and keeps the same chat style format when sending chat history.)
- [chat.display & chat.replace] fix: only use `chat.replace`  module to replace the `{item|inv|ender}` chat string into display placeholders. (If we use `chat.rewrite` module, it will break the message signature in online server.)
- (MC 1.20.1) [command_toolbox.sit] fix: the chair entity height offset is too small.
- [command_toolbox.sit] fix: if the binding block is broken, the chair entity didn't get killed.
- [command_toolbox.sit] feature: make the dis-mount position more sensible.
- [command_toolbox.heal] feature: now `/heal` command will fill the `food level`.
- [echo.send_broadcast] fix: the `/send-broadcast` command should not parse the player related placeholders. (If it's needed, use `/foreach` command.)
- [world_downloader] refactor: rename config keys in configuration.
- docs: new chapter `command` in document, to describe the command list.
- docs: new examples in `permission` chapter, to describe how to set up the proper permission for commands.

