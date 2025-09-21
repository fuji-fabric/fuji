package mod.fuji.module.initializer.command_toolbox.glow;

import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.argument.wrapper.impl.EntityCollection;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.module.initializer.ModuleInitializer;
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
