> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [world] refactor: now `/world tp` simply teleports to the `target dimension` with the same coordinate, instead of starting a rtp process. (The rtp is likely to failed, and cost too much. The `/world tp` command is an admin-level command for debug, if you really need a user-level rtp command, use `rtp` module.)
- [anti_build] fix: when using in `client-side`, the `place block` type will send the message twice.

