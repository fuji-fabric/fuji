> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [core] feature: now you can insert `language instructions` in `language value`.
  - feature: if the `language value` starts with `[send-action-bar]`, then this `language key` is sending via the `action bar`.
  - feature: if the `language value` starts with `[send-main-title]`, then this `language key` is sending via the `main title`.
  - feature: if the `language value` starts with `[send-sub-title]`, then this `language key` is sending via the `sub title`.
  - feature: if the `language value` starts with `[suppress-sending]`, then the sending of this `language key` will be cancelled.
- [core] feature: enhance the debug messages for language system. (Make the debug easier.)
- [command_meta.when_online] fix: should display the raw command when there are style tags inside the command in `/when-online list` GUI.

