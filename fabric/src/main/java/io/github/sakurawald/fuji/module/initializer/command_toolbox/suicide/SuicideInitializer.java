package io.github.sakurawald.fuji.module.initializer.command_toolbox.suicide;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class SuicideInitializer extends ModuleInitializer {

    @Document("Kill yourself.")
    @CommandNode("suicide")
    private static int $suicide(@CommandSource ServerPlayerEntity player) {
        EntityHelper.killEntity(player);
        return CommandHelper.Return.SUCCESS;
    }

}
