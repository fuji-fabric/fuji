package io.github.sakurawald.fuji.module.initializer.world.service;

import com.google.common.collect.ImmutableList;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.event.impl.ServerTickEvents;
import io.github.sakurawald.fuji.core.event.impl.ServerWorldEvents;
import io.github.sakurawald.fuji.core.extension.SimpleRegistryExtension;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.core.structure.TeleportTicket;
import io.github.sakurawald.fuji.module.initializer.world.accessor.IDimensionOptions;
import io.github.sakurawald.fuji.module.initializer.world.structure.DimensionNode;
import io.github.sakurawald.fuji.module.initializer.world.structure.MyServerWorld;
import io.github.sakurawald.fuji.module.initializer.world.structure.MyWorldProperties;
import io.github.sakurawald.fuji.module.initializer.world.structure.VoidWorldGenerationProgressListener;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    private static void createDimension(@NotNull DimensionNode dimensionNode) {
        MinecraftServer server = ServerHelper.getServer();
        Identifier dimension = RegistryHelper.makeIdentifier(dimensionNode.dimension);
        Identifier dimensionType = RegistryHelper.makeIdentifier(dimensionNode.dimension_type);
        long seed = dimensionNode.seed;
        registerWorld(server, dimension, dimensionType, seed);
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

        RegistryKey<World> dimensionKey = world.getRegistryKey();
        if (server.worlds.remove(dimensionKey, world)) {
            // fire unload event
            ServerWorldEvents.UNLOAD.invoker().fire(server, world);

            // remove the entry from registry
            SimpleRegistry<DimensionOptions> dimensionsRegistry = (SimpleRegistry<DimensionOptions>) RegistryHelper.ofRegistry(RegistryKeys.DIMENSION);
            SimpleRegistryExtension.remove(dimensionsRegistry, dimensionKey.getValue());

            // delete files
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

    private static @Nullable DimensionOptions getDimensionOptionsAsTemplate(@NotNull Registry<DimensionOptions> registry, Identifier dimensionTypeIdentifier) {
        return registry.get(dimensionTypeIdentifier);
    }

    /**
     * To avoid share the same reference of the vanilla minecraft default `DimensionOptions` instance,
     * we must create new instance.
     */
    private static @NotNull DimensionOptions makeDimensionOptions(DimensionOptions template) {
        return new DimensionOptions(template.dimensionTypeEntry(), template.chunkGenerator());
    }

    @SuppressWarnings("deprecation")
    private static void registerWorld(@NotNull MinecraftServer server, Identifier dimensionIdentifier, @NotNull Identifier dimenstionTypeIdentifier, long seed) {
        /* create the world */
        // note: we use the same WorldData from OVERWORLD
        MyWorldProperties worldProperties = new MyWorldProperties(server.getSaveProperties(), seed);

        RegistryKey<World> worldRegistryKey = RegistryKey.of(RegistryKeys.WORLD, dimensionIdentifier);
        LogUtil.debug("Make instance of world with registry key of type `World`: {}", worldRegistryKey);

        Registry<DimensionOptions> registry = RegistryHelper.ofRegistry(RegistryKeys.DIMENSION);
        @Nullable DimensionOptions template = getDimensionOptionsAsTemplate(registry, dimenstionTypeIdentifier);
        if (template == null) {
            LogUtil.error("Can't use {} dimension-type as the template to create extra fuji worlds.", dimenstionTypeIdentifier);
            return;
        }

        DimensionOptions dimensionOptions = makeDimensionOptions(template);
        ((IDimensionOptions) (Object) dimensionOptions).fuji$setSaveProperties(false);

        ServerWorld world;
        try {
            world = new MyServerWorld(server,
                Util.getMainWorkerExecutor(),
                server.session,
                worldProperties,
                worldRegistryKey,
                dimensionOptions,
                VoidWorldGenerationProgressListener.INSTANCE,
                false,
                BiomeAccess.hashSeed(seed),
                ImmutableList.of(),
                true,
                null);
        } catch (Exception e) {
            LogUtil.error("Failed to make ServerWorld instance: dimensionId = {}, dimensionTypeId = {}", dimensionIdentifier, dimenstionTypeIdentifier, e);
            return;
        }

        // start dragon fight if the dimension type is the end.
        if (dimenstionTypeIdentifier.equals(DimensionTypes.THE_END_ID)) {
            world.setEnderDragonFight(new EnderDragonFight(world, world.getSeed(), EnderDragonFight.Data.DEFAULT));
        }

        /* register the world */
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

        server.worlds.put(world.getRegistryKey(), world);
        ServerWorldEvents.LOAD.invoker().fire(server, world);

        world.tick(() -> true);
    }
}
