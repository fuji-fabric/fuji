> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
 
## Changelog

- [language] feature: now the `multi-language` feature supports `MC 1.20` and `MC 1.20.1`.
- [nametag] Enhance this module
  - docs: document how this module works.
  - feature: improve the responsiveness of the nametag hiding when press `SHIFT` key.
  - feature: use a better implementation.
    - Before: The nametag entity has to ride the owner, to sync the position update.
    - **Now: Use the virtual riding, and simulate the riding at client-side, making it better compatibility with vanilla behaviours and other mods.**
  - feature: new `/nametag toggle` command, to toggle between `fuji nametag` and `vanilla nametag`.
    - Example: `/nametag toggle @s`
    - Example: `/nametag toggle others @a false`
  - refactor: command `/test-afk` is changed to `/is-afk?`
