> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/
  
**This version contains BREAKING CHANGES if you are using them!** 

- **(BREAKING-CHANGE)** [skin] refactor: re-write the skin module.
  - **(BREAKING CHANGE) remove: now remove the `/skin set` command. It is replaced with `/skin use-mojang-skin` and `/skin use-url-skin` command. (Makes it easier and clearer to use.)**
  - **(BREAKING CHANGE) refactor: the `/skin use-online-skin` is renamed to `/skin use-my-mojang-skin`.**
  - feature: now you can define the `default skin` for a new player, if the player haven't set any skin before.
  - feature: now will only log the console when creating new skin data for a player. (It reduces the `console-spam` when a player join the server.)
  - feature: new command `/skin gui` to open a `Skin GUI`. (Make it easier to use a defined `default skin`)
  - feature: new command `/skin use-random-default-skins`, to use a `random skin` from `default skin list` defined in the config file.
  - feature: new command `/skin use-default-skin <skinName>`, to use a `specified default skin` from `default skin list` defined in the config file.
  - docs: add more document for this module.
  - fix: the client should not get stuck in the dimension changing screen for a long time. (Now it is very fast to change the skin.)
  - fix: if the new skin is identical to the old skin, should not send the failure message. (The `failure message` in this case will mis-lead the user.)
  - fix: updated player skins were only visible after others re-join the server. 
  - fix: should display the player name in the player list, after the using of `/skin` command.
  - fix: should restore the previous `vehicle` and `passengers` of the player, after using the `/skin` command.
  - fix: the 1 block drifting after using the `/skin` command. (It's a minor visual glitch.)
  - feature: optimize the default language file for this module. (With intuitive styling.)
- [economy] feature: improvements for this module.
  - feature: now will display the `balance`, `accounts` and `providers` with defined colorful styling.
  - feature: now allow to customize the `icon` of `fuji economy provider`, instead of hard-coded `minecraft:cherry_sapling` icon.
- [core] docs: improvements for `document string compiler`.
  - docs: new `tips` and `examples` for modules.
  - docs: now the `Retarget Command Descriptor` will copy the document string from its target `Command Descriptor`.
  - docs: now will `highlight` the `Alex` and `Steve` literal in `document string`.
  - docs: now will `highlight` the `conditional statement`. (For example, `1.`, `1.a` and `1.a.i`...)
  - docs: now will `underline` the `URL` in document string.
  - fix: the `url` in `doc string` should be highlighted and displayed correctly, and it should be able to click to open the URL.
