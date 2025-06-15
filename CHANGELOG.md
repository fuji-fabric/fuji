> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- **BREAKING CHANGES** [chat.stripe] refactor: the `chat.stripe` module is removed, its function is merged into `chat.style` module. (The original implementation of `chat.stripe` didn't work in online-mode server, with this new implementation, it does work.)
- [chat.mention] fix: make it working in online-mode server.
- [chat.style] feature: now will always stripe all tags in chat message. (It requires the corresponding permission for a player to use that tag.)
