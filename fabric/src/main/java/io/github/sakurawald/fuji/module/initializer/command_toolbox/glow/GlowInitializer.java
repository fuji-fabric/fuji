package io.github.sakurawald.fuji.module.initializer.command_toolbox.glow;

import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.EntityCollection;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;
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

    @CommandNode("glow")
    @CommandRequirement(level = 4)
    private static int $glow(@CommandSource ServerCommandSource source, EntityCollection entities) {
        entities
            .getValue()
            .forEach(entity -> entity.setGlowing(!entity.isGlowing()));
        return CommandHelper.Return.SUCCESS;
    }

}
