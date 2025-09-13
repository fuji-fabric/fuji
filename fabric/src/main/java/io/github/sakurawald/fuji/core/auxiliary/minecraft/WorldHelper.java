package io.github.sakurawald.fuji.core.auxiliary.minecraft;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldHelper {

    public static int getTopY(@NotNull Chunk chunk) {
        #if MC_VER <= MC_1_21
        return chunk.getTopY();
        #elif MC_VER > MC_1_21
        return chunk.getTopYInclusive();
        #endif
    }

    public static int getTopY(@NotNull World world) {
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

    public static Optional<ServerWorld> getWorld(@Nullable String dimensionId) {
        return getWorlds()
            .stream()
            .filter(it -> RegistryHelper.getIdAsString(it).equals(dimensionId))
            .findFirst();
    }

    public static @NotNull ServerWorld getWorldOrThrow(@NotNull String dimensionId) {
        return getWorld(dimensionId)
            .orElseThrow(() -> new IllegalStateException("Dimension %s not found.".formatted(dimensionId)));
    }

    public static boolean isServerWorld(@Nullable World world)  {
        if (world == null) {
            return false;
        }
        return world instanceof ServerWorld;
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

    public static int getMaxBlockY(@NotNull Chunk chunk) {
        int i = chunk.getHighestNonEmptySection();
        if (i == -1) {
            return getTopY(chunk);
        }

        // Returns the max Y in the chunk where the highest block is in.
        int blockCoord = ChunkSectionPos.getBlockCoord(chunk.sectionIndexToCoord(i));
        return blockCoord + 15;
    }

    public static class Formatter {

        public static @NotNull String format(@NotNull BlockPos blockPos) {
            return "%d %d %d".formatted(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
    }

    public static class Raycast {

        private static final double PLAYER_INTERACTION_DISTANCE = 5.0;

        public static Optional<BlockPos> getLookingAtBlock(@NotNull ServerPlayerEntity player) {
            return getLookingAtBlock(player, PLAYER_INTERACTION_DISTANCE);
        }

        public static Optional<BlockPos> getLookingAtBlock(@NotNull ServerPlayerEntity player, double maxDistance) {
            Vec3d eyePos = player.getCameraPosVec(1.0F);
            Vec3d lookVec = player.getRotationVec(1.0F);
            Vec3d reachVec = eyePos.add(lookVec.multiply(maxDistance));

            BlockHitResult blockHitResult = player.getWorld().raycast(new RaycastContext(
                eyePos,
                reachVec,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
            ));

            if (blockHitResult.getType() == HitResult.Type.BLOCK) {
                return Optional.ofNullable(blockHitResult.getBlockPos());
            }

            return Optional.empty();
        }

        public static Optional<Entity> getLookingAtEntity(@NotNull ServerPlayerEntity player) {
            return getLookingAtEntity(player, PLAYER_INTERACTION_DISTANCE);
        }

        public static Optional<Entity> getLookingAtEntity(@NotNull ServerPlayerEntity player, double maxDistance) {
            Vec3d start = player.getCameraPosVec(1.0F);
            Vec3d direction = player.getRotationVec(1.0F);
            Vec3d end = start.add(direction.multiply(maxDistance));

            Box box = player.getBoundingBox().stretch(direction.multiply(maxDistance)).expand(1.0D, 1.0D, 1.0D);
            EntityHitResult entityHitResult = ProjectileUtil.raycast(
                player,
                start,
                end,
                box,
                entity -> !entity.isSpectator() && entity.isAttackable() && entity != player,
                maxDistance * maxDistance
            );

            if (entityHitResult != null) {
                return Optional.ofNullable(entityHitResult.getEntity());
            }

            return Optional.empty();

        }
    }

}
