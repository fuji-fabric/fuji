> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/
  
- [world.border & world.gamerule] docs: add document to explain how it works.
- [command_event] feature: optimize the `default config`.
- [command_bundle] improvements for this module
  - feature: new `document` field for `Bundle Command Descriptor`, and will provide the document in `default config`.
  - feature: optimize the `default config` for this module.
  - feature: new `/nbt {item/block/item}` in `default config` for this module.
  - feature: new `/gmsp` in `default config` for this module.
  - fix: the `/sun`, `/rain` and `/thunder` in the `default config` should use `/run as player` to switch the command source, to ensure the identical semantics.
- [core] feature: now will print the `argument type` first, then the `argument name` when inspecting a `command descriptor`.

