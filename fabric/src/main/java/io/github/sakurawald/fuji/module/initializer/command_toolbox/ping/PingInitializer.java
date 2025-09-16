package io.github.sakurawald.fuji.module.initializer.command_toolbox.ping;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;


public class PingInitializer extends ModuleInitializer {

    @Document(id = 1751825386112L, value = "Query the ping of a player.")
    @CommandNode("ping")
    @CommandRequirement(level = 4)
    private static int $ping(@CommandSource ServerCommandSource source, ServerPlayerEntity target) {
        String targetPlayerName = PlayerHelper.getPlayerName(target);
        int latency = PlayerHelper.getPing(target);
        TextHelper.sendTextByKey(source, "ping.player", targetPlayerName, latency);

        return CommandHelper.Return.SUCCESS;
    }

}
