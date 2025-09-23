package mod.fuji.core.command.structure;

import com.mojang.brigadier.tree.CommandNode;
import lombok.Data;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

@Data
public class RegisteredCommandNode {

    public final @NotNull CommandNode<ServerCommandSource> parent;
    public final @NotNull CommandNode<ServerCommandSource> node;

}
