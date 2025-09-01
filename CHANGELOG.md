> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases

# Changelog

- **[core] Enhanced GUI User Experience**
  - **Feature:** Messages are now streamed to `toast` notifications if the player has an opened `paged GUI`.  
    *(Improves feedback message visibility.)*
  - **Feature:** Optimized the `close handling` of certain GUIs.  
    *(Some GUIs will now automatically re-open their parent GUI for convenience.)*

---

- **[command_state] Introduced the `command_state` Module**
  - **Functionality:**
    - Define a `state` using `predicate commands`.
    - Configure commands to be executed when a player *enters* or *leaves* the `state`.
    - Check whether a player is currently in a given `state`.
    - List all `states` of a player using the `/command-state info <player>` command.
    - Utilize `placeholders` associated with a `state`.
  - **Use Case Examples:**
    - Compose multiple predicate commands into a single `state`.
    - Integrate with **LuckPerms temporary permissions** to replicate effects such as `temp fly` and `temp god`.

---

- **[command_toolbox] New Features**
  - Added `/god <flag>` command.
  - Added `/fly <flag>` command.

