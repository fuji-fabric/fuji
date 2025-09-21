package mod.fuji.module.initializer.command_toolbox.burn;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.EntityCollection;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

public class BurnInitializer extends ModuleInitializer {

    @Document(id = 1751825133157L, value = "Burn target entities for ticks.")
    @CommandNode("burn")
    @CommandRequirement(level = 4)
    private static int $burn(@CommandSource ServerCommandSource source, EntityCollection target, int ticks) {
        target
            .getValue()
            .forEach(entity -> entity.setFireTicks(ticks));

        return CommandHelper.Return.SUCCESS;
    }
}
