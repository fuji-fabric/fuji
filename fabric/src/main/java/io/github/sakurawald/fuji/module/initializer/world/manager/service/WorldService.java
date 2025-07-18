package io.github.sakurawald.fuji.module.initializer.world.manager.service;

import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.event.impl.ServerTickEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.core.structure.Pair;
import io.github.sakurawald.fuji.core.structure.TeleportTicket;
import io.github.sakurawald.fuji.module.initializer.world.manager.WorldInitializer;
import io.github.sakurawald.fuji.module.initializer.world.manager.structure.RuntimeDimensionDescriptor;
import io.github.sakurawald.fuji.module.initializer.world.manager.structure.RuntimeDimensionLoader;
import io.github.sakurawald.fuji.module.initializer.world.manager.structure.RuntimeDimensionMaker;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import org.jetbrains.annotations.NotNull;

public class WorldService {

    private static final Set<ServerWorld> dimensionDeletionQueue = new ReferenceOpenHashSet<>();
    private static final Set<RuntimeDimensionDescriptor> dimensionCreationQueue = new ReferenceOpenHashSet<>();

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> processDimensionCreationAndDeletionQueue());
    }

    private static void processDimensionCreationAndDeletionQueue() {
        dimensionDeletionQueue.removeIf(WorldService::tryUnloadAndDeleteDimension);
        dimensionCreationQueue.removeIf(WorldService::tryCreateAndLoadDimension);
    }

    public static void requestToUnloadAndDeleteDimension(@NotNull ServerWorld world) {
        ServerHelper.getServer().submit(() -> {
            dimensionDeletionQueue.add(world);
        });
    }

    public static void requestToCreateAndLoadDimension(RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        ServerHelper.getServer().submit(() -> {
            dimensionCreationQueue.add(runtimeDimensionDescriptor);
        });
    }

    private static boolean tryUnloadAndDeleteDimension(@NotNull ServerWorld world) {
        if (world.getPlayers().isEmpty()) {
            unloadAndDeleteDimension(world);
            return true;
        } else {
            evacuatePlayers(world);
            return false;
        }
    }

    private static boolean tryCreateAndLoadDimension(@NotNull RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        // NOTE: Wait the target dimension to be deleted first (If there is a deletion ticket for it).
        if (dimensionDeletionQueue.stream().anyMatch(it -> RegistryHelper.toString(it).equals(runtimeDimensionDescriptor.dimension))) {
            return false;
        }

        createAndLoadDimension(runtimeDimensionDescriptor);
        // NOTE: Returns true anyway, to prevent the console spam.
        return true;
    }

    private static void createAndLoadDimension(RuntimeDimensionDescriptor runtimeDimensionDescriptor) {
        try {
            /* Make the runtime dimension. */
            Pair<ServerWorld, DimensionOptions> result = RuntimeDimensionMaker.makeRuntimeDimension(runtimeDimensionDescriptor);
            ServerWorld dimension = result.getKey();
            DimensionOptions dimensionOptions = result.getValue();

            /* Load the runtime dimension. */
            RuntimeDimensionLoader.loadRuntimeDimension(dimension, dimensionOptions);

            /* Start ticking it. */
            dimension.tick(() -> true);
        } catch (Exception e) {
            LogUtil.error("Failed to make RuntimeDimension instance: dimension descriptor = {}", runtimeDimensionDescriptor, e);
        }
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

    private static void unloadAndDeleteDimension(@NotNull ServerWorld world) {
        RuntimeDimensionLoader.unloadDimension(world);
        deleteDimensionFiles(world);
    }

    private static void deleteDimensionFiles(@NotNull ServerWorld world) {
        MinecraftServer server = world.getServer();
        RegistryKey<World> dimensionKey = world.getRegistryKey();

        /* Delete world files. */
        File worldDirectory = server.session.getWorldDirectory(dimensionKey).toFile();
        deleteFiles(worldDirectory);
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

    public static boolean existsDimension(Identifier dimensionId) {
        boolean dimensionExistedInRuntime = ServerHelper
            .getWorlds()
            .stream()
            .anyMatch(it -> RegistryHelper.toString(it).equals(dimensionId.toString()));
        boolean dimensionExistedInConfig = getRuntimeDimensionDescriptor(dimensionId.toString()).isPresent();

        return dimensionExistedInRuntime || dimensionExistedInConfig;
    }

    public static void deleteRuntimeDimensionDescriptor(String dimensionId) {
        Optional<RuntimeDimensionDescriptor> first = WorldInitializer.world.model().dimension_list.stream().filter(o -> o.getDimension().equals(dimensionId)).findFirst();
        first.ifPresent(dimensionNode -> {
            WorldInitializer.world.model().dimension_list.remove(dimensionNode);
            WorldInitializer.world.writeStorage();
        });
    }

    public static void saveRuntimeDimensionDescriptors() {
        WorldInitializer.config.writeStorage();
    }

    public static Optional<RuntimeDimensionDescriptor> getRuntimeDimensionDescriptor(String dimensionId) {
        return WorldInitializer.world.model()
            .dimension_list
            .stream()
            .filter(it -> it.dimension.equalsIgnoreCase(dimensionId))
            .findFirst();
    }

    public static List<RuntimeDimensionDescriptor> getUnloadedRuntimeDimensionDescriptors() {
        return WorldInitializer.world.model().dimension_list
            .stream()
            .filter(it -> !it.isDimensionLoaded())
            .toList();
    }
}


