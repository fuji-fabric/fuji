package io.github.sakurawald.module.initializer.command_toolbox.extinguish;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class ExtinguishInitializer extends ModuleInitializer {


    @CommandNode("extinguish")
    @CommandRequirement(level = 4)
    @Document("Set fire ticks to 0.")
    private static int $extinguish(@CommandSource @CommandTarget ServerPlayerEntity player) {
        player.setFireTicks(0);
        return CommandHelper.Return.SUCCESS;
    }

}
