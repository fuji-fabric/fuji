> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases

# Changelog

---

## [core]
- **Feature:** Improvements to `permission.json`
  - Automatically cleans invalid keys for better clarity.
  - Sorts `command path` entries alphabetically for easier readability.

- **Feature:** Enhanced game profile fetching
  - Significantly improves responsiveness of player skulls in:
    - `deathlog GUI`: `/deathlog`
    - `balance-top GUI`: `/economy balance-top gui <currency>`
    - `warnings GUI`: `/warning`
    - `about GUI`: `/fuji about`
  - `/fuji inspect languages` GUI now displays the player’s actual skin on the player skull.

---

## [tpa]
- **Feature:** Introduced new `/tpa gui` command.
- **Feature:** Added `/tpa` as an alias to `/tpa gui`.
 
---

## [command_menu]
- **Feature:** Added `other_indexes` option for `slot descriptor`.
  - Allows specifying multiple indexes for a slot (beyond the primary `index`).
  - Makes it easier to mirror slots and efficiently fill GUI space.

---

- **Feature:** Added `fill_blank_indexes` option for `slot descriptor`.
  - Enables designating a slot as an empty-slot filler.
## [command_cooldown]
- **Feature:** `/command-cooldown list` now provides detailed information, including `unnamed cooldowns`.

---

## [deathlog]
- **Feature:** `/deathlog` GUI now displays the **player skull** instead of the default skeleton skull.
