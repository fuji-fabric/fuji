package io.github.sakurawald.module.initializer.command_toolbox.suicide;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class SuicideInitializer extends ModuleInitializer {

    @CommandNode("suicide")
    @Document("Kill yourself.")
    private static int $suicide(@CommandSource ServerPlayerEntity player) {
        player.kill(player.getServerWorld());
        return CommandHelper.Return.SUCCESS;
    }

}
