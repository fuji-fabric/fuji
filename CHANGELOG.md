> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

## Changelog


- [core] fix: the `required argument` with `custom suggestions provider` should not be listed in the `command suggestions` if the command source can't use that command. **(Actually, this is a minor bug from Mojang, and it breaks the consistence between `/help` command and `Tab key completions`. We fix it to prevent user confusion.)**
- [back] feature: add shortcut command `/back <dimension>` to `/back 1 <dimension` command.
