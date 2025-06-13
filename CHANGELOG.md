> The version number of fuji follows `semver` now: https://semver.org/

- (command_event module) feature: add `enable` option for each command event. (Thanks to @imfrea)
- (echo.send_toast module) fix: didn't work since v7.0.0 version.
- (core) feature: use the consistent format for mod logger. (Now we will attach the `source module` when logging a message, making it easier to find the source of a problem.)
- (core) feature: improve the performance of computing a module path string from class name.
 
