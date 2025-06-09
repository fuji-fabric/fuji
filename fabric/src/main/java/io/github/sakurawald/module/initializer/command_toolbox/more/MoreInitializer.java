package io.github.sakurawald.module.initializer.command_toolbox.more;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;


public class MoreInitializer extends ModuleInitializer {

    @CommandNode("more")
    @CommandRequirement(level = 4)
    @Document("Set the count of item in hand to max count.")
    private static int $more(@CommandSource CommandContext<ServerCommandSource> ctx) {
        return CommandHelper.Pattern.itemInHandCommand(ctx, (player, itemStack) -> {
            itemStack.setCount(itemStack.getMaxCount());
            return CommandHelper.Return.SUCCESS;
        });
    }

}
