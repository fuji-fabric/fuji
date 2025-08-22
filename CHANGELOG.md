> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

# Changelog


- **[core] Feature:** Optimized the `performance` of `configuration handlers` by eliminating unnecessary I/O operations.
- **[core] Feature:** Introduced a new `validator` to detect `trailing commas` and `null` elements in JSON arrays, simplifying configuration management.  
- **[core] Fix:** Possible concurrent modification exception for config handlers with auto save feature.
- **[skin] Fix:** Failed to initialize `skin` module, due to `Unable to create instance of class com.mojang.authlib.properties.Property`.