package io.github.sakurawald.fuji.core.service.random_teleport;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

public class PositionSearcher {

    @ForDeveloper("Search once, and return empty if there is no good result.")
    public static void search(@NotNull LocationSearchContext context) {
        final RandomTeleportSettings settings = context.getSettings();
        final BlockPos blockPosInChunk = PositionXZGenerator.getRandomXZ(settings);
        TextHelper.sendTextByKey(context.getPlayer(), "rtp.progress.checking_chunk", blockPosInChunk.getX(), blockPosInChunk.getZ(), context.getAttempts(), context.getMaxAttempts());

        /* Filter by world border. */
        final ServerWorld serverWorld = WorldHelper.getWorldOrThrow(settings.getDimension());
        if (!LocationFilter.isInsideWorldBorder(serverWorld, blockPosInChunk)) {
            TextHelper.sendTextByKey(context.getPlayer(), "rtp.progress.skip_out_of_border");
            return;
        }

        final Chunk chunk = serverWorld.getChunk(blockPosInChunk);

        if (chunk.getInhabitedTime() >= context.getSettings().getChunkInhabitedTimeLowerThanTicks()) {
            TextHelper.sendTextByKey(context.getPlayer(), "rtp.progress.skip_old_chunk");
            return;
        }

        for (BlockPos.Mutable candidateBlockPos : ChunkCandidateBlocksGenerator.getChunkCandidateBlocks(chunk.getPos())) {
            final int blockPosX = candidateBlockPos.getX();
            final int blockPosZ = candidateBlockPos.getZ();

            /* Search a good Y in given XZ position. */
            PositionYSearcher positionYSearcher = PositionYSearcher.forWorld(serverWorld);
            final Optional<Integer> blockPosY = positionYSearcher.search(chunk, blockPosX, blockPosZ);
            if (blockPosY.isEmpty()) {
                continue;
            }
            final int $blockPosY = blockPosY.get();
            BlockPos value = new BlockPos(blockPosX, $blockPosY, blockPosZ);

            /* Filter by biome. */
            String biomeId = WorldHelper.getBiomeId(serverWorld, value);
            if (settings.getBiomes().getSkip().contains(biomeId)) {
                TextHelper.sendTextByKey(context.getPlayer(), "rtp.progress.skip_biome", biomeId);
                continue;
            }

            /* Filter by block. */
            BlockPos blockPos = new BlockPos(blockPosX, $blockPosY - 1, blockPosZ);
            BlockState blockState = chunk.getBlockState(blockPos);
            if (!LocationFilter.isSafeBlock(settings, blockState)) {
                TextHelper.sendTextByKey(context.getPlayer(), "rtp.progress.skip_block", RegistryHelper.getIdAsString(blockState));
                continue;
            }

            /* Filter by Y bound. */
            if (!LocationFilter.isYInRange(settings, blockPos)) {
                TextHelper.sendTextByKey(context.getPlayer(), "rtp.progress.skip_out_of_range_y", blockPos.getY());
                continue;
            }

            /* Pass all the filters, set the result now. */
            context.setResult(Optional.of(value));
        }
    }
}
