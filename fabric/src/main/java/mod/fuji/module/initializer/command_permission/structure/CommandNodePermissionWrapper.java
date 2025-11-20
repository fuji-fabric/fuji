package mod.fuji.module.initializer.command_permission.structure;

import mod.fuji.core.command.structure.CommandNodeWithPath;
import mod.fuji.module.initializer.command_permission.service.CommandPermissionService;
import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;

@Getter
public class CommandNodePermissionWrapper extends CommandNodeWithPath {

    final boolean wrapped;

    public CommandNodePermissionWrapper(com.mojang.brigadier.tree.CommandNode<CommandSourceStack> commandNode) {
        super(commandNode);
        this.wrapped = CommandPermissionService.isCommandNodeWrapped(commandNode);
    }

}
