package io.github.sakurawald.module.initializer.command_permission.structure;

import io.github.sakurawald.core.structure.CommandNodeWrapper;
import lombok.Getter;
import net.minecraft.server.command.ServerCommandSource;

@Getter
public class CommandNodePermission extends CommandNodeWrapper {

    final boolean wrapped;

    public CommandNodePermission(com.mojang.brigadier.tree.CommandNode<ServerCommandSource> commandNode) {
        super(commandNode);
        this.wrapped = commandNode.getRequirement() instanceof WrappedPredicate<ServerCommandSource>;
    }
}
