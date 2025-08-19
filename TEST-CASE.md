[Test Case]
- Module: afk
- Action: **Issue `/afk` and see the player list.**
- Target: The display name of an afk player should be modified.

[Test Case]
- Module: afk
- Action: **Try to move a player in afk state.**
- Target: The `moveable` option should work.

[Test Case]
- Module: chat
- Action: **Enable the `online-mode` in server.properties**
- Target: All of chat-related modules should not break the Mojang's chat signature.

[Test Case]
- Module: chat
- Action: **Test the chat-related modules with `Styled Chat` mod.**
- Target: It should work fine with other mods.

[Test Case]
- Module: chat.replace
- Action: **Test the `chat replace` and `chat trigger` module.**
- Target: Case: `inv`
- Target: Case: `prefix inv`
- Target: Case: `prefix inv `
- Target: Case: `prefix inv item ender suffix inv suffix`
- Target: Case: `prefix prefix item`

[Test Case]
- Module: color.sign
- Action: **Place a `sign block` and write style tags in it, then re-open the sign.**
- Target: The style tags in the sign block should be `parsed` and `reversed`.

[Test Case]
- Module: command_advice
- Action: **Issue `/say hi` command.**
- Target: The command should be cancelled with the `/send-broadcast` command.

[Test Case]
- Module: command_bundle
- Action: **Issue `/reload`, `/fuji reload`, `/fuji inspect fuji-commands` and `/command-bundle list`**
- Target: The bundle commands should be able to register and un-register on the fly.

[Test Case]
- Module: command_interactive
- Action: **Enable `command_warmup` module, issue `/back` command.**
- Target: It should work with un-signed argument type.

[Test Case]
- Module: command_interactive
- Action: **Enable `command_warmup` module, issue `/say hi` command.**
- Target: It should work with signed argument type.

[Test Case]
- Module: command_interactive
- Action: **Test the `command_interactive` module in `online-mode` server.**
- Target: The packet should not break the client-side signature validation.

[Test Case]
- Module: command_meta.IF
- Action: **Issue `/IF execute if block ~ ~-1 ~ minecraft:diamond_block THEN say You are standing on diamond block. ELSE say You are not standing on diamond block.` command.**
- Target: You should not see the red `Test failed` in the feedback.

[Test Case]
- Module: command_permission
- Action: **Issue `/reload` command, and check the client command tree.**
- Target: The `command_permission` module should warp the newly registered commands.
- Target: The client-side command tree should be updated.

[Test Case]
- Module: command_rewrite
- Action: **Issue `/home` command.**
- Target: It should be rewrite to `/home tp default` command.

[Test Case]
- Module: command_scheduler
- Action: **Issue `/fuji reload` command.**
- Target: The jobs from command_scheduler module should be re-scheduled.

[Test Case]
- Module: command_toolbox.tppos
- Action: **Issue the command `/tppos --z 64 --x 32 --y 128`**
- Target: The command context should be passed after the command redirection.

[Test Case]
- Module: command_toolbox.tppos
- Action: **Teleport to an offline player's location using `/tppos offline`**
- Target: We should be able to make the offline player instance.
- Target: The saved dimension of the offline player should not be reset to minecraft:overworld

[Test Case]
- Module: core
- Action: **Consider special states of a player.**
- Target: A player may be a fake-player from `carpet` mod.
- Target: Once a player die, the old ServerPlayerEntity is invalid.
- Target: A player may `disconnect` from the server.
- Target: A player may in `spectator` game-mode.
- Target: If a player is during the transferring of end portal, he is in no dimensions.

[Test Case]
- Module: core
- Action: **Consider the possible runtime environments.**
- Target: The fabric server-side environment.
- Target: The fabric client-side environment.
- Target: The neo-forge server-side environment. (With `sinytra-connector` mod)
- Target: The neo-forge client-side environment. (With `sinytra-connector` mod)
- Target: The hybrid server (forge+bukkit) with `sinytra-connector` mod
- Target: The GraalVM native image. (Which invalidates the reflection)

[Test Case]
- Module: core
- Action: **Create an inventory display that contains a shulker box.**
- Target: See if we can go inside the shulker box.

[Test Case]
- Module: core
- Action: **Issue `/fuji`, and see the `document of afk module`, the `details of run module` and the `details of skin` module.**
- Target: The text parser should parse the text properly from the earliest version to the latest version.
- Target: The URL highlighter should work properly.
- Target: Ensure the `</>` doesn't break the style of texts.

[Test Case]
- Module: core
- Action: **Issue `/stop` in the production environment.**
- Target: The program should be terminated.

[Test Case]
- Module: core
- Action: **Issue `/when-online ...` and `/json put ...` commands.**
- Target: The command suggestion optimizer should work fine.

[Test Case]
- Module: core
- Action: **Issue the `/warp` and `/back` command as normal user.**
- Target: The default command permission should be registered properly.
- Target: A public command, that shares a common command path prefix with another admin command, should be accessible to normal users.

[Test Case]
- Module: core
- Action: **List the command tree of a normal user.**
- Target: The command permissions should be handled properly.

[Test Case]
- Module: core
- Action: **Test the command assistant.**
- Target: Change the `cursor` using mouse click, and see the output.
- Target: Test the assistant with command redirect
- Target: Test the assistant at the beginning of the token
- Target: Test the assistant at the end of the token
- Target: Test the assistant with the optional argument: `/back 3`
- Target: Test the assistant with the entity selector: `/send-message @r`
- Target: Test the assistant with custom parser and non-zero-offset suggestions builder: `/fly others @a[distance=..8`

[Test Case]
- Module: core
- Action: **Test the command suggestion functionality.**
- Target: Issue `/command-attachment attach-entity-one <uuid>` command, it should suggest the looking at entity UUID.
- Target: Issue `/command-attachment attach-entity-one @e[type` command, it should be able to `insert` the suggestion content in the proper position. (non-zero-offset suggestions builder)
- Target: Issue `/command-attachment attach-entity-one <uuid>` command, it should be able to `insert` the suggestion content in the proper position. (zero-offset suggestions builder)
- Target: Issue `/command-attachment attach-block-one ` command, it should filter out the duplicated suggestions. (client-side suggestions and server-side suggestions)

[Test Case]
- Module: disabler.move_wrongly_disabler
- Action: **Sit in a `boat` and try to move it.**
- Target: The `vehicle moved wrongly` should be disabled.

[Test Case]
- Module: echo.send_title
- Action: **Issue the command `/send-title @s --mainTitle "<rainbow>Hello" --subTitle "<blue>World" --fadeInTicks 60 --stayTicks 60 --fadeOutTicks 60`**
- Target: Consecutive optional argument should work.

[Test Case]
- Module: fuji
- Action: **Inspect the configurations of `command_menu` module.**
- Target: It should be able to inspect complex data structures.

[Test Case]
- Module: functional.enchantment
- Action: **See if a pickaxe gets the max power level in `/enchantment`**
- Target: See if the lambda of enchantment context is modified.

[Test Case]
- Module: head
- Action: **Buy a new head in `/head`.**
- Target: See if the structure of skin is changed by Mojang.

[Test Case]
- Module: kit
- Action: **Create a new kit using `/kit editor` command.**
- Target: See if the `kit editor` works.

[Test Case]
- Module: kit
- Action: **Give the new kit using `/kit give` command.**
- Target: See if the items is inserted in the proper slots. (Note that the player in creative mode can always pick up the same items even their inventory is full.)

[Test Case]
- Module: multiplier
- Action: **Summon a fake player using `/player 1 spawn` and throw exp bottle to it.**
- Target: Test the compatibility between `luckperms` and `carpet`'s fake player.

[Test Case]
- Module: nametag
- Action: **Pass through a nether portal.**
- Target: The nametag entity should be discarded in the old dimension.
- Target: A new nametag entity should be created in the new dimension.

[Test Case]
- Module: sit
- Action: **Issue `/sit` command while stepping on the `bed block`.**
- Target: The raycast height should be proper.

[Test Case]
- Module: view
- Action: **Issue the `/view {inv/ender}` command on a fake-player.**
- Target: You should be able to modify the slots on the fly.

[Test Case]
- Module: works
- Action: **Create a new production work and start the sample.**
- Target: See if the chunk iterator works.
- Target: See if the hopper mixin works.

[Test Case]
- Module: world
- Action: **In MC 1.20.1, create a `overworld` dimension type with seed `12345`.**
- Target: Goto `/tp @s 14665 ~ 345`. (You should get `emerald * 7`, `gold ingot * 3`, `iron ingot * 11`, `tnt * 2`, `heart of the sea * 1`, `cooked cod * 8` and `potion of water breathing * 1`.)
- Target: Goto `/tp @s 0 128 0`, you should in `minecraft:ocean`, and there is a `minecraft:dark_forest` in front of you, also there is a `lava source` flowing down.

[Test Case]
- Module: world
- Action: **Issue the `/save-all` command without the installation of `fabric-api` mod.**
- Target: The runtime dimensions should be saved.

[Test Case]
- Module: world.border
- Action: **Issue `/tp` and `/world tp` between dimensions.**
- Target: The per-dimension border should be synced on the client-side.

