> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [command_meta.IF] feature: improvements to this module.
  - feature: now will not report the `Test failed` exception to the `command source` when using `/IF` command, to prevent the `feedback spam`.
- [economy] feature: improvements to this module.
  - feature: new `%fuji:balance <currency-id>` placeholder, to display the `balance` of a specified `currency` for the player.
  - feature: improve the `error feedback` when failed to get specified `economy account`. 
  - feature: introduce the `--confirm` optional argument for `/economy clear` command.
  - docs: add tips and examples for this module.
