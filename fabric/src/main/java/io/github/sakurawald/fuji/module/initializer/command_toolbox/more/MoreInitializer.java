package io.github.sakurawald.fuji.module.initializer.command_toolbox.more;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;


public class MoreInitializer extends ModuleInitializer {

    @Document("Set the count of item in hand to max count.")
    @CommandNode("more")
    @CommandRequirement(level = 4)
    private static int $more(@CommandSource CommandContext<ServerCommandSource> ctx) {
        return CommandHelper.Pattern.itemInHandCommand(ctx, (player, itemStack) -> {
            itemStack.setCount(itemStack.getMaxCount());
            return CommandHelper.Return.SUCCESS;
        });
    }

}
