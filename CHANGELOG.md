> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- **BREAKING CHANGES** [chat.stripe] refactor: the `chat.stripe` module is removed, its function is merged into `chat.style` module. (The original implementation of `chat.stripe` didn't work in online-mode server, with this new implementation, it does work!)
- **BREAKING CHANGES** [chat.rewrite] remove: the `chat.rewrite` module is removed, its function is broken. (The original implementation of `chat.rewrite` didn't work in online-mode server, it's broken, so we remove it.)
- [chat.mention] fix: make it working in online-mode server. (It works with `Styled Chat` mod now!)
- [chat.trigger] feature: new module `chat.trigger`, to make magic spells in chat. (It works with other chat-related mods, like `Styled Chat` mod!)
- [chat.style] feature: now will always stripe all tags in chat message. (It requires the corresponding permission for a player to use that tag.)
- docs: update contents for `chat` module and its sub-modules!
