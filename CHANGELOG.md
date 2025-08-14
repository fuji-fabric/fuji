> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

## Changelog

- [core] feature: add `--silent` and `--stdout` optional arguments for all fuji commands. (#266)
  - feature: add `--silent` optional argument, to `cancel the text sending` during this `command execution`.
  - feature: add `--stdout` optional argument, to `log the text sending into the console` during this `command execution`.
  - examples:
    - `/fly --silent true`
    - `/fly --silent true --stdout true`
    - `/run as console --silent true fly others @r --silent false`

