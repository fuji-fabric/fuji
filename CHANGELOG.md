> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases


## Changelog

### [rtp] 
Now support `async chunk loading & generation`, in a `lag-free` style.

- **Added support for fully asynchronous chunk loading and generation, ensuring a lag-free experience.**
  - Example: execute a batch RTP without performance issues:
    ```mcfunction
    /execute as @a run rtp
    ```
- **Introduced `async_chunk_loading_timeout_ticks`, allowing configuration of the maximum wait time (in ticks) before aborting an asynchronous loading attempt.**
- **Support for `biome whitelist mode`, the player can `rtp` to specified `biomes`.**
- The RTP process will now automatically cancel if the associated player is removed (e.g., if the player disconnects during RTP).

### [tppos]
- feature: add optional argument `--asyncChunkLoadingTimeoutTicks`
- feature: add optional argument `--chunkInhabitedTimeLowerThanTicks`
- feature: add optional argument `--biome`
  - Example: `/tppos --biome minecraft:taiga`


### [command_menu] 
Enhancement for `nested menus`
- Improved `/command-menu open`: the target menu now opens **1 tick later**, simplifying the handling of nested menu opening and closing.

### [command_toolbox.itemname]
- Added `/itemname {set|reset}` command to set or reset the custom name of the item currently held in hand.

