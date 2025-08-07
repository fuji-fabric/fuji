> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [chat.trigger] feature: slightly performance improve.
- [chat.mention] fix: should display the `mentioned players` properly when there are `common name prefix` in the `mentioned players`.
- [chat.display] feature: now allow the player to modify their inventory when viewing other's display GUI.
- [chat.spy] feature: improve the `debug logging` for this module, making it easier to find out the `message type` of the `target message`.
- [chat.history] improvements and fixes
  - feature: improve the `debug loggins` and `config fields`, making it clearer.
  - fix: the chat history module didn't work in `offline-mode server` when `MC >= 1.21.5`.
- [core] now will attach `MOD_VERSION` property for each `config file`.
