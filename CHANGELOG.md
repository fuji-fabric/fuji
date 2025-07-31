> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [core] feature: new `core.formatter.date_formatter` option allows users to customize the `date format`.
- [warning] feature: add `confirm` optional argument to `/warning clear` command.
- [core] build: introduce the `error prone` checking framework in building flow, to check the possible errors at compile-time automatically.
- [core] fix: apply the `text parser input fixer` for MC version in the range `[1.20, 1.20.4)`, addressing an issue where `custom color enclosing tags` like `</#FF0000>` were not functioning correctly.
