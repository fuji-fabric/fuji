> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/
 
## Changelog

- Enhanced compatibility in `neoforge` platform with `connector` mod.
  - fix: now the `/fuji reload` command works in `neoforge singple-player world`, when `command_permission` module enabled.
  
- [command_rewrite] improvements
  - **docs:** Add document to describe how this module works, and the details of the command lifecycle. 
  - **feature:** Added support for chained rewrite rules.
    - **Before:** Rewrite rules were applied in a top-down order, with only the first matching rule taking effect.
    - **Now:** Rewrite rules are applied in a top-down order, and multiple matching rules can take effect sequentially.  
- [deathlog] fix: the `next page button` in `/deathlog` GUI didn't work.