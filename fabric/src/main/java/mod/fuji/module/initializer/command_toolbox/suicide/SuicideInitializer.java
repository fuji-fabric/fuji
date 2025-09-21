package mod.fuji.module.initializer.command_toolbox.suicide;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;


public class SuicideInitializer extends ModuleInitializer {

    @Document(id = 1751825206093L, value = "Kill yourself.")
    @CommandNode("suicide")
    private static int $suicide(@CommandSource ServerPlayerEntity player) {
        EntityHelper.killEntity(player);
        return CommandHelper.Return.SUCCESS;
    }

}
