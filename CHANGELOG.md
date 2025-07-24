> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/
  
- [skin] refactor: re-write the skin module.
  - feature: now you can define the `default skin` for a new player, if the player haven't set any skin before.
  - fix: if the new skin is identical to the old skin, should not send the failure message. (The `failure message` in this case will mis-lead the user.)
  - feature: now will only log the console when creating new skin data for a player. (It reduces the `console-spam` when a player join the server.)
- [core] feature: now will highlight the `Alex` and `Steve` literal in `document string`.
