> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
> 
> For user manual, refer to: https://fuji-fabric.github.io
>

# 📑 Changelog

This release brings major improvements to the **world modules** and overall user experience.  
Special thanks to **@zonary123** for contributing!

---

## ✨ New Features
- **[world.gamerule]** — Optimized performance when retrieving *per-dimension gamerules*.  
  *(Thanks to @zonary123)*
- **[world]** — Prevent the `/world delete` command from deleting *vanilla dimensions*.
- **[world]** — Added support for specifying a **custom namespace** when *creating* or *importing* dimensions. (#517)
- **[world]** — Refined feedback messages for `/world {load|unload|reset}` commands to improve clarity.

---

## 🐛 Fixes
- **[world]** — Fixed `/world who` command suggestions not appearing properly.
