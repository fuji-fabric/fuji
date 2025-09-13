package io.github.sakurawald.fuji.core.service.random_teleport.structure;

import java.util.Optional;
import net.minecraft.world.chunk.Chunk;

@FunctionalInterface
public interface HeightFinder {
    Optional<Integer> getY(Chunk chunk, int x, int z);
}

