> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases


## Changelog

- **[maintenance]**
  - **Feature:** **Introduced a `maintenance` module to provide a maintenance mode.**
    - Added the `/maintenance {on/off}` command.
    - Added the `/maintenance kick-all` command.
    - Introduced the `fuji.maintenance.bypass` permission, allowing users to join the server during maintenance.
    - Displayed random maintenance messages on the MOTD screen.
    - Added custom events to execute commands when maintenance mode is enabled or disabled.
     
---

- **[home]**
  - **Feature:** **Added `/home gui` and `/home gui <player>` commands to open the homes GUI.**
    - The user can hover to see the details of a home.
    - The user can left-click to teleport to a home.
    - The user can right-click to delete a home.

---

- **[core]**
  - **New Features:**
    - **Introduced a `hotkey F` for the search button in all paged GUIs.**

---

- **[command_toolbox.warp]**
  - **Feature:** Introduced the `/warp set-position` command to conveniently modify the position of a warp.

---

- **[command_advice]**
  - **Feature:** Added a `document` property to enhance configuration clarity.

---

- **[command_meta.IF]**
  - **Documentation:** Added a new guide describing how to integrate `/IF` with `/tag` and `/scoreboard`.

