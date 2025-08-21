> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

# Changelog

- **[core] Feature:** Improved data de-serialization.
  - All data types now automatically generate a default configuration if the user accidentally deletes it.

- **[core] Feature:** Enhanced *item string parser*.
  - Items with custom NBT data can now be specified as icons in GUIs.
  - The syntax is consistent with the `/give <item>` command, allowing custom player heads and other data-driven items to be used in multiple contexts, such as:
    - **Warp icons**
    - **Head economy items (You can now specify an item with NBT as the item currency)**
    - **Title items**
    - **Works items**
    - **Command menu slot items**

  **Examples:**
  - Set a custom player head as a warp icon:
    ```mcfunction
    /warp set-item city minecraft:player_head[minecraft:profile=Steve]
    ```  
  - Use a custom player skull as a toast icon:
    ```mcfunction
    /send-toast @s --icon minecraft:player_head[minecraft:profile=Steve] <rb>Hello World
    ```  
  - Use a custom player skull as a dialog button icon:
    ```mcfunction
    /send-dialog @s --yesButtonItem minecraft:player_head[minecraft:profile=Steve] <rb>Hello World
    ```  
  - Construct item stacks with any data component:
    ```mcfunction
    /warp set-item shop minecraft:diamond_sword[minecraft:damage=1024]
    /warp set-item shop bundle[bundle_contents=[{id:"diamond",count:2}]]
    ```  

- **[color.anvil] Fix:** Ensured the module works in single-player worlds when installed client-side.  
- [core] feature: Improve the compatibility when running on `neoforge` platform (with `connector` mod)
  - [command_permission] fix: make it working in `neoforge single-player world`.
