package io.github.sakurawald.fuji.module.initializer.warning;


import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;

public class WarningInitializer extends ModuleInitializer {

    @CommandNode("warning")
    @CommandRequirement(level = 4)
    private static int $warning(@CommandSource ServerPlayerEntity player) {



        return CommandHelper.Return.SUCCESS;
    }


}
