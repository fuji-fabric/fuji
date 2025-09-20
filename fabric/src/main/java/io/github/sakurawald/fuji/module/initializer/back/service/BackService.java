package io.github.sakurawald.fuji.module.initializer.back.service;

import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerDeathEvent;
import io.github.sakurawald.fuji.core.event.message.player.PlayerTeleportPreEvent;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.module.initializer.back.BackInitializer;
import io.github.sakurawald.fuji.module.initializer.back.structure.LocationEntry;
import io.github.sakurawald.fuji.module.initializer.back.structure.LocationHistory;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BackService {

    public static <R> R withLocationHistory(@NotNull ServerPlayerEntity player, Function<LocationHistory, R> function) {
        String playerName = PlayerHelper.getPlayerName(player);
        BackInitializer.savedPositionConfig.model().player2history.computeIfAbsent(playerName, k -> new LocationHistory());
        LocationHistory locationHistory = BackInitializer.savedPositionConfig.model().player2history.get(playerName);
        return function.apply(locationHistory);
    }

    public static Integer listBackLocations(ServerCommandSource source, ServerPlayerEntity player) {
        return withLocationHistory(player, locationHistory -> {
            // Print header.
            String targetPlayerName = PlayerHelper.getPlayerName(player);
            TextHelper.sendTextByKey(source, "back.list", targetPlayerName);

            // Print body.
            locationHistory
                .listEntries()
                .forEachRemaining(it -> {
                    GlobalPos location = it.getLocation();
                    TextHelper.sendTextByKey(source, "back.list.entry"
                        , location.getLevel()
                        , location.getX()
                        , location.getY()
                        , location.getZ()
                        , ChronosUtil.Formatter.formatDate(it.getSavedTimestamp()));
                });

            return CommandHelper.Return.SUCCESS;
        });
    }

    public static int teleportBackLocation(@NotNull ServerPlayerEntity player, int lastNLocation, @Nullable Dimension targetDimension) {
        return withLocationHistory(player, locationHistory -> {
            // find location entry.
            LocationEntry latestEntry = locationHistory.findEntry(lastNLocation, targetDimension);
            if (latestEntry == null) {
                TextHelper.sendTextByKey(player, "back.no_previous_position");
                throw new AbortCommandExecutionException();
            }

            // teleport with the location entry.
            latestEntry.getLocation().teleport(player);
            return CommandHelper.Return.SUCCESS;
        });
    }

    private static int getMaxBackLocationEntriesToSave(@NotNull ServerPlayerEntity player) {
        Optional<Integer> value = LuckpermsHelper.getMeta(player.getUuid(), BackInitializer.MAX_LOCATION_ENTRIES_TO_SAVE_META);
        return value.orElse(BackInitializer.config.model().max_back_location_entries_to_save);
    }

    @SuppressWarnings("RedundantIfStatement")
    private static boolean shouldSaveCurrentLocation(@NotNull ServerPlayerEntity player) {
        return withLocationHistory(player, locationHistory -> {
            LocationEntry latestEntry = locationHistory.getLatestEntry();

            // Should save if there is no history entry before.
            if (latestEntry == null) {
                return true;
            }

            // Should not save location inside ignore distance.
            GlobalPos latestLocation = latestEntry.getLocation();
            double ignoreDistance = BackInitializer.config.model().ignore_distance;
            if (latestLocation.sameLevel(PlayerHelper.getServerWorld(player))
                && player.getPos().squaredDistanceTo(latestLocation.getX(), latestLocation.getY(), latestLocation.getZ()) <= ignoreDistance * ignoreDistance
            ) {
                return false;
            }

            return true;
        });
    }

    private static void trySaveCurrentLocation(@NotNull ServerPlayerEntity player) {
        withLocationHistory(player, locationHistory -> {
            if (shouldSaveCurrentLocation(player)) {
                LocationEntry locationEntry = LocationEntry.makeLocationEntry(player);
                pushBackLocation(player, locationHistory, locationEntry);
            }

            return null;
        });
    }

    public static void pushBackLocation(@NotNull ServerPlayerEntity player, @NotNull LocationHistory locationHistory, @NotNull LocationEntry locationEntry) {
        locationHistory.pushEntry(locationEntry);
        locationHistory.trimEntries(getMaxBackLocationEntriesToSave(player));
    }

    @EventConsumer
    private static void handleOnPlayerDeathEvent(PlayerDeathEvent event) {
        if (BackInitializer.config.model().enable_back_on_death) {
            trySaveCurrentLocation(event.getPlayer());
        }
    }

    @EventConsumer
    private static void handlePlayerPreTeleportEvent(PlayerTeleportPreEvent event) {
        if (event.getCallbackInfo().isCancelled()) return;

        if (BackInitializer.config.model().enable_back_on_teleport) {
            trySaveCurrentLocation(event.getPlayer());
        }
    }
}
