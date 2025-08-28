package io.github.sakurawald.fuji.module.initializer.command_toolbox.freeze;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.EntityCollection;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

public class FreezeInitializer extends ModuleInitializer {

    @Document(id = 1751825159763L, value = "Freeze target entities for ticks.")
    @CommandNode("freeze")
    @CommandRequirement(level = 4)
    private static int $freeze(@CommandSource ServerCommandSource source, EntityCollection target, int ticks) {
        target
            .getValue()
            .forEach(entity -> entity.setFrozenTicks(ticks));

        return CommandHelper.Return.SUCCESS;
    }
}
