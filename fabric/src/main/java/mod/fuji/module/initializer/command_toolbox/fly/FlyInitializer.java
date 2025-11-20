package mod.fuji.module.initializer.command_toolbox.fly;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.level.ServerPlayer;


public class FlyInitializer extends ModuleInitializer {

    @Document(id = 1751825231545L, value = "Toggle the fly state of the player.")
    @CommandNode("fly")
    @CommandRequirement(level = 4)
    private static int $fly(@CommandSource @CommandTarget ServerPlayer player) {
        boolean flag = !player.getAbilities().mayfly;
        return $fly(player, flag);
    }

    @Document(id = 1756705595613L, value = "Set the fly state of the player.")
    @CommandNode("fly")
    @CommandRequirement(level = 4)
    private static int $fly(@CommandSource @CommandTarget ServerPlayer player, boolean flag) {
        player.getAbilities().mayfly = flag;

        if (!flag) {
            player.getAbilities().flying = false;
        }

        player.onUpdateAbilities();
        TextHelper.sendTextByKey(player, flag ? "fly.on" : "fly.off");
        return CommandHelper.Return.SUCCESS;
    }
}
