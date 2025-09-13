package io.github.sakurawald.fuji.core.service.random_teleport;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.service.random_teleport.structure.HeightFinder;
import io.github.sakurawald.fuji.core.service.random_teleport.structure.HeightFindingStrategy;
import io.github.sakurawald.fuji.core.service.random_teleport.structure.RandomTeleportSettings;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

public class LocationSearcher {

    static @NotNull Optional<BlockPos> searchPosition(@NotNull RandomTeleportSettings settings) {
        final BlockPos targetXZ = LocationXZGenerator.getRandomXZ(settings);

        final ServerWorld serverWorld = WorldHelper.getWorldOrThrow(settings.getDimension());
        final Chunk chunk = serverWorld.getChunk(targetXZ);

        for (BlockPos.Mutable candidateBlock : ChunkCandidateBlocksGenerator.getChunkCandidateBlocks(chunk.getPos())) {
            final int x = candidateBlock.getX();
            final int z = candidateBlock.getZ();

            HeightFinder heightFinder = HeightFindingStrategy.forWorld(serverWorld);
            final OptionalInt yOpt = heightFinder.getY(chunk, x, z);
            if (yOpt.isEmpty()) {
                continue;
            }
            final int y = yOpt.getAsInt();

            if (BlockPosFilter.isSatisfied(settings, chunk, new BlockPos(x, y - 2, z))) {
                return Optional.of(new BlockPos(x, y, z));
            }
        }

        return Optional.empty();
    }
}
