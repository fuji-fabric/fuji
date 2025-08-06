> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

### [rank] — New **Rank** System
- Introduced a `rank` module to define **ranks** with customizable **requirements** and **awards**.
- For each rank, you can set:
  - **Display name**, **description**, **requirements**, and **events**.
  - Requirements are shown as a **checkbox list**, indicating which are met.
  - Requirements can be based on **any command** (e.g., statistics, placeholders, scoreboard, or `/execute if` conditions).
- Integrate with your existing `luckperms groups` using `rank events`.
- Multiple **rank paths** can lead to the same rank.
- Different user groups can have different **starting rank nodes**.
- New commands:
  - `/rank progress` — View available next ranks and click to preview them.
  - `/rank up <rank>` — Promote to a chosen next rank.
  - `/rank down <rank>` — Demote to a previously earned rank.
  - `/rank info` — View detailed info for a rank.
  - `/rank list <rank type>` — List ranks by type for a player.
  - `/rank set` — Assign a specific rank to a player.

### [command_toolbox.speed] — Player Speed Control
- New `speed` module with `/speed {walk|fly}` command.
- Allows easy modification of **walk** and **fly** speed.

### [command_attachment] — Single-player Support
- Fixed: Works in **single-player worlds** when installed on the **client-side**.

### [predicate] — Easy Condition Testing
- New predicate commands:
  - `/has-item? <player> <item-predicate> <count>` — Check if a player has a certain item and quantity.
  - `=?`, `!=?`, `>?`, `>=?`, `<?`, `<=?` — Compare a placeholder value to a number.  
    *(Example: `/<=? Steve 10 %player:health%`)*