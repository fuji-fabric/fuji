[Test Case]
- Module: afk
- Steps: **Issue `/afk` and see the player list.**
- Purpose: The display name of an afk player should be modified.

[Test Case]
- Module: afk
- Steps: **Try to move a player in afk state.**
- Purpose: The `moveable` option should work.

[Test Case]
- Module: chat
- Steps: **Enable the `online-mode` in server.properties**
- Purpose: All of chat-related modules should not break the Mojang's chat signature.

[Test Case]
- Module: chat
- Steps: **Test the chat-related modules with `Styled Chat` mod.**
- Purpose: It should work fine with other mods.

[Test Case]
- Module: color.sign
- Steps: **Place a `sign block` and write style tags in it, then re-open the sign.**
- Purpose: The style tags in the sign block should be `parsed` and `reversed`.

[Test Case]
- Module: command_advice
- Steps: **Issue `/say hi` command.**
- Purpose: The command should be cancelled with the `/send-broadcast` command.

[Test Case]
- Module: command_bundle
- Steps: **Issue `/reload`, `/fuji reload`, `/fuji inspect fuji-commands` and `/command-bundle list`**
- Purpose: The bundle commands should be able to register and un-register on the fly.

[Test Case]
- Module: command_interactive
- Steps: **Enable `command_warmup` module, issue `/back` command.**
- Purpose: It should work with un-signed argument type.

[Test Case]
- Module: command_interactive
- Steps: **Enable `command_warmup` module, issue `/say hi` command.**
- Purpose: It should work with signed argument type.

[Test Case]
- Module: command_interactive
- Steps: **Test the `command_interactive` module in `online-mode` server.**
- Purpose: The packet should not break the client-side signature validation.

[Test Case]
- Module: command_meta.IF
- Steps: **Issue `/IF execute if block ~ ~-1 ~ minecraft:diamond_block THEN say You are standing on diamond block. ELSE say You are not standing on diamond block.` command.**
- Purpose: You should not see the red `Test failed` in the feedback.

[Test Case]
- Module: command_rewrite
- Steps: **Issue `/home` command.**
- Purpose: It should be rewrite to `/home tp default` command.

[Test Case]
- Module: command_scheduler
- Steps: **Issue `/fuji reload` command.**
- Purpose: The jobs from command_scheduler module should be re-scheduled.

[Test Case]
- Module: command_toolbox.tppos
- Steps: **Issue the command `/tppos --z 64 --x 32 --y 128`**
- Purpose: The command context should be passed after the command redirection.

[Test Case]
- Module: command_toolbox.tppos
- Steps: **Teleport to an offline player's location using `/tppos offline`**
- Purpose: We should be able to make the offline player instance.
- Purpose: The saved dimension of the offline player should not be reset to minecraft:overworld

[Test Case]
- Module: core
- Steps: **Consider special states of a player.**
- Purpose: A player may be a fake-player from `carpet` mod.
- Purpose: Once a player die, the old ServerPlayerEntity is invalid.
- Purpose: A player may `disconnect` from the server.
- Purpose: A player may in `spectator` game-mode.
- Purpose: If a player is during the transferring of end portal, he is in no dimensions.

[Test Case]
- Module: core
- Steps: **Consider the possible runtime environments.**
- Purpose: The fabric server-side environment.
- Purpose: The fabric client-side environment.
- Purpose: The neo-forge server-side environment. (With `sinytra-connector` mod)
- Purpose: The neo-forge client-side environment. (With `sinytra-connector` mod)
- Purpose: The hybrid server (forge+bukkit) with `sinytra-connector` mod
- Purpose: The GraalVM native image. (Which invalidates the reflection)

[Test Case]
- Module: core
- Steps: **Create an inventory display that contains a shulker box.**
- Purpose: See if we can go inside the shulker box.

[Test Case]
- Module: core
- Steps: **Issue `/fuji`, and see the `document of afk module`, the `details of run module` and the `details of skin` module.**
- Purpose: The text parser should parse the text properly from the earliest version to the latest version.
- Purpose: The URL highlighter should work properly.
- Purpose: Ensure the `</>` doesn't break the style of texts.

[Test Case]
- Module: core
- Steps: **Issue `/stop` in the production environment.**
- Purpose: The program should be terminated.

[Test Case]
- Module: core
- Steps: **Issue `/when-online ...` and `/json put ...` commands.**
- Purpose: The command suggestion optimizer should work fine.

[Test Case]
- Module: core
- Steps: **List the command tree of a normal user.**
- Purpose: The command permissions should be handled properly.

[Test Case]
- Module: disabler.move_wrongly_disabler
- Steps: **Sit in a `boat` and try to move it.**
- Purpose: The `vehicle moved wrongly` should be disabled.

[Test Case]
- Module: echo.send_title
- Steps: **Issue the command `/send-title @s --mainTitle "<rainbow>Hello" --subTitle "<blue>World" --fadeInTicks 60 --stayTicks 60 --fadeOutTicks 60`**
- Purpose: Consecutive optional argument should work.

[Test Case]
- Module: fuji
- Steps: **Inspect the configurations of `command_menu` module.**
- Purpose: It should be able to inspect complex data structures.

[Test Case]
- Module: functional.enchantment
- Steps: **See if a pickaxe gets the max power level in `/enchantment`**
- Purpose: See if the lambda of enchantment context is modified.

[Test Case]
- Module: head
- Steps: **Buy a new head in `/head`.**
- Purpose: See if the structure of skin is changed by Mojang.

[Test Case]
- Module: kit
- Steps: **Create a new kit using `/kit editor` command.**
- Purpose: See if the `kit editor` works.

[Test Case]
- Module: kit
- Steps: **Give the new kit using `/kit give` command.**
- Purpose: See if the items is inserted in the proper slots. (Note that the player in creative mode can always pick up the same items even their inventory is full.)

[Test Case]
- Module: multiplier
- Steps: **Summon a fake player using `/player 1 spawn` and throw exp bottle to it.**
- Purpose: Test the compatibility between `luckperms` and `carpet`'s fake player.

[Test Case]
- Module: nametag
- Steps: **Pass through a nether portal.**
- Purpose: The nametag entity should be discarded in the old dimension.
- Purpose: A new nametag entity should be created in the new dimension.

[Test Case]
- Module: sit
- Steps: **Issue `/sit` command while stepping on the `bed block`.**
- Purpose: The raycast height should be proper.

[Test Case]
- Module: view
- Steps: **Issue the `/view {inv/ender}` command on a fake-player.**
- Purpose: You should be able to modify the slots on the fly.

[Test Case]
- Module: works
- Steps: **Create a new production work and start the sample.**
- Purpose: See if the chunk iterator works.
- Purpose: See if the hopper mixin works.

[Test Case]
- Module: world
- Steps: **In MC 1.20.1, create a `overworld` dimension type with seed `12345`.**
- Purpose: Goto `/tp @s 14665 ~ 345`. (You should get `emerald * 7`, `gold ingot * 3`, `iron ingot * 11`, `tnt * 2`, `heart of the sea * 1`, `cooked cod * 8` and `potion of water breathing * 1`.)
- Purpose: Goto `/tp @s 0 128 0`, you should in `minecraft:ocean`, and there is a `minecraft:dark_forest` in front of you, also there is a `lava source` flowing down.

[Test Case]
- Module: world
- Steps: **Issue the `/save-all` command without the installation of `fabric-api` mod.**
- Purpose: The runtime dimensions should be saved.

[Test Case]
- Module: world.border
- Steps: **Issue `/tp` and `/world tp` between dimensions.**
- Purpose: The per-dimension border should be synced on the client-side.

