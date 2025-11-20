package mod.fuji.core.command.structure;

import com.mojang.brigadier.tree.CommandNode;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import lombok.Data;
import net.minecraft.commands.CommandSourceStack;

@Data
public class CommandNodeWithPath {

    public final CommandNode<CommandSourceStack> node;
    public final String path;

    public CommandNodeWithPath(CommandNode<CommandSourceStack> node) {
        this.node = node;
        this.path = CommandHelper.Tree.findCommandNodePathString(node);
    }

}
