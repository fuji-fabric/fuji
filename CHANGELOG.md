> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

# Changelog

- **[core] Feature:** Added support for a fallback instance creator when the default creator fails.
  - **Before:** If the default Gson instance failed to create an object for a given class, the process resulted in a hard failure.
  - **Now:** If the default Gson instance fails to create an object for a given class, the system automatically falls back to a secondary Gson instance and retries the creation.
