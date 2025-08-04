> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [jail] feature: reduce the `console-spam` for scheduled jobs.
- [core] refactor: improvements to the `language` system.
  - feature: now will `validate` the `language files` when loading them.
  - feature: add `argument arity validator`, to check if the `argument number` for each `language value` is matched, and try to fix it if mismatched.
  - refactor: use a clearer syntax for `named arguments` in `language value`. (Changed from `[[argument-name]]` to `${argument-name}`)
- [core] feature: enhance the `data migrator`, to provide better cross-version data schema migration.
- [core] feature: auto-save feature for data files has been updated to save `every 10 seconds` instead of `every minute`.
