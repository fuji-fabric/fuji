package mod.fuji.module.initializer.command_menu;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.executor.CommandExecutor;
import mod.fuji.core.command.executor.structure.ExtendedCommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerActionEvent;
import mod.fuji.core.manager.impl.task.GameTaskManager;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.command_menu.command.argument.wrapper.MenuName;
import mod.fuji.module.initializer.command_menu.config.CommandMenuConfigModel;
import mod.fuji.module.initializer.command_menu.config.CommandMenuMenusModel;
import mod.fuji.module.initializer.command_menu.structure.MenuDescriptor;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751824895470L, value = """
    This module allows you to define `menu` GUI, to execute commands.
    """)
@ColorBox(id = 1752900650332L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The definition of `menu` and `slot`.
    A `menu` is a virtual `container GUI`.
    The `virtual GUI` is composed of `slots`.
    A `slot` is used to hold an `item stack`.
    The `minimal size` of the GUI is `9 x 1 = 9 slots`
    The `maximal size` of the GUI is `9 x 6 = 54 slots`

    <green>In short, you can define a `menu` to hold `slots`.
    <green>And bind `commands` to the `slots`.
    """)
@ColorBox(id = 1751870445592L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Use the built-in `event`, to open the `GUI`
    The `command_menu` module provides an event called `onSneakingAndSwapHandsEvent`.
    It will be triggered when a player press `SHIFT + F` key.
    You can configure this event in the config file.

    ◉ Use `command_bundle` to create a command, to open the `GUI`
    The `/command-menu open` command is an `admin-level` command.
    You need to use `command_bundle` module, to creat a `user-level` command.
    """)
@ColorBox(id = 1756687876655L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Switch the `command context` of `command execution`.
    All commands bound to `slot` are executed `as console`.
    You may need the `command_meta.run` module, to modify the `command context`.
    - `/run as console \\<cmd\\>`
    - `/run as player %player:name% \\<cmd\\>`
    - `/run as fake-op %player:name% \\<cmd\\>`
    """)
@ColorBox(id = 1751968513281L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Create a `nested` menus.
    If you want to create a `nested menu`:
    Click a `slot` in `menu A`, it will opens the `menu B`.
    Then you need to disable the `close_menu_on_clicked` option for `menu A`.
    To prevent the `menu B` being `opened` and `closed instantly`.

    ◉ Handle the `menu closing` manually.
    You can use `/run as fake-op %player:name% command-menu close %player:name%` command.
    To `close` the `opened GUI` for a `player`.
    """)
@ColorBox(id = 1753435167488L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Use the `menu editor` online to design a menu easily.
    Thanks to <dark_green>Hitnam</dark_green>, we have an `online editor` for `command_menu` module.
    The tool is hosted in: https://fuji-command-menu-editor-k4k4.vercel.app/
    """)
@ColorBox(id = 1755690116092L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ Use a custom player skull as the slot's icon.
    You can modify the `item` property for that slot directly.
    The syntax of `item` is the same as the `/give \\<item\\>` command.

    <green>NOTE: The data format may be different across MC versions.
    1. `/give @s minecraft:player_head[minecraft:profile=Steve]`
    2. `/give @s minecraft:player_head{SkullOwner:"Steve"}`
    """)


@CommandNode("command-menu")
@CommandRequirement(level = 4)
public class CommandMenuInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandMenuConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, CommandMenuConfigModel.class);
    public static final BaseConfigurationHandler<CommandMenuMenusModel> menus = ObjectConfigurationHandler.ofModule("menus.json", CommandMenuMenusModel.class);

    @Document(id = 1751824900662L, value = "Open the specified `menu` for the player.")
    @CommandNode("open")
    private static int $open(@CommandSource ServerCommandSource source, ServerPlayerEntity player, MenuName menuName) {
        /* Check if menu exists. */
        String $menuName = menuName.getValue();
        if (!menus.model().getMenus().containsKey($menuName)) {
            TextHelper.getTextByKey(source, "command_menu.menu.not_found", $menuName);
            return CommandHelper.Return.FAILURE;
        }

        /* Make the menu GUI and open it. */
        MenuDescriptor menuDescriptor = menus.model().getMenus().get($menuName);
        // NOTE: Schedule this task at next tick, making the opening and closing of nested menus easier.
        GameTaskManager.runInTicks(1, () -> {
            menuDescriptor.build(player)
                .open();
        });
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751824905935L, value = "Close the currently `opened GUI` for the player.")
    @CommandNode("close")
    private static int $close(@CommandSource ServerCommandSource source, ServerPlayerEntity player) {
        closeCurrentHandledScreen(player);
        return CommandHelper.Return.SUCCESS;
    }

    public static void closeCurrentHandledScreen(ServerPlayerEntity player) {
        player.closeHandledScreen();
    }

    public static void executeOnSneakingAndSwapHandsCommands(ServerPlayerEntity player) {
        CommandExecutor.executeBatch(ExtendedCommandSource.asConsole(player.getCommandSource()), config.model().onSneakingAndSwapHandsEvent.commands);
    }

    @EventConsumer
    private static void consumePlayerActionEvent(PlayerActionEvent event) {
        if (!CommandMenuInitializer.config.model().onSneakingAndSwapHandsEvent.enable) return;

        PlayerActionC2SPacket packet = event.getPacket();
        ServerPlayerEntity player = event.getPlayer();
        if (packet.getAction() == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND && player.isSneaking()) {
            CommandMenuInitializer.executeOnSneakingAndSwapHandsCommands(player);
            event.getCallbackInfo().cancel();
        }
    }

}
