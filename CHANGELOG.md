> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases


## Changelog

- [command_toolbox.{burn/freeze}]
  - **Feature:** `/burn` and `/freeze` commands now support selecting a collection of entities as the target.

---

- [home] Improvements
  - **Feature:** Added `/home rename <old-name> <new-name>` command.
  - **Feature:** Re-open the Home GUI after `right-click` for improved convenience.

---

- [kit] Improvements
  - **Feature:** Added `/kit preview <player> <kit>` command to preview the contents of a kit in a GUI.

---

- [command_toolbox.more]
  - **Feature:** Added new optional argument `--oversize` for the `/more` command.
    - **Example:** `/more --oversize true` sets the item count to 64, even for items such as `diamond sword` or `ender pearl`.

---

- [command_meta]
  - **Feature:** Introduced new modules providing `/AND` and `/OR` commands.
    - **Example:**
      - `/AND has-item? Steve minecraft:iron_ingot 8 AND has-item? Steve minecraft:gold_ingot 4`
      - `/OR has-item? Steve minecraft:iron_ingot 8 OR has-item? Steve minecraft:gold_ingot 4`
