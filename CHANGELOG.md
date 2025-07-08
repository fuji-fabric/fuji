> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/


- **docs: fix typos in the document. (Thanks to our new contributor @pinarruiz)**
- docs: simplify the `pdf document`, and migrate most contents into the `in-game GUI`. (Now the `document` is built into the Minecraft game, and it's play-able.)
- docs: new `language` chapter, to describe how the new language system works.
- [core] feature: now you can insert `language instructions` in `language value`.
  - feature: if the `language value` starts with `[send-action-bar]`, then this `language key` is sending via the `action bar`.
  - feature: if the `language value` starts with `[send-main-title]`, then this `language key` is sending via the `main title`.
  - feature: if the `language value` starts with `[send-sub-title]`, then this `language key` is sending via the `sub title`.
  - feature: if the `language value` starts with `[suppress-sending]`, then the sending of this `language key` will be cancelled.
- [fuji] feature: new `/fuji inspect languages` command, to inspect `loaded language files` and who is using them.
- [core] feature: enhance the debug messages for language system. (Make the debug easier.)
- [core] feature: now will `sort` the language keys inside language files automatically.
- [fuji] feature: increase the color contrast of `colored candles`. (UX improvement)
- [fuji] feature: sort the `colored candles` by its `color`. (UX improvement)
- [chat.style & color.sign] fix: should not have the ability to use the `markdown` parser.
- [command_meta.when_online] fix: should display the raw command when there are style tags inside the command in `/when-online list` GUI.
- [command_meta.delay] fix: the re-entrance problem of `/delay` command when using `fuji` mod in `client-sdie`.

