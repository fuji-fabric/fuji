package io.github.sakurawald.module.initializer.command_toolbox.glow;

import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class GlowInitializer extends ModuleInitializer {

    @CommandNode("glow")
    @Document("Toggle the glowing state.")
    private static int $glow(@CommandSource @CommandTarget ServerPlayerEntity player) {
        boolean flag = !player.isGlowing();
        player.setGlowing(flag);
        TextHelper.sendMessageByKey(player, flag ? "glow.on" : "glow.off");
        return CommandHelper.Return.SUCCESS;
    }

}
