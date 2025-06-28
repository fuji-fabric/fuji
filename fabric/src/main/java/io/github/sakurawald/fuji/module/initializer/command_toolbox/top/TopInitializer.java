package io.github.sakurawald.fuji.module.initializer.command_toolbox.top;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;


public class TopInitializer extends ModuleInitializer {

    @Document("Teleport to the top of your current position.")
    @CommandNode("top")
    private static int top(@CommandSource @CommandTarget ServerPlayerEntity player) {
        World world = player.getWorld();
        BlockPos topPosition = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, player.getBlockPos());

        GlobalPos globalPos = GlobalPos.of(player).withY(topPosition.getY());
        globalPos.teleport(player);

        TextHelper.sendMessageByKey(player, "top");
        return CommandHelper.Return.SUCCESS;
    }

}
