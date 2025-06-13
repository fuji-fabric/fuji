> The version number of fuji follows `semver` now: https://semver.org/

- [command_bundle] feature: now will logging when register/unregister a bundle command in console. (Making it easier for debugging.)
- [command_bundle] fix: the return value of bundle commands is always 1. (It should be 0 if execute successfully.)
