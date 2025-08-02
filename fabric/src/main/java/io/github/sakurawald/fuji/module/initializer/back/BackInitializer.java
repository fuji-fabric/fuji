package io.github.sakurawald.fuji.module.initializer.back;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.core.document.descriptor.MetaDescriptor;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.back.config.model.BackConfigModel;
import io.github.sakurawald.fuji.module.initializer.back.config.model.BackLocationHistoryModel;
import io.github.sakurawald.fuji.module.initializer.back.structure.LocationEntry;
import io.github.sakurawald.fuji.module.initializer.back.structure.LocationHistory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

@Document(id = 1751825568845L, value = """
    THis module allows the player to teleport back to:
    1. His last teleport point.
    2. His death point.
    """)
public class BackInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<BackConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, BackConfigModel.class);

    private static final BaseConfigurationHandler<BackLocationHistoryModel> savedPositionConfig = new ObjectConfigurationHandler<>("location-history.json", BackLocationHistoryModel.class)
        .enableAutoSaveFeature();

    @DocStringProvider(id = 1751999540893L, value = """
        The max location entries to save for this player.
        """)
    private static final MetaDescriptor<Integer> MAX_LOCATION_ENTRIES_TO_SAVE_META = new MetaDescriptor<>("fuji.back.max_location_entries_to_save", Integer::valueOf, 1751999540893L);

    private static <R> R withLocationHistory(@NotNull ServerPlayerEntity player, Function<LocationHistory, R> function) {
        String playerName = player.getGameProfile().getName();
        savedPositionConfig.model().player2history.computeIfAbsent(playerName, k -> new LocationHistory());
        LocationHistory locationHistory = savedPositionConfig.model().player2history.get(playerName);
        return function.apply(locationHistory);
    }

    @Document(id = 1751825574805L, value = "Push current location into the back location history.")
    @CommandNode("back push")
    @CommandRequirement(level = 4)
    private static int $push(@CommandSource @CommandTarget ServerPlayerEntity player) {
        return withLocationHistory(player, locationHistory -> {
            LocationEntry locationEntry = LocationEntry.makeLocationEntry(player);
            pushEntryAndTrim(player,locationHistory,locationEntry);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751825581305L, value = "Clear the back location history.")
    @CommandNode("back clear")
    @CommandRequirement(level = 4)
    private static int $clear(@CommandSource CommandContext<ServerCommandSource> source, @CommandTarget ServerPlayerEntity player) {
        return withLocationHistory(player, locationHistory -> {
            locationHistory.clearEntries();
            TextHelper.sendTextByKey(source, "back.clear", player.getGameProfile().getName());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751825587373L, value = "List the back location history.")
    @CommandNode("back list")
    @CommandRequirement(level = 4)
    private static int $list(@CommandSource CommandContext<ServerCommandSource> source, ServerPlayerEntity player) {
        return backListWithParameters(source.getSource(), player);
    }

    @Document(id = 1751825593993L, value = "List the back location history.")
    @CommandNode("back list")
    private static int $list(@CommandSource ServerPlayerEntity source) {
        return backListWithParameters(source.getCommandSource(), source);
    }

    private static Integer backListWithParameters(ServerCommandSource source, ServerPlayerEntity player) {
        return withLocationHistory(player, locationHistory -> {
            // Print header.
            String targetPlayerName = player.getGameProfile().getName();
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

    @Document(id = 1751825598230L, value = "Back to the specified location.")
    @CommandNode("back")
    private static int $back(@CommandSource ServerPlayerEntity player) {
        return backWithParameters(player, 1, null);
    }

    @Document(id = 1751825604578L, value = "Back to the specified location.")
    @CommandNode("back")
    private static int $back(@CommandSource ServerPlayerEntity player, int lastNLocation) {
        return backWithParameters(player, lastNLocation, null);
    }

    @Document(id = 1751825608994L, value = "Back to the specified location.")
    @CommandNode("back")
    private static int $back(@CommandSource ServerPlayerEntity player, int lastNLocation, Dimension targetDimension) {
        return backWithParameters(player, lastNLocation, targetDimension);
    }

    @Document(id = 1751825615959L, value = "Back to the specified location.")
    @CommandNode("back")
    private static int $back(@CommandSource ServerPlayerEntity player, Dimension targetDimension) {
        return backWithParameters(player, 1, targetDimension);
    }

    private static int backWithParameters(@NotNull ServerPlayerEntity player, int lastNLocation, @Nullable Dimension targetDimension) {
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
        Optional<Integer> value = LuckpermsHelper.getMeta(player.getUuid(), MAX_LOCATION_ENTRIES_TO_SAVE_META);
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
            GlobalPos latestLocation = latestEntry.getLocation();
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

    public static void trySaveCurrentLocationOnTeleport(ServerPlayerEntity player) {
        if (config.model().enable_back_on_teleport) {
            trySaveCurrentLocation(player);
        }
    }
}
