> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
 
## Changelog

- **[command_rewrite] Improvements**  
  - **Docs:** Added documentation to describe the functionality of this module and provide details about the command lifecycle.  
  - **Feature:** Introduced support for chained rewrite rules.  
    - **Before:** Rewrite rules were applied in a top-down order, with only the first matching rule taking effect.  
    - **Now:** Rewrite rules are applied in a top-down order, and multiple matching rules can take effect sequentially.

---

- **[deathlog] Fix**  
  - **Fix:** The `Next Page` button in the `/deathlog` GUI was not functioning as expected.

---

- **[color.sign & color.anvil] Improvements**  
  - **Docs:** Updated documentation to better describe the `style tags` permission.

---

- **[command_warmup] Improvements**  
  - **Docs:** Enhanced documentation to clarify the `tag` bypass permission.

---

- **Enhanced Compatibility with `neoforge` Platform**
  - **Fix:** The `/fuji reload` command now functions correctly in `neoforge` single-player worlds when the `command_permission` module is enabled.


