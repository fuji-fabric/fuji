package io.github.sakurawald.fuji.module.initializer.command_menu;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.command_menu.command.argument.wrapper.MenuName;
import io.github.sakurawald.fuji.module.initializer.command_menu.config.CommandMenuConfigModel;
import io.github.sakurawald.fuji.module.initializer.command_menu.config.CommandMenuMenusModel;
import io.github.sakurawald.fuji.module.initializer.command_menu.structure.MenuDescriptor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751824895470L, value = """
    This module allows you to define `menu` GUI, to execute commands.
    """)

@ColorBox(id = 1751870445592L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    The `/command-menu open` command is an `admin-level` command.
    You need to use `command_bundle` module, to creat a `user-level` command.
    """)


@CommandNode("command-menu")
@CommandRequirement(level = 4)
public class CommandMenuInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandMenuConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandMenuConfigModel.class);
    public static final BaseConfigurationHandler<CommandMenuMenusModel> menus = new ObjectConfigurationHandler<>("menus.json", CommandMenuMenusModel.class);

    @Document(id = 1751824900662L, value = "Open the specified `menu` for the player.")
    @CommandNode("open")
    private static int $open(@CommandSource ServerCommandSource source, ServerPlayerEntity player, MenuName menuName) {
        /* Check if menu exists. */
        String $menuName = menuName.getValue();
        if (!menus.model().menus.containsKey($menuName)) {
            TextHelper.getTextByKey(source, "command_menu.menu.not_found", $menuName);
            return CommandHelper.Return.FAIL;
        }

        /* Make the menu GUI and open it. */
        MenuDescriptor menuDescriptor = menus.model().menus.get($menuName);
        menuDescriptor.build(player)
            .open();

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
        CommandExecutor.execute(ExtendedCommandSource.asConsole(player.getCommandSource()), config.model().onSneakingAndSwapHandsEvent.commands);
    }

}
