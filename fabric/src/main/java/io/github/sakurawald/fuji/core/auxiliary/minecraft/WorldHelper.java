package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;

public class WorldHelper {

    public static int getTopY(Chunk chunk) {
        #if MC_VER <= MC_1_21
        return chunk.getTopY();
        #elif MC_VER > MC_1_21
        return chunk.getTopYInclusive();
        #endif
    }

    public static int getTopY(World world) {
        #if MC_VER <= MC_1_21
        return world.getTopY();
        #elif MC_VER > MC_1_21
        return world.getTopYInclusive();
        #endif
    }

    public static Vec3d toBottomCenterPos(BlockPos pos) {
        return Vec3d.add(pos, 0.5, 0.0, 0.5);
    }

    public static Item toGuiItem(String dimension) {
        if (dimension.equals(DimensionTypes.OVERWORLD_ID.toString())) {
            return Items.GRASS_BLOCK;
        }

        if (dimension.equals(DimensionTypes.THE_END_ID.toString())) {
            return Items.END_STONE;
        }

        if (dimension.equals(DimensionTypes.THE_NETHER_ID.toString())) {
            return Items.NETHERRACK;
        }

        return Items.ENDER_PEARL;
    }

    public static Collection<ServerWorld> getWorlds() {
        return ServerHelper.getServer()
            .worlds
            .values();
    }

    public static Optional<ServerWorld> getWorld(@NotNull String dimensionId) {
        return getWorlds()
            .stream()
            .filter(it -> RegistryHelper.getIdAsString(it).equals(dimensionId))
            .findFirst();
    }

    public static @NotNull ServerWorld getWorldOrThrow(@NotNull String dimensionId) {
        return getWorld(dimensionId)
            .orElseThrow(() -> new IllegalStateException("Dimension %s not found.".formatted(dimensionId)));
    }

    public static
    #if  MC_VER <= MC_1_20_6
    net.minecraft.server.world.ThreadedAnvilChunkStorage
    #elif MC_VER > MC_1_20_6
    net.minecraft.server.world.ServerChunkLoadingManager
    #endif
    getChunkStorage(ServerWorld world) {
        #if MC_VER <= MC_1_20_6
        return world.getChunkManager().threadedAnvilChunkStorage;
        #elif MC_VER > MC_1_20_6
        return world.getChunkManager().chunkLoadingManager;
        #endif
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static Iterable<ChunkHolder> getChunks(ServerWorld world) {
        Iterable<ChunkHolder> chunkHolders = getChunkStorage(world).entryIterator();
        return chunkHolders;
    }
}
