package io.github.sakurawald.module.initializer.command_toolbox.ping;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;


public class PingInitializer extends ModuleInitializer {

    @CommandNode("ping")
    @CommandRequirement(level = 4)
    @Document("Query the ping of a player.")
    private static int $ping(@CommandSource ServerCommandSource source, ServerPlayerEntity target) {
        String name = target.getGameProfile().getName();

        int latency = target.networkHandler.getLatency();
        TextHelper.sendMessageByKey(source, "ping.player", name, latency);

        return CommandHelper.Return.SUCCESS;
    }

}
