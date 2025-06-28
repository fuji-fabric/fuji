> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/


- [command_permission] feature: now allow to define `ruels` to handle `permission test result` for `special cases`. (This provides a convenient way to exclude `others` command from the `root command`, if the `permission implicitly inheritance` feature from `luckperms` is enabled.) (#351)
- [core] refactor: rename the root package name of the project, to provide a better debug information in exception trace list. 
- [fuji] feature: add more the `colorboxes` for `all modules`. (Provides more document strings.)
- [core] feature: provide better warning messages when failed to initialize a module on server startup.
- [core] feature: use a better implementation to invoke module initializers, and skip the initialization of variables if the module is disabled.
- [fuji] fix: should escape the `placeholders` and `style tags` when displaying `document string`.
 