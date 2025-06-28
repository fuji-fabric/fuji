> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/


- [core] refactor: rename the root package name of the project, to provide a better debug information in exception trace list. 
- [fuji] feature: add more the `colorboxes` for `all modules`. (Provides more document strings.)
- [core] feature: provide better warning messages when failed to initialize a module on server startup.
- [core] feature: use a better implementation to invoke module initializers, and skip the initialization of variables if the module is disabled.
- [fuji] fix: should escape the `placeholders` and `style tags` when displaying `document string`.
 