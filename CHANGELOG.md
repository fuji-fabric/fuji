> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- **feature: new module `economy` module. (To enable the `economy gameplay` in fabric server)**
  - feature: integrate with the `common economy api`, to work with any other mods who support it.
  - feature: define your own `currencies` type using easy config.
  - feature: integrate with `Universal Shops` mod, to get the similar gameplay like `QuickShop` in `bukkit`.
  - feature: new command `/economy {give/take/set/clear} ...` to manage balance.
  - feature: new command `/economy {pay} ...` to transfer currency.
  - feature: new command `/economy {account/accounts}` to query accounts.
  - feature: new command `/economy {balance/balance-top}` to query balance, and open the balance top GUI.
- **feature: new module `echo.send_dialog`, to send the text using `dialog GUI`.**
- [core] feature: now will only send the `error stack trace` when `exceptions in command execution`, if the command source is admin.
- [core] feature: now will report `error details` when failed to verify command source, or when failed to make command parameters.


