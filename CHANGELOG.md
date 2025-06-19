> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [core] fix: remove the `command manager: ...` console spam.
- [core] feature: now log the `Write default configuration ...` logging into the `debug` logger level, reduce the console spam.
- [fuji] feature: add lang keys for `/fuji debug` command.
- [color.sign] fix: should only parse `color style tags`, and ignore `placeholders`. (Ignore the placeholders, making it performs bette with `command_interactive` module.)
- [color.sign] fix: should not throw NPE when editing signs if the sign lines is empty.
- (<= MC 1.20.4) [command_interactive] fix: should respect the `command_warmup` module.
