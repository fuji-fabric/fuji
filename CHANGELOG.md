> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>

# 📑 Changelog

## 🐛 Fixes
- **[core]**
  - Fixed an issue where scheduled jobs were not being re-scheduled after re-joining a `single-player world` on `the client side`. (This issue only occurred when the mod was installed and used on the `client side`. Since for `server-side`, the `server instance` will only be started exactly once.)
