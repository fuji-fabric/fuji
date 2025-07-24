> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/
  
- [skin] refactor: re-write the skin module.
  - feature: now you can define the `default skin` for a new player, if the player haven't set any skin before.
  - feature: now will only log the console when creating new skin data for a player. (It reduces the `console-spam` when a player join the server.)
  - feature: new command `/skin use-random-default-skins`, to use a `random skin` from `default skin list` defined in the config file.
  - feature: new command `/skin use-default skin <skinName>`, to use a `specified default skin` from `default skin list` defined in the config file.
  - docs: add more document for this module.
  - fix: if the new skin is identical to the old skin, should not send the failure message. (The `failure message` in this case will mis-lead the user.)
- [economy] feature: improvements for this module.
  - feature: now will display the `balance`, `accounts` and `providers` with defined colorful styling.
  - feature: now allow to customize the `icon` of `fuji economy provider`, instead of hard-coded `minecraft:cherry_sapling` icon.
- [core] docs: improvements for `document string compiler`.
  - docs: new `tips` and `examples` for modules.
  - docs: now will `highlight` the `Alex` and `Steve` literal in `document string`.
  - docs: now will `highlight` the `conditional statement`. (For example, `1.`, `1.a` and `1.a.i`...)
  - docs: now will `underline` the `URL` in document string.
  - fix: the `url` in `doc string` should be highlighted and displayed correctly, and it should be able to click to open the URL.
