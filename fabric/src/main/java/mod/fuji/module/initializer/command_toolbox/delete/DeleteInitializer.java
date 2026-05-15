package mod.fuji.module.initializer.command_toolbox.delete;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.argument.wrapper.impl.EntityCollection;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.commands.CommandSourceStack;

public class DeleteInitializer extends ModuleInitializer {

    @CommandNode("delete")
    @CommandRequirement(level = 4)
    @Document(id = 1778870295366L, value = "Delete the target entity from the dimension.")
    private static int $delete(@CommandSource CommandSourceStack source, EntityCollection target) {
        target
            .getValue()
            .stream()
            .filter(it -> !PlayerHelper.Kind.isServerPlayer(it))
            .forEach(EntityHelper::deleteEntity);
        return CommandHelper.Return.SUCCESS;
    }
}
