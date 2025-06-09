package io.github.sakurawald.module.initializer.command_toolbox.freeze;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class FreezeInitializer extends ModuleInitializer {

    @CommandNode("freeze")
    @CommandRequirement(level = 4)
    @Document("Freeze a player for ticks.")
    private static int freeze(@CommandSource ServerCommandSource source, ServerPlayerEntity player, int ticks) {
        player.setFrozenTicks(ticks);
        return CommandHelper.Return.SUCCESS;
    }
}
