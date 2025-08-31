> For detailed change logs, please visit: https://github.com/sakurawald/fuji/commits/dev/
> 
> For historical change logs, refer to: https://github.com/sakurawald/fuji/releases

# Changelog

- [core] Enhanced **Exception Handling**
  - **Feature:** Enhanced *command execution exception handling*.
    - Players now see `localized error messages` for command syntax issues (e.g., *no player found*).
    - Administrator players can now **click the error text** to easily **copy the stack trace**.
  - **Feature:** Improved exception messages for the `/run as {console|player|fake-op}` command.
    - Now will `stream the command execution feedback` and provide the `command syntax error` for `initialing command source`.
      - Example: `/run as console run as fake-player Steve bad command`
    - Make it easier to understand the command context.
  - **Feature:** Simplified exception messages and added clear instructions, making them more user-friendly.
  - **Feature:** Improved handling of *module initialization failures*.
    - Users now receive a **clear, user-friendly error message** along with suggested solutions.
    - Reduced console noise and spam.
  - **Feature:** Refined *mixin injection failure messages*.
    - Reduced console noise and spam.
  - **Feature:** Improved exception messages for the `/fuji reload` command.
    - Users now receive **detailed diagnostic messages**, helping them identify the source of the issue.
