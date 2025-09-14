package io.github.sakurawald.fuji.module.initializer.command_toolbox.top;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.service.random_teleport.searcher.PositionYTopDownSearcher;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;


public class TopInitializer extends ModuleInitializer {

    @Document(id = 1751825123433L, value = "Teleport to the top of your current position.")
    @CommandNode("top")
    private static int $top(@CommandSource @CommandTarget ServerPlayerEntity player) {
        World world = player.getWorld();
        BlockPos blockPos = player.getBlockPos();
        Chunk chunk = world.getChunk(blockPos);
        int resultY = new PositionYTopDownSearcher()
            .search(chunk, blockPos.getX(), blockPos.getZ())
            .orElseGet(blockPos::getY);

        GlobalPos globalPos = GlobalPos
            .of(player)
            .withY(resultY);
        globalPos.teleport(player);

        TextHelper.sendTextByKey(player, "top");
        return CommandHelper.Return.SUCCESS;
    }

}
