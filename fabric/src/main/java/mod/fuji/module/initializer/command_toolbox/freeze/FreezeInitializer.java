package mod.fuji.module.initializer.command_toolbox.freeze;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.EntityCollection;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.commands.CommandSourceStack;

public class FreezeInitializer extends ModuleInitializer {

    @Document(id = 1751825159763L, value = "Freeze target entities for ticks.")
    @CommandNode("freeze")
    @CommandRequirement(level = 4)
    private static int $freeze(@CommandSource CommandSourceStack source, EntityCollection target, int ticks) {
        target
            .getValue()
            .forEach(entity -> entity.setTicksFrozen(ticks));

        return CommandHelper.Return.SUCCESS;
    }
}
