package mod.fuji.module.initializer.world.manager.service;

import mod.fuji.core.annotation.Unused;
import mod.fuji.core.auxiliary.IOUtil;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.lifecycle.ServerStartedEvent;
import mod.fuji.core.event.message.server.tick.ServerTickStartEvent;
import mod.fuji.core.service.bossbar.BossBarManager;
import mod.fuji.core.structure.GlobalPos;
import mod.fuji.core.structure.Pair;
import mod.fuji.core.structure.TeleportTicket;
import mod.fuji.module.initializer.world.manager.WorldInitializer;
import mod.fuji.module.initializer.world.manager.service.structure.DimensionCreationTicket;
import mod.fuji.module.initializer.world.manager.service.structure.DimensionDeletionTicket;
import mod.fuji.module.initializer.world.manager.structure.RuntimeDimensionDescriptor;
import mod.fuji.module.initializer.world.manager.structure.RuntimeDimensionLoader;
import mod.fuji.module.initializer.world.manager.structure.RuntimeDimensionMaker;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
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
        ServerLevel world = ticket.world;
        saveChunksBeforeUnloadingTheDimension(world);

        ServerHelper.executeSync(() -> dimensionDeletionTicketQueue.add(ticket));
    }

    private static void saveChunksBeforeUnloadingTheDimension(ServerLevel world) {
        ServerHelper.executeSync(() -> {
            world.noSave = false;
            #if MC_VER <= MC_1_21_4
            world.getChunkManager().removePersistentTickets();
            #elif MC_VER > MC_1_21_4
            world.getChunkSource().deactivateTicketsOnClosing();
            #endif

            world.getChunkSource().tick(() -> true, false);
        });
    }

    public static void submitDimensionCreationTicket(DimensionCreationTicket ticket) {
        ServerHelper.executeSync(() -> dimensionCreationQueue.add(ticket));
    }

    private static boolean tryConsumeDimensionDeletionTicket(@NotNull DimensionDeletionTicket ticket) {
        ServerLevel world = ticket.getWorld();

        if (world.players().isEmpty() && !shouldDelayShutdown(world)) {
            consumeDimensionDeletionTicket(ticket);
            return true;
        } else {
            evacuatePlayers(world);
            return false;
        }
    }

    private static boolean shouldDelayShutdown(ServerLevel world) {
        #if MC_VER <= MC_1_20_6
        return world.getChunkManager().threadedAnvilChunkStorage.shouldDelayShutdown();
        #elif MC_VER > MC_1_20_6
        return world.getChunkSource().chunkMap.hasWork();
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
            Pair<ServerLevel, LevelStem> result = RuntimeDimensionMaker.makeRuntimeDimension(descriptor);
            ServerLevel dimension = result.getKey();
            LevelStem dimensionOptions = result.getValue();

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

    private static void evacuatePlayers(@NotNull ServerLevel dimension) {
        List<ServerPlayer> players = new ArrayList<>(dimension.players());
        for (ServerPlayer player : players) {
            GlobalPos from = GlobalPos.of(player);
            GlobalPos to = WorldHelper.SpawnPos.getSafeServerSpawnPos();

            TeleportTicket ticket = TeleportTicket.makeVipTicket(player, from, to);
            BossBarManager.addTicket(ticket);
        }
    }


    private static void consumeDimensionDeletionTicket(@NotNull DimensionDeletionTicket ticket) {
        ServerLevel world = ticket.getWorld();
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

    private static void deleteDimensionFiles(@NotNull ServerLevel world) {
        MinecraftServer server = world.getServer();
        ResourceKey<Level> dimensionKey = world.dimension();

        /* Delete world files. */
        File worldDirectory = server.storageSource.getDimensionPath(dimensionKey).toFile();
        IOUtil.deleteFilesAndPreserveDirs(worldDirectory);
    }

    public static boolean existsDimension(ResourceLocation dimensionId) {
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

    @EventConsumer
    private static void loadRuntimeDimensions(@Unused ServerStartedEvent event) {
        WorldInitializer.world.model().dimension_list
            .stream()
            .filter(RuntimeDimensionDescriptor::isAuto_load_on_server_startup)
            .forEach(it -> {
                try {
                    DimensionCreationTicket ticket = new DimensionCreationTicket(CommandHelper.Source.getConsoleCommandSource(), it);
                    submitDimensionCreationTicket(ticket);
                    LogUtil.info("Load dimension {} into the server.", it.getDimension());
                } catch (Exception e) {
                    LogUtil.error("Failed to load dimension `{}`", it, e);
                }
            });
    }
}


