# Checklist

It's good to have a checklist, to avoid forgetting something.

## Bump version

- read the change log of minecraft version
- update the version of fabric loader
- update the version of mappings
- update the version of dependent mods
- test the jar file in a real environment.

## Test a new version

- test interesting commands. (from a to z)
- test interesting mixins.
- test the integration with carpet-fabric.
- test the integration with luckperms.

## Some interesting tests

- Summon a fake player using `/player 1 spawn` and throw exp bottle to it.
    - Test the luckperms integration with fake-player user.
- Issue the command `/tppos --z 64 --x 32 --y 128`
    - Test the context passing after command redirection.
- Issue the command `/send-title @s --mainTitle "<rainbow>Hello" --subTitle "<blue>World" --fadeInTicks 60 --stayTicks 60 --fadeOutTicks 60`
- See if a pickaxe gets the max power level in `/enchantment`
    - Test the lambda for power of providers.
- See an inventory display contains a shulker box.
    - Test the deep-level gui.
    - Test the shulker entity reader.
- Try to move a player in afk state.
- Buy a new head in `/head`.
- Get a kit from `/kit`.
    - Test the screen gui.
    - Test the `/kit give` command while inventory is full. (note that the player in creative mode can always pick up the same items even the inventory is full.)
    - Issue `/clear`, `/give @s minecraft:apple 32`, `/kit give`...
- Start a sample for a production work.
    - Test the sign input gui.
    - Test the chunks iterator.
    - Test the hopper mixins.
- Teleport to an offline player's location using `/tppos offline`.
    - Test the player instance making.
    - Test the offline data reader.
- Enter afk using `/afk` and watch the player list.
- Test command requirements:
    - Test the requirement of `/warp`
    - Test the requirement of `/world`
- Open a `shulker box display` from a `inventory display`. 
- Test the command tree for a default user.
- Test platform environments:
    - The fabric server-side environment.
    - The fabric client-side environment.
    - The neoforge client-side environment.
    - The neoforge server-side environment.
- Test `/save-all` without the `fabric-api` mod installed.
- Test `/reload`, `/fuji reload`, `/fuji inspect fuji-commands` and `/command-bundle list`
- Special states of a player
    - as a fake-player
    - died
    - offline
    - in spectator gamemode
    - in the end portal
- Test `/stop` and see if the server closed (in production server).
- Test the translatable text in `[item]` or `%fuji:item%` in chat message.
- Place a `sign` to see if colorful `Text` can be parsed and reversed.
- Sit in a `boat` and move it.
- Issue `/sit` and sit in a bed, to see if the raycast height is correct.
- Issue `/view {inv/ender}` command, and see if it can change the inv of another player on the fly.
- Issue `/home` to see if it's rewrited to `/home tp default`.
- Issue `/back` to see if the `command advice` is processed exactly once. 
- Issue `/say hi` to see if the `command advice` can cancel with another command. 
- Check if the `nametag` entity blocks the `nether` and `the end` portal.
- For `chat` and its submodules.
  - Test in online-mode and offline-mode.
  - Test with `chat.style` and `Styled Chat`.
- Test the `command_warmup` module and `command_interactive` module:
  - In `online-mode` server.
  - Test `/back` command.
  - Test `/say hello world` command. (The SayCommand uses SignedArgument.)
- To test `/fuji inspect configurations`, use the config files from `command_menu` module.
- Issue `/fuji reload`, to see if `command scheduler` jobs are `re-scheduled`.
- Run the `/run as console save-all` command.
- Run `/tp` and `/world tp` between `dimensions`, and see if the `world border` is synced in the `client-side`.
- In `MC 1.20.1`, create a `overworld` dimension type with `seed=12345`. 
  - Goto `/tp @s 14665 ~ 345`. (You should get `emerald * 7`, `gold ingot * 3`, `iron ingot * 11`, `tnt * 2`, `heart of the sea * 1`, `cooked cod * 8` and `potion of water breathing * 1`.)
  - Goto `/tp @s 0 128 0`, you should in `minecraft:ocean`, and there is a `minecraft:dark_forest` in front of you, also there is a `lava source` flowing down.

## Publish a new version

- sync the language files.
- update the version in "gradle.properties". (Maybe respect the `semvar` spec.)
- update change log in "CHANGELOG.md"
- test the jar file in a real environment.
- publish the pdf file in "dev" branch.
- modify the `build.yml` file to include the new supported MC version.
- place a sign and write `/say hi` and `back` command on it.
- push a git commit with the prefix `[publish]`


## Painful things
- Don't use star import. 
- Don't use static import in mixin class (Or better just don't use it in the project).
- Try not use @Redirect and @Override in mixin class.