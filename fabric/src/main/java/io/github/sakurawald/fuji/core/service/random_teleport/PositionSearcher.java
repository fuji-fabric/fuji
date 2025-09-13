package io.github.sakurawald.fuji.core.service.random_teleport;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import io.github.sakurawald.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import java.util.Optional;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

public class PositionSearcher {

    @ForDeveloper("Search once, and return empty if there is no good result.")
    public static void search(@NotNull LocationSearchContext context) {
        final RandomTeleportSettings settings = context.getSettings();
        final BlockPos blockPosInChunk = PositionXZGenerator.getRandomXZ(settings);

        final ServerWorld serverWorld = WorldHelper.getWorldOrThrow(settings.getDimension());
        final Chunk chunk = serverWorld.getChunk(blockPosInChunk);

        LogUtil.debug("Select the RTP candidate chunk: chunk pos = {}", chunk.getPos());

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

            /* Filter: biome */
            String biomeId = WorldHelper.getBiomeId(serverWorld, value);
            LogUtil.debug("RTP: Biome at block pos {} is {}", value, biomeId);
            if (settings.getBiomes().getSkip().contains(biomeId)) {
                LogUtil.debug("RTP: Skip the biome {} at block pos {}", biomeId, value);
                return;
            }

            /* Filter: safe location */
            if (BlockPosFilter.isSatisfied(settings, chunk, new BlockPos(blockPosX, $blockPosY - 2, blockPosZ))) {
                context.setResult(Optional.of(value));
                return;
            }
        }
    }
}
