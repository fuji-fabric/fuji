package mod.fuji.module.initializer.command_permission.structure;

import mod.fuji.core.command.structure.CommandNodeWrapper;
import mod.fuji.module.initializer.command_permission.service.CommandPermissionService;
import lombok.Getter;
import net.minecraft.server.command.ServerCommandSource;

@Getter
public class CommandNodePermissionWrapper extends CommandNodeWrapper {

    final boolean wrapped;

    public CommandNodePermissionWrapper(com.mojang.brigadier.tree.CommandNode<ServerCommandSource> commandNode) {
        super(commandNode);
        this.wrapped = CommandPermissionService.isCommandNodeWrapped(commandNode);
    }

}
