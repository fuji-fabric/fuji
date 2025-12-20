> For detailed change logs, refer to: https://github.com/fuji-fabric/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/fuji-fabric/fuji/releases
> 
> **For user manual, refer to: https://fuji-fabric.github.io**
>



# 📑 Changelog

- **[core] Fix:** Players were sharing a single empty `PropertyMap` instance since MC **1.21.9**.
    - **Affected:** `skin`, `head`, and any feature that uses player heads to render player skins.
