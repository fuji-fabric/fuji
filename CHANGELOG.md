> The version number of fuji follows `semver` now: https://semver.org/

- [core] feature: escape tags when reporting an exception in CommandExecutor, so that we can display the raw command properly. (e.g. `/run as console aa <blue> bb`)
- [fuji] feature: now will display the `From Module` field for all fuji commands. (Issue `/fuji inspect fuji-commands` to see it.)
- [fuji] feature: now will display the `From Module` field for all fuji argument types. (Issue `/fuji inspect argument-types` to see it.)
 