# Checklist

It's good to have a checklist, to avoid forgetting something.

## Bump version

- read the change log of minecraft version
- update the version of fabric loader
- update the version of mappings
- update the version of dependent mods
- test the jar file in a real environment.

## Test a new version

- test interesting commands. (from a to z)
- test interesting mixins.
- test the integration with carpet-fabric.
- test the integration with luckperms.

## Publish a new version

- sync the language files.
- update the version in "gradle.properties". (Maybe respect the `semvar` spec.)
- update change log in "CHANGELOG.md"
- test the jar file in a real environment.
- publish the pdf file in "dev" branch.
- modify the `build.yml` file to include the new supported MC version.
- place a sign and write `/say hi` and `back` command on it.
- push a git commit with the prefix `[publish]`


## Painful things
- Don't use star import. 
- Don't use static import in mixin class (Or better just don't use it in the project).
- Try not use @Redirect and @Override in mixin class.