package io.github.sakurawald.fuji.module.initializer.command_toolbox.top;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;


public class TopInitializer extends ModuleInitializer {

    @Document(id = 1751825123433L, value = "Teleport to the top of your current position.")
    @CommandNode("top")
    private static int $top(@CommandSource @CommandTarget ServerPlayerEntity player) {
        ServerWorld serverWorld = PlayerHelper.getServerWorld(player);
        BlockPos blockPos = player.getBlockPos();
        GlobalPos globalPos = WorldHelper.findSafeTopY(serverWorld, blockPos);
        globalPos.teleport(player);

        TextHelper.sendTextByKey(player, "top");
        return CommandHelper.Return.SUCCESS;
    }

}
