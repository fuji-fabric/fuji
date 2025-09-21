package mod.fuji.module.initializer.command_toolbox.down;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.service.random_teleport.searcher.PositionYDownTopSearcher;
import mod.fuji.core.structure.GlobalPos;
import mod.fuji.module.initializer.ModuleInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;


public class DownInitializer extends ModuleInitializer {

    @Document(id = 1757792247065L, value = "Teleport to the lowest of your current position.")
    @CommandNode("down")
    private static int $down(@CommandSource @CommandTarget ServerPlayerEntity player) {
        World world = PlayerHelper.getServerWorld(player);
        BlockPos blockPos = player.getBlockPos();
        Chunk chunk = world.getChunk(blockPos);
        int resultY = new PositionYDownTopSearcher()
            .search(chunk, blockPos.getX(), blockPos.getZ())
            .orElseGet(blockPos::getY);

        GlobalPos globalPos = GlobalPos
            .of(player)
            .withY(resultY);
        globalPos.teleport(player);

        TextHelper.sendTextByKey(player, "down");
        return CommandHelper.Return.SUCCESS;
    }

}
