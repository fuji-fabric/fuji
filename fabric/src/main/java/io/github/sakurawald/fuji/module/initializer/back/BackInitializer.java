package io.github.sakurawald.fuji.module.initializer.back;

import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.annotation.CommandTarget;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.descriptor.MetaDescriptor;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.back.config.model.BackConfigModel;
import io.github.sakurawald.fuji.module.initializer.back.config.model.BackLocationHistoryModel;
import io.github.sakurawald.fuji.module.initializer.back.service.BackService;
import io.github.sakurawald.fuji.module.initializer.back.structure.LocationEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751825568845L, value = """
    THis module allows the player to teleport back to:
    1. His last teleport point.
    2. His death point.
    """)
public class BackInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<BackConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, BackConfigModel.class);

    public static final BaseConfigurationHandler<BackLocationHistoryModel> savedPositionConfig = ObjectConfigurationHandler
        .ofModule("location-history.json", BackLocationHistoryModel.class)
        .enableAutoSaveFeature();

    @DocStringProvider(id = 1751999540893L, value = """
        The max location entries to save for this player.
        """)
    public static final MetaDescriptor<Integer> MAX_LOCATION_ENTRIES_TO_SAVE_META = new MetaDescriptor<>("fuji.back.max_location_entries_to_save", Integer::valueOf, 1751999540893L);

    @Document(id = 1751825574805L, value = "Push current location into the back location history.")
    @CommandNode("back push")
    @CommandRequirement(level = 4)
    private static int $push(@CommandSource @CommandTarget ServerPlayerEntity player) {
        return BackService.withLocationHistory(player, locationHistory -> {
            LocationEntry locationEntry = LocationEntry.makeLocationEntry(player);
            BackService.pushBackLocation(player,locationHistory,locationEntry);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751825581305L, value = "Clear the back location history.")
    @CommandNode("back clear")
    @CommandRequirement(level = 4)
    private static int $clear(@CommandSource CommandContext<ServerCommandSource> source, @CommandTarget ServerPlayerEntity player) {
        return BackService.withLocationHistory(player, locationHistory -> {
            locationHistory.clearEntries();
            TextHelper.sendTextByKey(source, "back.clear", player.getGameProfile().getName());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751825587373L, value = "List the back location history.")
    @CommandNode("back list")
    @CommandRequirement(level = 4)
    private static int $list(@CommandSource CommandContext<ServerCommandSource> source, ServerPlayerEntity player) {
        return BackService.listBackLocations(source.getSource(), player);
    }

    @Document(id = 1751825593993L, value = "List the back location history.")
    @CommandNode("back list")
    private static int $list(@CommandSource ServerPlayerEntity source) {
        return BackService.listBackLocations(source.getCommandSource(), source);
    }

    @Document(id = 1751825598230L, value = "Back to the specified location.")
    @CommandNode("back")
    private static int $back(@CommandSource ServerPlayerEntity player) {
        return BackService.teleportBackLocation(player, 1, null);
    }

    @Document(id = 1751825604578L, value = "Back to the specified location.")
    @CommandNode("back")
    private static int $back(@CommandSource ServerPlayerEntity player, int lastNLocation) {
        return BackService.teleportBackLocation(player, lastNLocation, null);
    }

    @Document(id = 1751825608994L, value = "Back to the specified location.")
    @CommandNode("back")
    private static int $back(@CommandSource ServerPlayerEntity player, int lastNLocation, Dimension targetDimension) {
        return BackService.teleportBackLocation(player, lastNLocation, targetDimension);
    }

    @Document(id = 1751825615959L, value = "Back to the specified location.")
    @CommandNode("back 1")
    private static int $back(@CommandSource ServerPlayerEntity player, Dimension targetDimension) {
        return BackService.teleportBackLocation(player, 1, targetDimension);
    }

}
