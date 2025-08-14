package io.github.sakurawald.fuji.core.command.structure;

import com.mojang.brigadier.context.CommandContext;
import lombok.Data;
import net.minecraft.server.command.ServerCommandSource;

@SuppressWarnings("ClassCanBeRecord")
@Data
public class CommandActor {

    final CommandContext<ServerCommandSource> commandContext;

}
