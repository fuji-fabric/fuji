package mod.fuji.core.command.structure;

import com.mojang.brigadier.context.CommandContext;
import lombok.Value;
import net.minecraft.server.command.ServerCommandSource;

@SuppressWarnings("ClassCanBeRecord")
@Value
public class CommandActor {

    CommandContext<ServerCommandSource> commandContext;

}
