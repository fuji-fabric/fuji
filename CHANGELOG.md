> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/



- [command_permission] feature: now allow to define `ruels` to handle `permission test result` for `special cases`. (This provides a convenient way to exclude `others` command from the `root command`, if the `permission implicitly inheritance` feature from `luckperms` is enabled.) (#351)
- [chat.replace] feature: improve the `performance` of this module, now only compute the `replacement text` when needed.
- [core] refactor: rename the root package name of the project, to provide a better debug information in exception trace list. 
- [core] feature: use a better implementation to invoke module initializers, and skip the initialization of variables if the module is disabled.
- [core] feature: provide better warning messages when failed to initialize a module on server startup.
- [fuji] feature: add more the `colorboxes` for `all modules`. (Provides more document strings.)
- [fuji] feature: now will display the `class document string` in `/fuji inspect configurations` command.
- [fuji] fix: should escape the `placeholders` and `style tags` when displaying `document string`.
 