package mod.fuji.module.initializer.command_toolbox.ping;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;


public class PingInitializer extends ModuleInitializer {

    @Document(id = 1751825386112L, value = "Query the ping of a player.")
    @CommandNode("ping")
    @CommandRequirement(level = 4)
    private static int $ping(@CommandSource CommandSourceStack source, ServerPlayer target) {
        String targetPlayerName = PlayerHelper.getPlayerName(target);
        int latency = PlayerHelper.getPing(target);
        TextHelper.sendTextByKey(source, "ping.player", targetPlayerName, latency);

        return CommandHelper.Return.SUCCESS;
    }

}
