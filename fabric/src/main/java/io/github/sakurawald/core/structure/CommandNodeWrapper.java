package io.github.sakurawald.core.structure;

import com.mojang.brigadier.tree.CommandNode;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import lombok.Data;
import net.minecraft.server.command.ServerCommandSource;

@Data
public class CommandNodeWrapper {

    final CommandNode<ServerCommandSource> node;
    final String path;

    public CommandNodeWrapper(CommandNode<ServerCommandSource> node) {
        this.node = node;
        this.path = CommandHelper.computeCommandNodePath(node);
    }

}
