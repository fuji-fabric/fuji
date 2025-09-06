package io.github.sakurawald.fuji.module.initializer.world.manager.service;

import io.github.sakurawald.fuji.core.annotation.Unused;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.impl.on_demand.ServerTickStartEvent;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.core.structure.Pair;
import io.github.sakurawald.fuji.core.structure.TeleportTicket;
import io.github.sakurawald.fuji.module.initializer.world.manager.WorldInitializer;
import io.github.sakurawald.fuji.module.initializer.world.manager.service.structure.DimensionCreationTicket;
import io.github.sakurawald.fuji.module.initializer.world.manager.service.structure.DimensionDeletionTicket;
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
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import org.jetbrains.annotations.NotNull;

public class WorldService {

    private static final Set<DimensionCreationTicket> dimensionCreationQueue = new ReferenceOpenHashSet<>();
    private static final Set<DimensionDeletionTicket> dimensionDeletionTicketQueue = new ReferenceOpenHashSet<>();

    @EventConsumer
    private static void processDimensionCreationAndDeletionQueue(@Unused ServerTickStartEvent event) {
        dimensionDeletionTicketQueue.removeIf(WorldService::tryConsumeDimensionDeletionTicket);
        dimensionCreationQueue.removeIf(WorldService::tryConsumeDimensionCreationTicket);
    }

    public static void submitDimensionDeletionTicket(@NotNull DimensionDeletionTicket ticket) {
        ServerWorld world = ticket.world;
        saveChunksBeforeUnloadingTheDimension(world);

        ServerHelper.executeSync(() -> dimensionDeletionTicketQueue.add(ticket));
    }

    private static void saveChunksBeforeUnloadingTheDimension(ServerWorld world) {
        ServerHelper.executeSync(() -> {
            world.savingDisabled = false;
            #if MC_VER <= MC_1_21_4
            world.getChunkManager().removePersistentTickets();
            #elif MC_VER > MC_1_21_4
            world.getChunkManager().shutdown();
            #endif

            world.getChunkManager().tick(() -> true, false);
        });
    }

    public static void submitDimensionCreationTicket(DimensionCreationTicket ticket) {
        ServerHelper.executeSync(() -> dimensionCreationQueue.add(ticket));
    }

    private static boolean tryConsumeDimensionDeletionTicket(@NotNull DimensionDeletionTicket ticket) {
        ServerWorld world = ticket.getWorld();

        if (world.getPlayers().isEmpty() && !shouldDelayShutdown(world)) {
            consumeDimensionDeletionTicket(ticket);
            return true;
        } else {
            evacuatePlayers(world);
            return false;
        }
    }

    private static boolean shouldDelayShutdown(ServerWorld world) {
        #if MC_VER <= MC_1_20_6
        return world.getChunkManager().threadedAnvilChunkStorage.shouldDelayShutdown();
        #elif MC_VER > MC_1_20_6
        return world.getChunkManager().chunkLoadingManager.shouldDelayShutdown();
        #endif
    }

    private static boolean tryConsumeDimensionCreationTicket(@NotNull DimensionCreationTicket ticket) {
        // NOTE: Wait the target dimension to be deleted first (If there is a deletion ticket for it).
        if (dimensionDeletionTicketQueue.stream().anyMatch(it -> RegistryHelper.getIdAsString(it.world).equals(ticket.descriptor.dimension))) {
            return false;
        }

        consumeDimensionCreationTicket(ticket);
        // NOTE: Returns true anyway, to prevent the console spam.
        return true;
    }

    private static void consumeDimensionCreationTicket(@NotNull DimensionCreationTicket ticket) {
        RuntimeDimensionDescriptor descriptor = ticket.descriptor;

        try {
            /* Make the runtime dimension. */
            Pair<ServerWorld, DimensionOptions> result = RuntimeDimensionMaker.makeRuntimeDimension(descriptor);
            ServerWorld dimension = result.getKey();
            DimensionOptions dimensionOptions = result.getValue();

            /* Load the runtime dimension. */
            RuntimeDimensionLoader.loadRuntimeDimension(dimension, dimensionOptions);

            /* Start ticking it. */
            dimension.tick(() -> true);

            /* Send feedback. */
            TextHelper.sendTextByKey(ticket.source, "world.dimension.created", ticket.descriptor.dimension);
        } catch (Exception e) {
            LogUtil.error("Failed to make RuntimeDimension instance: dimension descriptor = {}", descriptor, e);
        }
    }

    private static void evacuatePlayers(@NotNull ServerWorld dimension) {
        ServerWorld safeDimension = dimension.getServer().getOverworld();
        BlockPos safeBlockPos = safeDimension.getSpawnPos();

        List<ServerPlayerEntity> players = new ArrayList<>(dimension.getPlayers());
        for (ServerPlayerEntity player : players) {
            GlobalPos from = GlobalPos.of(player);
            GlobalPos to = new GlobalPos(safeDimension, safeBlockPos.getX() + 0.5, safeBlockPos.getY() + 0.5, safeBlockPos.getZ() + 0.5, 0, 0);

            BlockPos topPosition = safeDimension.getTopPosition(Heightmap.Type.MOTION_BLOCKING, player.getBlockPos());
            to = to.withY(topPosition.getY());

            TeleportTicket teleportTicket = TeleportTicket.makeVipTicket(player, from, to);
            Managers.getBossBarManager().addTicket(teleportTicket);
        }
    }

    private static void consumeDimensionDeletionTicket(@NotNull DimensionDeletionTicket ticket) {
        ServerWorld world = ticket.getWorld();
        RuntimeDimensionLoader.unloadDimension(world);

        if (ticket.deleteWorldFiles) {
            deleteDimensionFiles(world);
        }

        if (ticket.deleteRuntimeDimensionDescriptor) {
            String dimensionId = RegistryHelper.getIdAsString(world);
            WorldService.deleteRuntimeDimensionDescriptor(dimensionId);
        }

        TextHelper.sendTextByKey(ticket.source,"world.dimension.deleted", RegistryHelper.getIdAsString(ticket.world));
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
        boolean dimensionExistedInRuntime = WorldHelper
            .getWorlds()
            .stream()
            .anyMatch(it -> RegistryHelper.getIdAsString(it).equals(dimensionId.toString()));
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

    public static List<RuntimeDimensionDescriptor> getRuntimeDimensionDescriptors() {
        return WorldInitializer.world.model().dimension_list;
    }

    public static void loadRuntimeDimensions() {
        WorldInitializer.world.model().dimension_list
            .stream()
            .filter(RuntimeDimensionDescriptor::isAuto_load_on_server_startup)
            .forEach(it -> {
                try {
                    DimensionCreationTicket ticket = new DimensionCreationTicket(ServerHelper.getServer().getCommandSource(), it);
                    submitDimensionCreationTicket(ticket);
                    LogUtil.info("Load dimension {} into the server.", it.getDimension());
                } catch (Exception e) {
                    LogUtil.error("Failed to load dimension `{}`", it, e);
                }
            });
    }
}


