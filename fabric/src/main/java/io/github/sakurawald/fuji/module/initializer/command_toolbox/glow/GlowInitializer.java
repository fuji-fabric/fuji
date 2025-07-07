package io.github.sakurawald.fuji.module.initializer.command_toolbox.glow;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class GlowInitializer extends ModuleInitializer {

    @Document(id = 1751825116796L, value = "Toggle the glowing state.")
    @CommandNode("glow")
    private static int $glow(@CommandSource @CommandTarget ServerPlayerEntity player) {
        boolean flag = !player.isGlowing();
        player.setGlowing(flag);
        TextHelper.sendTextByKey(player, flag ? "glow.on" : "glow.off");
        return CommandHelper.Return.SUCCESS;
    }

}
