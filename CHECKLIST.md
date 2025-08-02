# Checklist

It's good to have a checklist, to avoid forgetting something.

## Bump version

- Read the change log of Minecraft version.
  - https://fabricmc.net/blog/
- Update the version of fabric loader.
- Update the version of mappings.
- Update the version of dependent mods.
- Modify the `build.yml` file to include the new supported MC version.

## Publish a new version

- Pass the test cases in [TEST-CASE.md](https://github.com/sakurawald/fuji/blob/dev/TEST-CASE.md) file.
- Sync the language files from crowdin.
- Update the `semantics version` in `gradle.properties` file.
- Write the `CHANGELOG.md` file.
- Push a git commit with the prefix `[publish]`.

## Painful things
- Don't use star import. 
- Don't use static import in mixin class (Or better just don't use it in the project).
- Try not use @Redirect and @Override in mixin class.