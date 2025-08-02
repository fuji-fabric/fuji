> Read detailed change logs in https://github.com/sakurawald/fuji/commits/dev/

- [command_event] fix: the `on_player_first_join_event` will be trigger again for a player, if the server is shutdown by `/stop` before the `LEAVE_GAME` stat gets `updated` and `saved` (#432)