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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;


public class GlowInitializer extends ModuleInitializer {

    @Document(id = 1751825116796L, value = "Toggle the glowing state.")
    @CommandNode("glow")
    private static int $glow(@CommandSource @CommandTarget ServerPlayer player) {
        boolean flag = !player.isCurrentlyGlowing();
        player.setGlowingTag(flag);
        TextHelper.sendTextByKey(player, flag ? "glow.on" : "glow.off");
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("glow")
    @CommandRequirement(level = 4)
    private static int $glow(@CommandSource CommandSourceStack source, EntityCollection entities) {
        entities
            .getValue()
            .forEach(entity -> entity.setGlowingTag(!entity.isCurrentlyGlowing()));
        return CommandHelper.Return.SUCCESS;
    }

}
