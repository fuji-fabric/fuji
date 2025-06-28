package io.github.sakurawald.fuji.module.initializer.command_toolbox.god;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class GodInitializer extends ModuleInitializer {

    @Document("Toggle the invulnerable state.")
    @CommandNode("god")
    @CommandRequirement(level = 4)
    private static int $god(@CommandSource @CommandTarget ServerPlayerEntity player) {
        boolean flag = !player.getAbilities().invulnerable;
        player.getAbilities().invulnerable = flag;
        player.sendAbilitiesUpdate();

        TextHelper.sendMessageByKey(player, flag ? "god.on" : "god.off");
        return CommandHelper.Return.SUCCESS;
    }

}
