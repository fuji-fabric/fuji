package io.github.sakurawald.fuji.module.initializer.command_permission.structure;

import io.github.sakurawald.fuji.core.command.structure.CommandNodeWrapper;
import io.github.sakurawald.fuji.module.initializer.command_permission.CommandPermissionInitializer;
import lombok.Getter;
import net.minecraft.server.command.ServerCommandSource;

@Getter
public class CommandNodePermissionWrapper extends CommandNodeWrapper {

    final boolean wrapped;

    public CommandNodePermissionWrapper(com.mojang.brigadier.tree.CommandNode<ServerCommandSource> commandNode) {
        super(commandNode);
        this.wrapped = CommandPermissionInitializer.isCommandNodeWrapped(commandNode);
    }

}
