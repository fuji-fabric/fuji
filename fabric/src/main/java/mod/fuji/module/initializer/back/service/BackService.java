package mod.fuji.module.initializer.back.service;

import java.util.function.Function;
import mod.fuji.core.auxiliary.ChronosUtil;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.argument.wrapper.impl.Dimension;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.core.config.mapper.structure.PlayerKey;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerDeathEvent;
import mod.fuji.core.event.message.player.PlayerTeleportPreEvent;
import mod.fuji.core.structure.GlobalPos;
import mod.fuji.module.initializer.back.BackInitializer;
import mod.fuji.module.initializer.back.structure.LocationHistory;
import mod.fuji.module.initializer.back.structure.LocationSnapshot;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BackService {

    public static <R> R withLocationHistory(@NotNull ServerPlayer player, @NotNull Function<LocationHistory, R> function) {
        String playerName = PlayerHelper.getPlayerName(player);
        LocationHistory locationHistory = PlayerKey
            .computeValueByPlayerNameOrThrow(BackInitializer.playerBackLocationHistoryData.model().getPlayer2history(), playerName, k -> new LocationHistory());
        return function.apply(locationHistory);
    }

    public static int listBackLocations(@NotNull CommandSourceStack source, @NotNull ServerPlayer player) {
        return withLocationHistory(player, locationHistory -> {
            /* Print header. */
            String targetPlayerName = PlayerHelper.getPlayerName(player);
            TextHelper.sendTextByKey(source, "back.list", targetPlayerName);

            /* Print body. */
            locationHistory
                .listLocationSnapshots()
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

    @SuppressWarnings("CodeBlock2Expr")
    public static int teleportToLocation(@NotNull ServerPlayer player, int lastNLocation, @Nullable Dimension targetDimension) {
        return withLocationHistory(player, locationHistory -> {
            return locationHistory
                .findLocationSnapshot(lastNLocation, targetDimension)
                .map(targetLocationSnapshot -> {
                    targetLocationSnapshot.getLocation().teleport(player);
                    return CommandHelper.Return.SUCCESS;
                })
                .orElseThrow(() -> {
                    TextHelper.sendTextByKey(player, "back.no_previous_position");
                    return new AbortCommandExecutionException();
                });
        });
    }

    private static int getMaxBackLocationsToSave(@NotNull ServerPlayer player) {
        return LuckpermsHelper
            .getMeta(player.getUUID(), BackInitializer.MAX_LOCATIONS_TO_SAVE_META)
            .orElse(BackInitializer.config.model().getMaxBackLocationsToSave());
    }

    @SuppressWarnings({"RedundantIfStatement", "CodeBlock2Expr"})
    private static boolean shouldPushCurrentLocation(@NotNull ServerPlayer player) {
        return withLocationHistory(player, locationHistory -> {
            return locationHistory
                .getLastLocationSnapshot()
                .map(lastLocationSnapshot -> {
                    /* Process ignore distance option. */
                    GlobalPos lastLocation = lastLocationSnapshot.getLocation();
                    double ignoreDistance = BackInitializer.config.model().getDoNotPushBackLocationIfCloserThanNBlocks();
                    double ignoreDistanceSquare = ignoreDistance * ignoreDistance;
                    boolean tooClose = EntityHelper.getPos(player).distanceToSqr(lastLocation.getX(), lastLocation.getY(), lastLocation.getZ()) <= ignoreDistanceSquare;
                    if (lastLocation.sameLevel(PlayerHelper.getServerWorld(player)) && tooClose) {
                        return false;
                    }

                    return true;
                })
                /* Always save if there is no locations. */
                .orElse(true);
        });
    }

    private static void tryPushCurrentLocation(@NotNull ServerPlayer player) {
        withLocationHistory(player, locationHistory -> {
            if (shouldPushCurrentLocation(player)) {
                LocationSnapshot locationSnapshot = LocationSnapshot.ofPlayer(player);
                pushBackLocation(player, locationHistory, locationSnapshot);
            }

            return null;
        });
    }

    public static void pushBackLocation(@NotNull ServerPlayer player, @NotNull LocationHistory locationHistory, @NotNull LocationSnapshot locationSnapshot) {
        locationHistory.pushLocationSnapshot(locationSnapshot);
        locationHistory.trimHistory(getMaxBackLocationsToSave(player));
    }

    @EventConsumer
    private static void handleOnPlayerDeathEvent(PlayerDeathEvent event) {
        if (BackInitializer.config.model().isPushBackLocationOnPlayerDeath()) {
            tryPushCurrentLocation(event.getPlayer());
        }
    }

    @EventConsumer
    private static void handlePlayerPreTeleportEvent(PlayerTeleportPreEvent event) {
        if (event.getCallbackInfo().isCancelled()) return;

        if (BackInitializer.config.model().isPushBackLocationOnPlayerTeleport()) {
            tryPushCurrentLocation(event.getPlayer());
        }
    }
}
