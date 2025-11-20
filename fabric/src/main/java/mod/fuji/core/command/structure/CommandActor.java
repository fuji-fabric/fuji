package mod.fuji.core.command.structure;

import com.mojang.brigadier.context.CommandContext;
import lombok.Value;
import net.minecraft.commands.CommandSourceStack;

@SuppressWarnings("ClassCanBeRecord")
@Value
public class CommandActor {

    CommandContext<CommandSourceStack> commandContext;

}
