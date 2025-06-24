package io.github.sakurawald.module.initializer.command_menu;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.executor.CommandExecutor;
import io.github.sakurawald.core.command.structure.ExtendedCommandSource;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.command_menu.command.argument.wrapper.MenuName;
import io.github.sakurawald.module.initializer.command_menu.config.CommandMenuConfigModel;
import io.github.sakurawald.module.initializer.command_menu.config.CommandMenuMenusModel;
import io.github.sakurawald.module.initializer.command_menu.structure.MenuDescriptor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document("""
    This module allows you to define `menu` GUI, to execute commands.
    """)
@CommandNode("command-menu")
@CommandRequirement(level = 4)
public class CommandMenuInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<CommandMenuConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, CommandMenuConfigModel.class);
    public static final BaseConfigurationHandler<CommandMenuMenusModel> menus = new ObjectConfigurationHandler<>("menus.json", CommandMenuMenusModel.class);

    @Document("Open the specified `menu` for the player.")
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

    @Document("Close the currently `opened GUI` for the player.")
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
