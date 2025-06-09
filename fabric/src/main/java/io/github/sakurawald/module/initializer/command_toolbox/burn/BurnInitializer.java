package io.github.sakurawald.module.initializer.command_toolbox.burn;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class BurnInitializer extends ModuleInitializer {

    @CommandNode("burn")
    @CommandRequirement(level = 4)
    @Document("Burn a target player for ticks.")
    private static int burn(@CommandSource ServerCommandSource source, ServerPlayerEntity player, int ticks) {
        player.setFireTicks(ticks);
        return CommandHelper.Return.SUCCESS;
    }
}
