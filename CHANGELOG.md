> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
 
## Changelog

- [language]
  - feature: The `multi-language` feature now supports `MC 1.20` and `MC 1.20.1`.

---

- [nametag]
  - Enhance this module:
    - docs: Added documentation on how this module works.
    - feature: Improved the responsiveness of the nametag hiding when pressing the `SHIFT` key.
    - **feature: Implemented a better solution for nametag synchronization.**
      - Before: The nametag entity had to ride the owner to sync the position updates.
      - **Now: Utilizes virtual riding, simulating the ride client-side for better compatibility with vanilla behaviors and other mods.**
    - feature: Introduced a new `/nametag toggle` command to switch between `fuji nametag` and `vanilla nametag`.
      - Example: `/nametag toggle @s`
      - Example: `/nametag toggle others @a false`

---

- [afk]
  - Enhance this module:
    - **feature: Improved implementation for better `performance` and `compatibility`.**
    - refactor: Changed the `/test-afk` command to `/is-afk?`.
    - feature: Now `fake players` from the `carpet` mod are ignored and will not enter the `afk state`.
