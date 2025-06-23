> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [fuji] feature: new implementation for `/fuji inspect configuration` command.
  - feature: highlight the `path` and other useful text, to read them easier.
  - feature: represent different `data types` using different items. (Better support for `primitive types`)
  - feature: now you can go inside `List` type and `Map` type.
- [fuji] feature: add the `From Module` information for `/fuji inspect configuration` command.
- [fuji] fix: the `/fuji inspect configuration` command should not parse the `object value`, just keep the `literal string`.
- [fuji] fix: the `/fuji inspect configuration` command should escape the tags like `$` character.
- [core] fix: the `style tags only text parser` should support the `markdown` language.
- [head] feature: now will send the `data fetching` message when the `head category` data is empty. (For the first time setup)
