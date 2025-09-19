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
- Target: Input `inv`
- Target: Input `prefix inv`
- Target: Input `prefix inv<space>`
- Target: Input `inv suffix`
- Target: Input `prefix inv item ender suffix inv suffix`
- Target: Input `prefix prefix item`

[Test Case]
- Module: color.sign
- Action: **Place a `sign block` and write style tags in it, then re-open the sign.**
- Target: The style tags in the sign block should be `parsed` and `reversed`.

[Test Case]
- Module: command_advice
- Action: **Test the advanced use-case of command advice.**
- Target: Issue `/repair` with `iron_ingot x 8`, `gold_ingot x 16` and `damaged diamond sword`.
- Target: Issue `/repair` with `iron_ingot x 16`, `gold_ingot x 16` and `non-damaged diamond sword`.
- Target: Issue `/repair` with `iron_ingot x 16`, `gold_ingot x 16` and `damaged diamond sword`.

[Test Case]
- Module: command_advice
- Action: **Test the basic use-case of command advice.**
- Target: Issue `/heal` command, you should see the heart particle.
- Target: Issue `/say Hello World` command, you should see the replaced version.
- Target: Issue `/msg @s Ping` command, you should see the replaced version.

[Test Case]
- Module: command_alias
- Action: **Test the redirect functionality.**
- Target: The redirect target command is a non-leaf command: `/invsee` -> `/view inv`
- Target: The redirect target command is a leaf command: `/wb` -> `/workbench`

[Test Case]
- Module: command_bundle
- Action: **Issue `/reload`, `/fuji reload`, `/fuji inspect fuji-commands` and `/command-bundle list`**
- Target: The bundle commands should be able to register and un-register on the fly.

[Test Case]
- Module: command_cooldown
- Action: **Test the compatibility with other modules.**
- Target: Issue `/heal` command twice, the `command warmup` should be performed first, then the `command cooldown`
- Target: Issue `/run as player @s heal` command twice, the command cooldown should be performed.

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
- Action: **Issue `/fuji reload` and `/reload` commands in `neoforge single player world`.**
- Target: It should not trigger the Concurrent Modification Exception.

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
- Action: **Issue `/fuji` and click `Next Page` button.**
- Target: You should see the reimu there, the footer should not be over-drawn.

[Test Case]
- Module: core
- Action: **Issue `/fuji` command, and press `F` key.**
- Target: Check the semantics of `SlotGuiInterface#click`, ensure it didn't changed in new version.

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
- Action: **Issue `/when-online ...` and `/json put 1 2 3 ...` commands.**
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
- Action: **Modify the `my-command` into `my-command-v2`, and issue `/fuji reload`.**
- Target: The command descriptor should be able to un-register the old command node in the command tree, even the new command node has different structure compared to the old one.

[Test Case]
- Module: core
- Action: **Test the `GUI linking` in paged GUI.**
- Target: Issue `/fuji`, and click `core` - `About`, then press `Esc` key to close the GUIs.
- Target: Issue `/fuji`, and click the `afk` module, to open the module details GUI, then press `Esc` key to close this GUI.
- Target: Issue `/fuji`, click `Next Page` button twice, and click any module here, then press `Esc` key to close this GUI.

[Test Case]
- Module: core
- Action: **Test the `optional argument` functionality.**
- Target: Issue `/send-title @s --mainTitle "main"`
- Target: Issue `/send-title @s --mainTitle "main" --subTitle "sub"`
- Target: Issue `/send-title @s --subTitle "sub" --mainTitle "main"`

[Test Case]
- Module: core
- Action: **Test the `search` button in paged GUI.**
- Target: Issue `/fuji`, and search with keyword `a` twice, then close the GUI. The same GUI should not be linked.
- Target: Issue `/fuji`, and search with keyword `afk`, then close the GUI. The different GUI should be linked.
- Target: Issue `/fuji`, and search with keyword `world`, then the GUI elements in other pages should be initialized for this search.

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
- Module: core
- Action: **Test the exception handler functions.**
- Target: This mod failed at server startup, due to mixin injection errors.
- Target: This mod failed at server startup, due to module initialization failed.
- Target: This mod failed at the execution of `/fuji reload` command.
- Target: This mod failed at the execution of `/json read a b` command.
- Target: This mod failed at the execution of `/run as console run as player %player:name% bad` command.

[Test Case]
- Module: core
- Action: **Test the functionality of async chunk loading.**
- Target: The RTP process should not block the game-playing. (Ticking entities, selecting target blocks...)
- Target: Throwing item entities during RTP process, the game should be ticked normally.
- Target: Start 3 RTP processes at the same time, it should be processed normally.
- Target: Run `/execute as @a run rtp` command, it should be processed normally.

[Test Case]
- Module: core
- Action: **Test the parsers in Sign block and Anvil block.**
- Target: The `color.sign` and `color.anvil` should work in single-player world, when installed client-side.

[Test Case]
- Module: disabler.move_wrongly_disabler
- Action: **Sit in a `boat` and try to move it.**
- Target: The `vehicle moved wrongly` should be disabled.

[Test Case]
- Module: document
- Action: **Test the generated document files.**
- Target: Check the heading levels, ensure the TOC is generated properly.
- Target: Check the `newline` and `indent`.
- Target: Check simple files: `afk.effect.md`.
- Target: Check the search function: `command_advice.md`
- Target: Check `ordered list` and `un-ordered list`: `rank.md`, `rtp.md`
- Target: Check the `indent` for a `multi-line list item`: `core.md`
- Target: Check `tags escaping`: `predicate.md`, `placeholder.md`
- Target: Check complex files: `command_bundle.md`, `command_meta.IF.md`

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
- Target: The nametag entity should be removed in the old dimension.
- Target: A new nametag entity should be created in the new dimension.
- Target: A new nametag entity should be created after the use of `nether portal`
- Target: A new nametag entity should be created after the use of `ender portal`
- Target: A new nametag entity should be created after the use of `/player Steve spawn`
- Target: A new nametag entity should be removed after the use of `/kill Steve`
- Target: A new nametag entity should be seen after mounting a `pig` entity.
- Target: A new nametag entity should be seen after dis-mounting a `pig` entity.

[Test Case]
- Module: sit
- Action: **Issue `/sit` command while stepping on the `bed block`.**
- Target: The raycast height should be proper.

[Test Case]
- Module: skin
- Action: **Try to send a chat message after the skin changed in online-mode server.**
- Target: The chat message validation should be proper after a player changed its skin.
- Target: It should work in both `online-mode` and `offline-mode` servers.

[Test Case]
- Module: system_message
- Action: **Test the functionality of this module.**
- Target: The player joined text should be modified, with custom color.
- Target: The player left text sending should be cancelled
- Target: The chest title text should be modified
- Target: Issue `/gamerule showDeathMessages` command, you should see the command feedback.
- Target: Issue `/seed` command, you should see the modified command feedback.
- Target: Issue `/ban` command, you should see the modified ban screen.
- Target: Fell from a high place, you should see the modified death message.

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

