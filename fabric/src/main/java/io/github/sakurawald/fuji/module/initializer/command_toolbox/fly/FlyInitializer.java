package io.github.sakurawald.fuji.module.initializer.command_toolbox.fly;

import io.github.sakurawald.fuji.core.command.structure.CommandActor;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class FlyInitializer extends ModuleInitializer {

    @Document(id = 1751825231545L, value = "Toggle the fly state of the player.")
    @CommandNode("fly")
    @CommandRequirement(level = 4)
    private static int $fly(CommandActor actor, @CommandSource @CommandTarget ServerPlayerEntity player) {
        boolean flag = !player.getAbilities().allowFlying;
        player.getAbilities().allowFlying = flag;

        if (!flag) {
            player.getAbilities().flying = false;
        }

        player.sendAbilitiesUpdate();
        TextHelper.sendTextByKey(player, flag ? "fly.on" : "fly.off");
        return CommandHelper.Return.SUCCESS;
    }
}
