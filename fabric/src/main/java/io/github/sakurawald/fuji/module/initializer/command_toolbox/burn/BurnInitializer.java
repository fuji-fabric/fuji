package io.github.sakurawald.fuji.module.initializer.command_toolbox.burn;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class BurnInitializer extends ModuleInitializer {

    @Document(id = 1751825133157L, value = "Burn a target player for ticks.")
    @CommandNode("burn")
    @CommandRequirement(level = 4)
    private static int burn(@CommandSource ServerCommandSource source, ServerPlayerEntity player, int ticks) {
        player.setFireTicks(ticks);
        return CommandHelper.Return.SUCCESS;
    }
}
