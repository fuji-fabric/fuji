package io.github.sakurawald.module.initializer.back;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.annotation.Document;
import io.github.sakurawald.core.auxiliary.ChronosUtil;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.auxiliary.minecraft.PermissionHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.command.annotation.CommandNode;
import io.github.sakurawald.core.command.annotation.CommandRequirement;
import io.github.sakurawald.core.command.annotation.CommandSource;
import io.github.sakurawald.core.command.annotation.CommandTarget;
import io.github.sakurawald.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.structure.SpatialPose;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.back.config.model.BackConfigModel;
import io.github.sakurawald.module.initializer.back.config.model.BackLocationHistoryModel;
import io.github.sakurawald.module.initializer.back.structure.LocationEntry;
import io.github.sakurawald.module.initializer.back.structure.LocationHistory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public class BackInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<BackConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, BackConfigModel.class);

    private static final BaseConfigurationHandler<BackLocationHistoryModel> savedPositionConfig = new ObjectConfigurationHandler<>("location-history.json", BackLocationHistoryModel.class)
        .autoSaveEveryMinute();

    private static <R> R withLocationHistory(@NotNull ServerPlayerEntity player, Function<LocationHistory, R> function) {
        String playerName = player.getGameProfile().getName();
        savedPositionConfig.model().player2history.computeIfAbsent(playerName, k -> new LocationHistory());
        LocationHistory locationHistory = savedPositionConfig.model().player2history.get(playerName);
        return function.apply(locationHistory);
    }

    @CommandNode("back push")
    @CommandRequirement(level = 4)
    @Document("Push current location into the back location history.")
    private static int $push(@CommandSource @CommandTarget ServerPlayerEntity player) {
        return withLocationHistory(player, locationHistory -> {
            LocationEntry locationEntry = LocationEntry.makeLocationEntry(player);
            pushEntryAndTrim(player,locationHistory,locationEntry);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @CommandNode("back clear")
    @CommandRequirement(level = 4)
    @Document("Clear the back location history.")
    private static int $clear(@CommandSource CommandContext<ServerCommandSource> source, @CommandTarget ServerPlayerEntity player) {
        return withLocationHistory(player, locationHistory -> {
            locationHistory.clearEntries();
            TextHelper.sendMessageByKey(source, "back.clear", player.getGameProfile().getName());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @CommandNode("back list")
    @CommandRequirement(level = 4)
    @Document("List the back location history.")
    private static int $list(@CommandSource CommandContext<ServerCommandSource> source, ServerPlayerEntity player) {
        return backListWithParameters(source.getSource(), player);
    }

    @CommandNode("back list")
    @Document("List the back location history.")
    private static int $list(@CommandSource ServerPlayerEntity source) {
        return backListWithParameters(source.getCommandSource(), source);
    }

    private static Integer backListWithParameters(ServerCommandSource source, ServerPlayerEntity player) {
        return withLocationHistory(player, locationHistory -> {
            // Print header.
            String targetPlayerName = player.getGameProfile().getName();
            TextHelper.sendMessageByKey(source, "back.list", targetPlayerName);

            // Print body.
            locationHistory
                .listEntries()
                .forEachRemaining(it -> {
                    SpatialPose location = it.getLocation();
                    TextHelper.sendMessageByKey(source, "back.list.entry"
                        , location.getLevel()
                        , location.getX()
                        , location.getY()
                        , location.getZ()
                        , ChronosUtil.toStandardDateFormat(it.getSavedTimestamp()));
                });

            return CommandHelper.Return.SUCCESS;
        });
    }

    @CommandNode("back")
    @Document("Back to the specified location.")
    private static int $back(@CommandSource ServerPlayerEntity player) {
        return backWithParameters(player, 1, null);
    }

    @CommandNode("back")
    @Document("Back to the specified location.")
    private static int $back(@CommandSource ServerPlayerEntity player, int lastNLocation) {
        return backWithParameters(player, lastNLocation, null);
    }

    @CommandNode("back")
    @Document("Back to the specified location.")
    private static int $back(@CommandSource ServerPlayerEntity player, int lastNLocation, Dimension targetDimension) {
        return backWithParameters(player, lastNLocation, targetDimension);
    }

    @CommandNode("back")
    @Document("Back to the specified location.")
    private static int $back(@CommandSource ServerPlayerEntity player, Dimension targetDimension) {
        return backWithParameters(player, 1, targetDimension);
    }

    private static int backWithParameters(@NotNull ServerPlayerEntity player, int lastNLocation, @Nullable Dimension targetDimension) {
        return withLocationHistory(player, locationHistory -> {
            // find location entry.
            LocationEntry latestEntry = locationHistory.findEntry(lastNLocation, targetDimension);
            if (latestEntry == null) {
                TextHelper.sendActionBarByKey(player, "back.no_previous_position");
                throw new AbortCommandExecutionException();
            }

            // teleport with the location entry.
            latestEntry.getLocation().teleport(player);
            return CommandHelper.Return.SUCCESS;
        });
    }

    private static int getMaxBackLocationEntriesToSave(@NotNull ServerPlayerEntity player) {
        Optional<Integer> value = PermissionHelper.getMeta(player.getUuid(), "fuji.back.max_location_entries_to_save", Integer::valueOf);
        return value.orElse(config.model().max_back_location_entries_to_save);
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
            SpatialPose latestLocation = latestEntry.getLocation();
            double ignoreDistance = config.model().ignore_distance;
            if (latestLocation.sameLevel(player.getWorld())
                && player.getPos().squaredDistanceTo(latestLocation.getX(), latestLocation.getY(), latestLocation.getZ()) <= ignoreDistance * ignoreDistance
            ) {
                return false;
            }

            return true;
        });
    }

    public static void trySaveCurrentLocation(@NotNull ServerPlayerEntity player) {
        withLocationHistory(player, locationHistory -> {
            if (shouldSaveCurrentLocation(player)) {
                LocationEntry locationEntry = LocationEntry.makeLocationEntry(player);
                pushEntryAndTrim(player, locationHistory, locationEntry);
            }

            return null;
        });
    }

    private static void pushEntryAndTrim(@NotNull ServerPlayerEntity player, @NotNull LocationHistory locationHistory, @NotNull LocationEntry locationEntry) {
        locationHistory.pushEntry(locationEntry);
        locationHistory.trimEntries(getMaxBackLocationEntriesToSave(player));
    }

}
