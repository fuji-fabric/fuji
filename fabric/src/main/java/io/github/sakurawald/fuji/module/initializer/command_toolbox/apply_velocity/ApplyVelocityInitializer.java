package io.github.sakurawald.fuji.module.initializer.command_toolbox.apply_velocity;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.EntityCollection;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1756293176728L, value = """
    Provides `/apply-velocity` command.
    """)
public class ApplyVelocityInitializer extends ModuleInitializer {

    @CommandNode(value = "apply-velocity")
    @CommandRequirement(level = 4)
    private static int $applyVelocity(@CommandSource ServerCommandSource source, EntityCollection target, double x, double y, double z) {
        target.getValue().forEach(entity -> {
            EntityHelper.applyVelocity(entity, x, y, z);
        });

        return CommandHelper.Return.SUCCESS;
    }

}
