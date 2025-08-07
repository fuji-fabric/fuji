> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

### [chat.history]
**Improvements and Fixes:**
- **Feature:** Improved debug logging and configuration fields for better clarity.
- **Fix:** The chat history module was non-functional on offline-mode servers when Minecraft version ≥ 1.21.5.
 
### [chat.trigger]
**Feature:** Slight performance improvement.

### [chat.mention]
**Fix:** Mentioned players are now displayed correctly even when their names share a common prefix.

### [chat.display]
**Feature:** Players can now modify their own inventory while viewing another player's display GUI.

### [chat.spy]
**Feature:** Enhanced debug logging for this module, making it easier to identify the message type of the target message.

### [core]
**Change:** Now attaches the `MOD_VERSION` property to each generated config file.
