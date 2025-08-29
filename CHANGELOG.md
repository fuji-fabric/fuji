> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases

# Changelog

---

## [warp & world]
- **Feature:** Increased the `auto save interval` from **10 seconds** to **1 minute**.  
  *(This makes it easier to manually modify the config file on disk and reload it.)*

---

## [launcher]
- **Feature:** Introduced the new `/launch` command.

### Usage Examples
- ◉ Use a lower `angle` for **fast horizontal movement**
  1. `/launch facing @s 15 1`
  2. `/launch facing @s 15 3.9`  
     ✅ *The `power` is clamped in `[-3.9, +3.9]`*

- ◉ Use a median `angle` for **balanced horizontal and vertical movement**
  1. `/launch facing @s 30 3.9`
  2. `/launch facing @s 45 3.9`

- ◉ Use a higher `angle` for a **rocket launcher effect**
  - `/launch facing @s 60 3.9`  
    💡 *Tip: Remember to bring your `elytra`.*

- ◉ Use a `vertical angle` for a **trampoline effect**
  - `/launch facing @s 90 1`

- ◉ Use a positive `power` for a **push effect**
  - `/launch facing @s 0 1`

- ◉ Use a negative `power` for a **pull effect**
  - `/launch facing @s 0 -1`

- ◉ Use another entity's perspective as the direction to **kick the target entity**
  - `/launch at @s @e[type=!minecraft:player,distance=..8] 30 1`

- ◉ Create a **jump pad** that launches players when stepped on  
  *(Integration with the `command_attachment` module is supported.)*
  - `/command-attachment attach-block-one ~ ~ ~ --interactType STEP_ON <command>`

---

## [command_attachment]
- **Feature:** Added `/command-attachment editor` command with an **editor GUI** for improved usability.
  - Edit the `looking at entity`, `looking at block`, or the `item in hand`.
  - Use **left click** and **right click** to reorder attached commands.
  - Use **Shift + Right Click** to remove an attached command quickly.

- **Feature:** Improved the **readability** of `/command-attach query-{block|entity|item}` commands.

- **Feature:** Added support for the `SWAP_HAND` interaction type in `/command-attachment attach-item-one`.

- **Feature:** Prevent blocks with existing command attachments from being broken.

- **Feature:** Added `--confirm` optional argument for dangerous commands:
  - `/command-attachment detach-{item|block|entity}-all`

- **Docs:** Added documentation for this module.  
