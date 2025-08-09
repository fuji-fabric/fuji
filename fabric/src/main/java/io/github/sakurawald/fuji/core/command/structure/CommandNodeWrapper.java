package io.github.sakurawald.fuji.core.command.structure;

import com.mojang.brigadier.tree.CommandNode;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import lombok.Data;
import net.minecraft.server.command.ServerCommandSource;

@Data
public class CommandNodeWrapper {

    public final CommandNode<ServerCommandSource> node;
    public final String path;

    public CommandNodeWrapper(CommandNode<ServerCommandSource> node) {
        this.node = node;
        this.path = CommandHelper.Node.findCommandNodePath(node);
    }

}
