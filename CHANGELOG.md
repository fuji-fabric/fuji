> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

**This version is the `Quality of Life` improvement version.**

- [command_warmup] fix: should not `send warning` or `send bossbar`, if the player can't even use that target command. (#352)
- [core] **feature: enhance the `search function` in `all paged GUI`, now we will search any text you can see. (Much better to search a `keyword` inside Item)**
- [fuji] **feature: adds `about`, `user guide`, `reload` and `debug` buttons for `core` module in `/fuji gui` command.**
- [fuji] feature: make the `/fuji` command a shortcut to `/fuji inspect modules`.
- [fuji] feature: new `/fuji gui` command, as a shortcut to `/fuji inspect modules`.
- [fuji] feature: display the `relative path` for `/fuji inspect configurations`. (It's shorter, if not harm the readability)
- [core] refactor: a better implementation to invoke module initializers.