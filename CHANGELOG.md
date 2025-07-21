> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/
  
- [world] fix: now fuji will fire the `onWorldLoadEvent` and `onWorldUnloadEvent` if you installed the `fabric-api` mod. (#414, #279)
  - This improves the compatibility with other mods, like `Distant Horizons`, `Grief Defender` mods.
- [top_chunks] fix: should not use async task to open the virtual GUI. (For the compatibility with Bukkit API) (#413)
- [echo.send_custom] docs: add the example custom text file to describe how to use this module. 
- docs: replace the `<player>` with `Alice` in examples.

