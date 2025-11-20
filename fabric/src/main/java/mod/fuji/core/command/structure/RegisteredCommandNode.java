package mod.fuji.core.command.structure;

import com.mojang.brigadier.tree.CommandNode;
import lombok.Data;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

@Data
public class RegisteredCommandNode {

    public final @NotNull CommandNode<CommandSourceStack> parent;
    public final @NotNull CommandNode<CommandSourceStack> node;

}
