package io.github.sakurawald.module.initializer.command_toolbox.fly;

import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class FlyInitializer extends ModuleInitializer {

    @CommandNode("fly")
    @CommandRequirement(level = 4)
    private static int $fly(@CommandSource @CommandTarget ServerPlayerEntity player) {
        boolean flag = !player.getAbilities().allowFlying;
        player.getAbilities().allowFlying = flag;

        if (!flag) {
            player.getAbilities().flying = false;
        }

        player.sendAbilitiesUpdate();
        TextHelper.sendMessageByKey(player, flag ? "fly.on" : "fly.off");
        return CommandHelper.Return.SUCCESS;
    }
}
