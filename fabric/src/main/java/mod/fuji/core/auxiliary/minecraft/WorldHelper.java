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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldHelper {

    public static int getMaxY(@NotNull ChunkAccess chunk) {
        #if MC_VER <= MC_1_21
        return chunk.getMaxBuildHeight();
        #elif MC_VER > MC_1_21
        return chunk.getMaxY();
        #endif
    }

    public static int getMinY(@NotNull ChunkAccess chunk) {
        #if MC_VER <= MC_1_21
        return chunk.getMinBuildHeight();
        #elif MC_VER > MC_1_21
        return chunk.getMinY();
        #endif
    }

    public static int getMaxY(@NotNull Level world) {
        #if MC_VER <= MC_1_21
        return world.getMaxBuildHeight();
        #elif MC_VER > MC_1_21
        return world.getMaxY();
        #endif
    }

    public static int getMinY(@NotNull Level world) {
        #if MC_VER <= MC_1_21
        return world.getMinBuildHeight();
        #elif MC_VER > MC_1_21
        return world.getMinY();
        #endif
    }

    public static Vec3 toBottomCenterPos(BlockPos pos) {
        return Vec3.atLowerCornerWithOffset(pos, 0.5, 0.0, 0.5);
    }

    public static double squareDistance(@NotNull Vec3 vec3d, double x2, double y2, double z2) {
        return squareDistance(vec3d.x(), vec3d.y(), vec3d.z(), x2, y2, z2);
    }

    public static double squareDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return dx * dx + dy * dy + dz * dz;
    }

    public static Item toGuiItem(String dimension) {
        if (dimension.equals(BuiltinDimensionTypes.OVERWORLD_EFFECTS.toString())) {
            return Items.GRASS_BLOCK;
        }

        if (dimension.equals(BuiltinDimensionTypes.END_EFFECTS.toString())) {
            return Items.END_STONE;
        }

        if (dimension.equals(BuiltinDimensionTypes.NETHER_EFFECTS.toString())) {
            return Items.NETHERRACK;
        }

        return Items.ENDER_PEARL;
    }

    public static boolean isVanillaDimension(@NotNull String idAsString) {
        return Set
            .of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end")
            .contains(idAsString);
    }

    public static Collection<ServerLevel> getWorlds() {
        return ServerHelper.getServer()
            .levels
            .values();
    }

    public static Optional<ServerLevel> getWorld(@Nullable String dimensionId) {
        return getWorlds()
            .stream()
            .filter(it -> RegistryHelper.getIdAsString(it).equals(dimensionId))
            .findFirst();
    }

    public static Optional<ServerLevel> getWorld(@NotNull ResourceKey<Level> dimensionKey) {
        String idAsString = RegistryHelper.getIdAsString(dimensionKey);
        return getWorld(idAsString);
    }

    public static @NotNull ServerLevel getWorldOrThrow(@NotNull String dimensionId) {
        return getWorld(dimensionId)
            .orElseThrow(() -> new IllegalStateException("Dimension %s not found.".formatted(dimensionId)));
    }

    public static boolean isServerWorld(@Nullable Level world)  {
        if (world == null) {
            return false;
        }
        return world instanceof ServerLevel;
    }

    public static
    #if  MC_VER <= MC_1_20_6
    net.minecraft.server.world.ThreadedAnvilChunkStorage
    #elif MC_VER > MC_1_20_6
    net.minecraft.server.level.ChunkMap
    #endif
    getChunkStorage(ServerLevel world) {
        #if MC_VER <= MC_1_20_6
        return world.getChunkManager().threadedAnvilChunkStorage;
        #elif MC_VER > MC_1_20_6
        return world.getChunkSource().chunkMap;
        #endif
    }


    public static @NotNull ChunkPos makeChunkPos(@NotNull BlockPos blockPos) {
        return makeChunkPos(blockPos.getX(), blockPos.getZ());
    }

    public static @NotNull ChunkPos makeChunkPos(int blockPosX, int blockPosZ) {
        return new ChunkPos(blockPosX >> 4, blockPosZ >> 4);
    }

    public static @NotNull Iterable<ChunkHolder> getChunks(@NotNull ServerLevel world) {
        var chunkLoadingManager = getChunkStorage(world);
        Iterable<ChunkHolder> iterable = chunkLoadingManager.visibleChunkMap.values();
        return Iterables.unmodifiableIterable(iterable);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static @NotNull List<Entity> getEntities(@NotNull ServerLevel world) {
        ArrayList<Entity> snapshot = Lists.newArrayList(world.getAllEntities());
        return snapshot;
    }

    public static int getMaxBlockY(@NotNull ChunkAccess chunk) {
        int i = chunk.getHighestFilledSectionIndex();
        if (i == -1) {
            return getMaxY(chunk);
        }

        // Returns the max Y in the chunk where the highest block is in.
        // NOTE: The returned Y is an upper bound, you can simply iterate it, it's cache friendly.
        int blockCoord = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i));
        return blockCoord + 15;
    }

    public static @NotNull String getBiomeId(@NotNull ServerLevel world, @NotNull BlockPos blockPos) {
        Holder<Biome> biome = world.getBiome(blockPos);
        return biome
            .unwrapKey()
            .map(RegistryHelper::getIdAsString)
            .orElse("[UnknownBiome]");
    }

    public static @NotNull GlobalPos findSafeTopY(@NotNull Level world, @NotNull BlockPos blockPos) {
        ChunkAccess chunk = world.getChunk(blockPos);
        int resultY = new PositionYTopDownSearcher()
            .search(chunk, blockPos.getX(), blockPos.getZ())
            .orElseGet(blockPos::getY);

        return GlobalPos
            .of(world, blockPos)
            .withY(resultY);
    }

    public static class SpawnPos {

        public static @NotNull GlobalPos getServerSpawnPos() {
            @NotNull ServerLevel overworld = ServerHelper.getServer().overworld();
            @NotNull BlockPos spawnPos;

            #if MC_VER < MC_1_21_9
            spawnPos = overworld.getSharedSpawnPos();
            #elif MC_VER >= MC_1_21_9
            spawnPos = overworld.getRespawnData().pos();
            #endif

            return GlobalPos.of(overworld, spawnPos);
        }

        public static @NotNull GlobalPos getSafeServerSpawnPos() {
            /* Get server spawn pos from properties file. */
            GlobalPos serverSpawnPos = getServerSpawnPos();

            /* Adjust the alignment. */
            GlobalPos safeSpawnPos = new GlobalPos(serverSpawnPos.getLevel(), serverSpawnPos.getX() + 0.5, serverSpawnPos.getY() + 0.5, serverSpawnPos.getZ() + 0.5, 0, 0);

            /* Find the top Y. */
            @NotNull ServerLevel serverWorld = getWorldOrThrow(safeSpawnPos.getLevel());
            @NotNull BlockPos blockPos = safeSpawnPos.toBlockPos();
            safeSpawnPos = findSafeTopY(serverWorld, blockPos);
            return safeSpawnPos;
        }

        public static Optional<GlobalPos> getPlayerSpawnPos(@NotNull ServerPlayer player) {
            #if MC_VER < MC_1_21_5
            return Optional
                .ofNullable(player.getRespawnPosition())
                .map(spawnBlockPos -> {
                    @NotNull ResourceKey<Level> dimension = player.getRespawnDimension();
                    return Optional.of(GlobalPos.of(dimension, spawnBlockPos));
                })
                .orElse(Optional.empty());
            #elif MC_VER >= MC_1_21_5 && MC_VER < MC_1_21_9
            return Optional
                .ofNullable(player.getRespawnConfig())
                .map(respawn -> {
                    ResourceKey<Level> dimension = respawn.dimension();
                    BlockPos blockPos = respawn.pos();
                    return Optional.of(GlobalPos.of(dimension,blockPos));
                })
                .orElse(Optional.empty());
            #elif MC_VER >= MC_1_21_9
            return Optional
                .ofNullable(player.getRespawnConfig())
                .map(respawn -> {
                    net.minecraft.world.level.storage.LevelData.RespawnData spawnPoint = respawn.respawnData();
                    ResourceKey<Level> dimension = spawnPoint.dimension();
                    BlockPos blockPos = spawnPoint.pos();
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

        public static Optional<BlockPos> getLookingAtBlock(@NotNull ServerPlayer player) {
            return getLookingAtBlock(player, PLAYER_INTERACTION_DISTANCE);
        }

        public static Optional<BlockPos> getLookingAtBlock(@NotNull ServerPlayer player, double maxDistance) {
            Vec3 eyePos = player.getEyePosition(1.0F);
            Vec3 lookVec = player.getViewVector(1.0F);
            Vec3 reachVec = eyePos.add(lookVec.scale(maxDistance));

            ServerLevel serverWorld = PlayerHelper.getServerWorld(player);
            BlockHitResult blockHitResult = serverWorld.clip(new ClipContext(
                eyePos,
                reachVec,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
            ));

            if (blockHitResult.getType() == HitResult.Type.BLOCK) {
                return Optional.ofNullable(blockHitResult.getBlockPos());
            }

            return Optional.empty();
        }

        public static Optional<Entity> getLookingAtEntity(@NotNull ServerPlayer player) {
            return getLookingAtEntity(player, PLAYER_INTERACTION_DISTANCE);
        }

        public static Optional<Entity> getLookingAtEntity(@NotNull ServerPlayer player, double maxDistance) {
            Vec3 start = player.getEyePosition(1.0F);
            Vec3 direction = player.getViewVector(1.0F);
            Vec3 end = start.add(direction.scale(maxDistance));

            AABB box = player.getBoundingBox().expandTowards(direction.scale(maxDistance)).inflate(1.0D, 1.0D, 1.0D);
            EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
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
