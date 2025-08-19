> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

## Changelog

This release is the re-publish of `v12.22.0`

This release introduces a new **core feature** called **Command Assistant**, along with several bug fixes and improvements.

### Features
- **[core] Command Assistant**  
  Provides **real-time command hints** while typing commands.
  - Added new `core.command` options in `config.json` to configure the requirements for using Command Assistant, or to disable it entirely.

### Fixes
- **[core] Argument Suggestions**  
 Fixed an issue where a *required argument* with a custom suggestions provider was still listed in command suggestions, even when the command source could not use that command.
  - *Note: This was originally a minor bug from Mojang, causing inconsistency between the `/help` command and tab completions. We resolved it to prevent user confusion.*
- **[core] ANSI Color Reset**  
  Ensured that all ANSI color styles are properly reset in every terminal after printing the user guide in the console.
