package io.github.sakurawald.fuji.core.service.random_teleport.searcher;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.service.async_chunk_loader.AsyncChunkLoader;
import io.github.sakurawald.fuji.core.service.random_teleport.generator.ChunkCandidateBlocksGenerator;
import io.github.sakurawald.fuji.core.service.random_teleport.generator.PositionXZGenerator;
import io.github.sakurawald.fuji.core.service.random_teleport.structure.PositionSearchContext;
import io.github.sakurawald.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import io.github.sakurawald.fuji.core.service.random_teleport.filter.PositionFilter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

public class PositionSearcher {

    @ForDeveloper("Search once, and return empty if there is no good result.")
    public static void search(@NotNull PositionSearchContext context) {
        final RandomTeleportSettings settings = context.getSettings();
        final BlockPos blockPosInChunk = PositionXZGenerator.getRandomXZ(settings);
        TextHelper.sendTextByKey(context.getPlayer(), "rtp.progress.checking_chunk", blockPosInChunk.getX(), blockPosInChunk.getZ(), context.getAttempts(), context.getMaxAttempts());

        /* Filter by world border. */
        final ServerWorld serverWorld = WorldHelper.getWorldOrThrow(settings.getDimension());
        if (!PositionFilter.isInsideWorldBorder(serverWorld, blockPosInChunk)) {
            TextHelper.sendTextByKey(context.getPlayer(), "rtp.progress.skip_out_of_border");
            return;
        }

        /* Load the chunk async and waiting off-thread. */
        ChunkPos chunkPos = WorldHelper.makeChunkPos(blockPosInChunk);
        int asyncChunkLoadingTimeoutTicks = Math.min(20 * 60, context.getSettings().getAsyncChunkLoadingTimeoutTicks());
        CompletableFuture<Void> voidCompletableFuture = AsyncChunkLoader.loadChunkAsync(serverWorld, chunkPos, asyncChunkLoadingTimeoutTicks, getChunkConsumer(context, serverWorld), getOnFailedHook(context));
        voidCompletableFuture.join();
    }

    private static @NotNull Runnable getOnFailedHook(@NotNull PositionSearchContext context) {
        return () -> {
            TextHelper.sendTextByKey(context.getPlayer(), "rtp.progress.async_chunk_loading_timeout");
        };
    }

    private static @NotNull Consumer<Chunk> getChunkConsumer(@NotNull PositionSearchContext context, @NotNull ServerWorld serverWorld) {
        return (chunk) -> {
            /* Filter by inhabited time. */
            if (chunk.getInhabitedTime() >= context.getSettings().getChunkInhabitedTimeLowerThanTicks()) {
                TextHelper.sendTextByKey(context.getPlayer(), "rtp.progress.skip_inhabited_chunk");
                return;
            }

            /* Iterate the candidate block pos. */
            for (BlockPos.Mutable candidateBlockPos : ChunkCandidateBlocksGenerator.getChunkCandidateBlocks(chunk.getPos())) {
                final int blockPosX = candidateBlockPos.getX();
                final int blockPosZ = candidateBlockPos.getZ();

                /* Search a good Y in given XZ position. */
                PositionYSearcher positionYSearcher = PositionYSearcher.forWorld(serverWorld);
                final Optional<Integer> blockPosY = positionYSearcher.search(chunk, blockPosX, blockPosZ);
                if (blockPosY.isEmpty()) {
                    TextHelper.sendTextByKey(context.getPlayer(), "rtp.progress.skip_no_standing_space");
                    continue;
                }
                final int $blockPosY = blockPosY.get();
                BlockPos value = new BlockPos(blockPosX, $blockPosY, blockPosZ);

                /* Filter by biome. */
                String biomeId = WorldHelper.getBiomeId(serverWorld, value);
                RandomTeleportSettings settings = context.getSettings();
                if (settings.getBiomes().getSkip().contains(biomeId)) {
                    TextHelper.sendTextByKey(context.getPlayer(), "rtp.progress.skip_biome", biomeId);
                    continue;
                }

                /* Filter by block. */
                BlockPos blockPos = new BlockPos(blockPosX, $blockPosY - 1, blockPosZ);
                BlockState blockState = chunk.getBlockState(blockPos);
                if (!PositionFilter.isSafeBlock(settings, blockState)) {
                    TextHelper.sendTextByKey(context.getPlayer(), "rtp.progress.skip_block", RegistryHelper.getIdAsString(blockState));
                    continue;
                }

                /* Filter by Y boundary. */
                if (!PositionFilter.isYInRange(settings, blockPos)) {
                    TextHelper.sendTextByKey(context.getPlayer(), "rtp.progress.skip_out_of_range_y", blockPos.getY());
                    continue;
                }

                /* Pass all the filters, set the result now. */
                context.setResult(Optional.of(value));
            }
        };
    }
}
