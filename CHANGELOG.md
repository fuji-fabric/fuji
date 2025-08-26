> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases
 


## Changelog

- **[command_advice]** features and fixes:
  - Improved the interaction between `canceller advices` (`CANCEL_AS_SUCCESS` and `CANCEL_AS_FAILURE`) and `non-canceller advices`. A `non-canceller advice` will no longer be executed if the target command has already been cancelled by a `canceller advice`.
  - Added new advice types: `CANCEL_IF_ANY_SUCCESS`, `CANCEL_IF_ALL_SUCCESS`, `ON_EXECUTION_CANCELLED`, `ON_EXECUTION_SUCCESS`, and `ON_EXECUTION_FAILURE`.

  ```markdown
  ◉ Semantics of each `advice type`:
  - `BEFORE_EXECUTION`: Executes specified commands *before* the target command, if not cancelled by other advices.
  - `AFTER_EXECUTION`: Executes specified commands *after* the target command, regardless of `SUCCESS` or `FAILURE`.
  - `ON_EXECUTION_SUCCESS`: Executes specified commands *if* the target command returns `SUCCESS` (return value > 0).
  - `ON_EXECUTION_FAILURE`: Executes specified commands *if* the target command returns `FAILURE` (return value = 0).
  - `ON_EXECUTION_CANCELLED`: Executes specified commands *if* the target command is `CANCELLED` by other advices.
  - `CANCEL_AS_SUCCESS`: Cancels the target command and treats it as `SUCCESS` (return value = 1).
  - `CANCEL_AS_FAILURE`: Cancels the target command and treats it as `FAILURE` (return value = 0).
  - `CANCEL_IF_ANY_SUCCESS`: Cancels the target command and treats it as `FAILURE` if *any* specified command returns `SUCCESS` (return value = 0).
  - `CANCEL_IF_ALL_SUCCESS`: Cancels the target command and treats it as `FAILURE` if *all* specified commands return `SUCCESS` (return value = 0).

---

- **[command_alias]** fix: Added support for redirecting one `literal command` to another, such as redirecting `/wb` to `/workbench`.
  - **Before:** The redirect target command was required to have child command nodes.
  - **Now:** The redirect target command can also be a leaf command node.

---

- **[tab]** feature: Introduced new options `enable_header` and `enable_footer`.

---

- **[command_toolbox.reply]** feature: Added new command `/reply set-target <player>`.

---

- **[command_toolbox.glow]** feature: Added new command `/glow <entities>` to select a collection of entities as targets.
  - **Example:** `/glow @e[type=pig,distance=..16]`

---

- **[core]** feature: Introduced a new `argument type adapter` for the `entities` argument type.

---

- **[command_meta.NOT]** feature: Added new `/NOT` command to invert the return value of `SUCCESS` and `FAILURE`.
  - **Example:**
    ```mcfunction
    /IF NOT has-item? <player> minecraft:apple 16 THEN say You don't have 16 apples. ELSE say You have 16 apples.
    ```

