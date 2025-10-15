package mod.fuji.core.auxiliary.minecraft;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mod.fuji.core.service.random_teleport.searcher.PositionYTopDownSearcher;
import mod.fuji.core.structure.GlobalPos;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
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

    public static int getBottomYInclusive(@NotNull Chunk chunk) {
        return chunk.getBottomY();
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

    public static double squareDistance(@NotNull Vec3d vec3d, double x2, double y2, double z2) {
        return squareDistance(vec3d.getX(), vec3d.getY(), vec3d.getZ(), x2, y2, z2);
    }

    public static double squareDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return dx * dx + dy * dy + dz * dz;
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

    public static boolean isVanillaDimension(@NotNull String idAsString) {
        return Set
            .of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end")
            .contains(idAsString);
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

    public static Optional<ServerWorld> getWorld(@NotNull RegistryKey<World> dimensionKey) {
        String idAsString = RegistryHelper.getIdAsString(dimensionKey);
        return getWorld(idAsString);
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


    public static @NotNull ChunkPos makeChunkPos(@NotNull BlockPos blockPos) {
        return makeChunkPos(blockPos.getX(), blockPos.getZ());
    }

    public static @NotNull ChunkPos makeChunkPos(int blockPosX, int blockPosZ) {
        return new ChunkPos(blockPosX >> 4, blockPosZ >> 4);
    }

    public static @NotNull Iterable<ChunkHolder> getChunks(@NotNull ServerWorld world) {
        var chunkLoadingManager = getChunkStorage(world);
        Iterable<ChunkHolder> iterable = chunkLoadingManager.chunkHolders.values();
        return Iterables.unmodifiableIterable(iterable);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static @NotNull List<Entity> getEntities(@NotNull ServerWorld world) {
        ArrayList<Entity> snapshot = Lists.newArrayList(world.iterateEntities());
        return snapshot;
    }

    public static int getMaxBlockY(@NotNull Chunk chunk) {
        int i = chunk.getHighestNonEmptySection();
        if (i == -1) {
            return getTopY(chunk);
        }

        // Returns the max Y in the chunk where the highest block is in.
        // NOTE: The returned Y is an upper bound, you can simply iterate it, it's cache friendly.
        int blockCoord = ChunkSectionPos.getBlockCoord(chunk.sectionIndexToCoord(i));
        return blockCoord + 15;
    }

    public static @NotNull String getBiomeId(@NotNull ServerWorld world, @NotNull BlockPos blockPos) {
        RegistryEntry<Biome> biome = world.getBiome(blockPos);
        return biome
            .getKey()
            .map(RegistryHelper::getIdAsString)
            .orElse("[UnknownBiome]");
    }

    public static @NotNull GlobalPos findSafeTopY(@NotNull World world, @NotNull BlockPos blockPos) {
        Chunk chunk = world.getChunk(blockPos);
        int resultY = new PositionYTopDownSearcher()
            .search(chunk, blockPos.getX(), blockPos.getZ())
            .orElseGet(blockPos::getY);

        return GlobalPos
            .of(world, blockPos)
            .withY(resultY);
    }

    public static class SpawnPos {

        public static @NotNull GlobalPos getServerSpawnPos() {
            @NotNull ServerWorld overworld = ServerHelper.getServer().getOverworld();
            @NotNull BlockPos spawnPos;

            #if MC_VER < MC_1_21_9
            spawnPos = overworld.getSpawnPos();
            #elif MC_VER >= MC_1_21_9
            spawnPos = overworld.getSpawnPoint().getPos();
            #endif

            return GlobalPos.of(overworld, spawnPos);
        }

        public static @NotNull GlobalPos getSafeServerSpawnPos() {
            /* Get server spawn pos from properties file. */
            GlobalPos serverSpawnPos = getServerSpawnPos();

            /* Adjust the alignment. */
            GlobalPos safeSpawnPos = new GlobalPos(serverSpawnPos.getLevel(), serverSpawnPos.getX() + 0.5, serverSpawnPos.getY() + 0.5, serverSpawnPos.getZ() + 0.5, 0, 0);

            /* Find the top Y. */
            @NotNull ServerWorld serverWorld = getWorldOrThrow(safeSpawnPos.getLevel());
            @NotNull BlockPos blockPos = safeSpawnPos.toBlockPos();
            safeSpawnPos = findSafeTopY(serverWorld, blockPos);
            return safeSpawnPos;
        }

        public static Optional<GlobalPos> getPlayerSpawnPos(@NotNull ServerPlayerEntity player) {
            #if MC_VER < MC_1_21_5
            return Optional
                .ofNullable(player.getSpawnPointPosition())
                .map(spawnBlockPos -> {
                    @NotNull RegistryKey<World> dimension = player.getSpawnPointDimension();
                    return Optional.of(GlobalPos.of(dimension, spawnBlockPos));
                })
                .orElse(Optional.empty());
            #elif MC_VER >= MC_1_21_5 && MC_VER < MC_1_21_9
            return Optional
                .ofNullable(player.getRespawn())
                .map(respawn -> {
                    RegistryKey<World> dimension = respawn.comp_3683();
                    BlockPos blockPos = respawn.comp_3684();
                    return Optional.of(GlobalPos.of(dimension,blockPos));
                })
                .orElse(Optional.empty());
            #elif MC_VER >= MC_1_21_9
            return Optional
                .ofNullable(player.getRespawn())
                .map(respawn -> {
                    net.minecraft.world.WorldProperties.SpawnPoint spawnPoint = respawn.comp_4913();
                    RegistryKey<World> dimension = spawnPoint.getDimension();
                    BlockPos blockPos = spawnPoint.getPos();
                    return Optional.of(GlobalPos.of(dimension, blockPos));
                })
                .orElse(Optional.empty());
            #endif
        }
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

            ServerWorld serverWorld = PlayerHelper.getServerWorld(player);
            BlockHitResult blockHitResult = serverWorld.raycast(new RaycastContext(
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
