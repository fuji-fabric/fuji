> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases

# Changelog

- [core] enhanced `exception handling`
  - feature: simplify the `exception messages`, and providing instructions, making it more user-friendly.
  - feature: improved `error printing` functions.
  - feature: optimize the error feedback for `Command Syntax Error`.
  - feature: optimize the `command execution execption handler`
    - feature: now the `players` will get `localized exception message` for `command syntax error`, like `no player was found` message.
    - feature: now the `admin players` can `click` the `error text` to `copy the stacktrace`.
