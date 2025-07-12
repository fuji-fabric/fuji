package io.github.sakurawald.fuji.module.initializer.world.service;

import com.google.common.collect.ImmutableList;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.event.impl.ServerTickEvents;
import io.github.sakurawald.fuji.core.event.impl.ServerWorldEvents;
import io.github.sakurawald.fuji.core.extension.SimpleRegistryExtension;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.core.structure.TeleportTicket;
import io.github.sakurawald.fuji.module.initializer.world.WorldInitializer;
import io.github.sakurawald.fuji.module.initializer.world.accessor.IDimensionOptions;
import io.github.sakurawald.fuji.module.initializer.world.structure.DimensionNode;
import io.github.sakurawald.fuji.module.initializer.world.structure.RuntimeWorld;
import io.github.sakurawald.fuji.module.initializer.world.structure.RuntimeWorldProperties;
import io.github.sakurawald.fuji.module.initializer.world.structure.VoidWorldGenerationProgressListener;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;

#if MC_VER <= MC_1_20_4
import com.mojang.serialization.Lifecycle;
#elif MC_VER > MC_1_20_4
import net.minecraft.registry.entry.RegistryEntryInfo;
#endif

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldService {

    private static final Set<ServerWorld> dimensionDeletionQueue = new ReferenceOpenHashSet<>();
    private static final Set<DimensionNode> dimensionCreationQueue = new ReferenceOpenHashSet<>();

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> processDimensionCreationAndDeletionQueue());
    }

    private static void processDimensionCreationAndDeletionQueue() {
        dimensionDeletionQueue.removeIf(WorldService::tryDeleteDimension);
        dimensionCreationQueue.removeIf(WorldService::tryCreateDimension);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void requestToDeleteDimension(@NotNull ServerWorld world) {
        ServerHelper.getServer().submit(() -> {
            dimensionDeletionQueue.add(world);
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void requestToCreateDimension(DimensionNode dimensionNode) {
        ServerHelper.getServer().submit(() -> {
            dimensionCreationQueue.add(dimensionNode);
        });
    }

    private static boolean tryDeleteDimension(@NotNull ServerWorld world) {
        if (world.getPlayers().isEmpty()) {
            deleteDimension(world);
            return true;
        } else {
            evacuatePlayers(world);
            return false;
        }
    }


    private static boolean tryCreateDimension(@NotNull DimensionNode dimensionNode) {
        // NOTE: Wait the target dimension to be deleted first (If there is a deletion ticket for it).
        if (dimensionDeletionQueue.stream().anyMatch(it -> RegistryHelper.toString(it).equals(dimensionNode.dimension))) {
            return false;
        }

        createDimension(dimensionNode);
        return true;
    }

    @SuppressWarnings("deprecation")
    private static void createDimension(@NotNull DimensionNode dimensionNode) {
        MinecraftServer server = ServerHelper.getServer();
        Identifier dimensionIdentifier = RegistryHelper.makeIdentifier(dimensionNode.dimension);
        Identifier dimensionTypeIdentifier = RegistryHelper.makeIdentifier(dimensionNode.dimension_type);

        /* Make the dimension properties. */
        // note: we use the same WorldData from OVERWORLD
        RuntimeWorldProperties worldProperties = new RuntimeWorldProperties(server.getSaveProperties(), dimensionNode);

        /* Make the dimension options. */
        @Nullable DimensionOptions dimensionOptions = makeDimensionOptions(dimensionTypeIdentifier);
        if (dimensionOptions == null) {
            LogUtil.error("Can't use {} dimension-type as the template to create extra fuji worlds.", dimensionTypeIdentifier);
            return;
        }
        ((IDimensionOptions) (Object) dimensionOptions).fuji$setSaveProperties(false);

        /* Make the dimension instance. */
        RegistryKey<World> worldRegistryKey = RegistryKey.of(RegistryKeys.WORLD, dimensionIdentifier);
        LogUtil.debug("Make instance of world with registry key of type `World`: {}", worldRegistryKey);
        ServerWorld dimension;
        try {
            dimension = new RuntimeWorld(server,
                Util.getMainWorkerExecutor(),
                server.session,
                worldProperties,
                worldRegistryKey,
                dimensionOptions,
                VoidWorldGenerationProgressListener.INSTANCE,
                false,
                BiomeAccess.hashSeed(dimensionNode.seed),
                ImmutableList.of(),
                dimensionNode.shouldTickTime,
                null);
        } catch (Exception e) {
            LogUtil.error("Failed to make ServerWorld instance: dimensionId = {}, dimensionTypeId = {}", dimensionIdentifier, dimensionTypeIdentifier, e);
            return;
        }

        // start dragon fight if the dimension type is the end.
        if (dimensionTypeIdentifier.equals(DimensionTypes.THE_END_ID)) {
            dimension.setEnderDragonFight(new EnderDragonFight(dimension, dimension.getSeed(), EnderDragonFight.Data.DEFAULT));
        }

        /* Register the dimension. */
        SimpleRegistry<DimensionOptions> dimensionOptionsRegistry = (SimpleRegistry<DimensionOptions>) RegistryHelper.ofRegistry(RegistryKeys.DIMENSION);
        boolean original = ((SimpleRegistryExtension<?>) dimensionOptionsRegistry).fuji$isFrozen();
        ((SimpleRegistryExtension<?>) dimensionOptionsRegistry).fuji$setFrozen(false);

        RegistryKey<DimensionOptions> dimensionOptionsRegistryKey = RegistryKeys.toDimensionKey(worldRegistryKey);

        if (!dimensionOptionsRegistry.contains(dimensionOptionsRegistryKey)) {
            LogUtil.debug("Add entry for dimension options registry: key = {}, value = {}", dimensionOptionsRegistryKey, dimensionOptions);
            #if MC_VER <= MC_1_20_4
            dimensionOptionsRegistry.add(dimensionOptionsRegistryKey, dimensionOptions, Lifecycle.stable());
            #elif MC_VER > MC_1_20_4
            dimensionOptionsRegistry.add(dimensionOptionsRegistryKey, dimensionOptions, RegistryEntryInfo.DEFAULT);
            #endif
        }
        ((SimpleRegistryExtension<?>) dimensionOptionsRegistry).fuji$setFrozen(original);

        server.worlds.put(dimension.getRegistryKey(), dimension);
        ServerWorldEvents.LOAD.invoker().fire(server, dimension);

        /* Start ticking it. */
        dimension.tick(() -> true);
    }

    private static void evacuatePlayers(@NotNull ServerWorld dimension) {
        ServerWorld safeDimension = dimension.getServer().getOverworld();
        BlockPos safeBlockPos = safeDimension.getSpawnPos();

        List<ServerPlayerEntity> players = new ArrayList<>(dimension.getPlayers());
        for (ServerPlayerEntity player : players) {
            GlobalPos from = GlobalPos.of(player);
            GlobalPos to = new GlobalPos(safeDimension, safeBlockPos.getX() + 0.5, safeBlockPos.getY() + 0.5, safeBlockPos.getZ() + 0.5, 0, 0);
            TeleportTicket teleportTicket = TeleportTicket.makeVipTicket(player, from, to);

            Managers.getBossBarManager().addTicket(teleportTicket);
        }
    }

    private static void deleteDimension(@NotNull ServerWorld world) {
        MinecraftServer server = world.getServer();

        // FIXME: Use the vanilla function to handle dimension shutdown.
        RegistryKey<World> dimensionKey = world.getRegistryKey();
        if (server.worlds.remove(dimensionKey, world)) {
            /* Fire an unload event */
            ServerWorldEvents.UNLOAD.invoker().fire(server, world);

            /* remove the entry from registry */
            SimpleRegistry<DimensionOptions> dimensionsRegistry = (SimpleRegistry<DimensionOptions>) RegistryHelper.ofRegistry(RegistryKeys.DIMENSION);
            SimpleRegistryExtension.remove(dimensionsRegistry, dimensionKey.getValue());

            /* Delete world files. */
            File worldDirectory = server.session.getWorldDirectory(dimensionKey).toFile();
            deleteFiles(worldDirectory);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void deleteFiles(@NotNull File file) {
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return;
            for (File child : files) {
                if (child.isDirectory()) {
                    deleteFiles(child);
                } else {
                    child.delete();
                }
            }
        }
    }

    private static @Nullable DimensionOptions makeDimensionOptions(Identifier dimensionTypeIdentifier) {
        /* Get an existing dimension options from registry. */
        Registry<DimensionOptions> registry = RegistryHelper.ofRegistry(RegistryKeys.DIMENSION);
        @Nullable DimensionOptions originalDimensionOptions = registry.get(dimensionTypeIdentifier);
        if (originalDimensionOptions == null) {
            return null;
        }

        // NOTE: Clone a DimensionOptions instance from existing one.
        return new DimensionOptions(originalDimensionOptions.dimensionTypeEntry(), originalDimensionOptions.chunkGenerator());
    }

    public static boolean existsDimension(Identifier dimensionId) {
        // Check the existence of dimensions using the runtime worlds variable.
        return ServerHelper
            .getWorlds()
            .stream()
            .anyMatch(it -> RegistryHelper.toString(it).equals(dimensionId.toString()));
    }

    public static void deleteDimensionNode(ServerCommandSource source, String dimensionId) {
        Optional<DimensionNode> first = WorldInitializer.storage.model().dimension_list.stream().filter(o -> o.getDimension().equals(dimensionId)).findFirst();
        first.ifPresent(dimensionNode -> {
            WorldInitializer.storage.model().dimension_list.remove(dimensionNode);
            WorldInitializer.storage.writeStorage();
        });
    }

    public static void saveRuntimeWorldConfigs() {
        WorldInitializer.config.writeStorage();
    }

    public static void syncWorldBorder() {
        ServerHelper
            .getOnlinePlayers()
            .forEach(WorldService::syncWorldBorder);

    }

    public static void syncWorldBorder(ServerPlayerEntity player) {
        ServerWorld world = PlayerHelper.getServerWorld(player);
        WorldBorder worldBorder = world.getWorldBorder();

        LogUtil.debug("Sync world border: player = {}, world = {}, size = {}", PlayerHelper.getPlayerName(player), RegistryHelper.toString(world), worldBorder.getSize());
        player.networkHandler.sendPacket(new WorldBorderCenterChangedS2CPacket(worldBorder));
        player.networkHandler.sendPacket(new WorldBorderSizeChangedS2CPacket(worldBorder));
        player.networkHandler.sendPacket(new WorldBorderWarningBlocksChangedS2CPacket(worldBorder));
        player.networkHandler.sendPacket(new WorldBorderWarningTimeChangedS2CPacket(worldBorder));
//        player.networkHandler.sendPacket(new WorldBorderInterpolateSizeS2CPacket(worldBorder));
    }

    public static Optional<DimensionNode> getDimensionNode(String dimensionId) {
        return WorldInitializer.storage.model()
            .dimension_list
            .stream()
            .filter(it -> it.dimension.equalsIgnoreCase(dimensionId))
            .findFirst();
    }


}


