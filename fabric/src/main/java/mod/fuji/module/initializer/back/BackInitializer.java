package mod.fuji.module.initializer.back;

import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.command.annotation.CommandTarget;
import mod.fuji.core.command.argument.wrapper.impl.Dimension;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.descriptor.MetaDescriptor;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.back.config.model.BackConfigModel;
import mod.fuji.module.initializer.back.config.model.BackLocationHistoryModel;
import mod.fuji.module.initializer.back.service.BackService;
import mod.fuji.module.initializer.back.structure.LocationSnapshot;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

@Document(id = 1751825568845L, value = """
    This module allows a player to `teleport` to their `last location`.
    The `last location` can be:
    - Their last teleport location.
    - Their last death location.
    """)
public class BackInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<BackConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, BackConfigModel.class);

    public static final BaseConfigurationHandler<BackLocationHistoryModel> playerBackLocationHistoryData = ObjectConfigurationHandler
        .ofModule("location-history.json", BackLocationHistoryModel.class)
        .enableAutoSaveFeature();

    @DocStringProvider(id = 1751999540893L, value = """
        The max locations to save for this player.
        """)
    public static final MetaDescriptor<Integer> MAX_LOCATIONS_TO_SAVE_META = new MetaDescriptor<>("fuji.back.max_location_entries_to_save", Integer::valueOf, 1751999540893L);

    @Document(id = 1751825574805L, value = "Push current location into the location history.")
    @CommandNode("back push")
    @CommandRequirement(level = 4)
    private static int $push(@CommandSource @CommandTarget ServerPlayer player) {
        return BackService.withLocationHistory(player, locationHistory -> {
            LocationSnapshot locationSnapshot = LocationSnapshot.ofPlayer(player);
            BackService.pushBackLocation(player,locationHistory, locationSnapshot);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751825581305L, value = "Clear the location history.")
    @CommandNode("back clear")
    @CommandRequirement(level = 4)
    private static int $clear(@CommandSource CommandContext<CommandSourceStack> source, @CommandTarget ServerPlayer player) {
        return BackService.withLocationHistory(player, locationHistory -> {
            locationHistory.clearHistory();
            String playerName = PlayerHelper.getPlayerName(player);
            TextHelper.sendTextByKey(source, "back.clear", playerName);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751825587373L, value = "List the locations in the location history.")
    @CommandNode("back list")
    @CommandRequirement(level = 4)
    private static int $list(@CommandSource CommandContext<CommandSourceStack> source, ServerPlayer player) {
        return BackService.listBackLocations(source.getSource(), player);
    }

    @Document(id = 1751825593993L, value = "List the locations in the location history.")
    @CommandNode("back list")
    private static int $list(@CommandSource ServerPlayer source) {
        return BackService.listBackLocations(CommandHelper.Source.getCommandSource(source), source);
    }

    @Document(id = 1751825598230L, value = "Teleport to the last `one` location in the location history.")
    @CommandNode("back")
    private static int $back(@CommandSource ServerPlayer player) {
        return BackService.teleportToLocation(player, 1, null);
    }

    @Document(id = 1751825604578L, value = "Teleport to the last `N` location in the location history.")
    @CommandNode("back")
    private static int $back(@CommandSource ServerPlayer player, int lastNLocation) {
        return BackService.teleportToLocation(player, lastNLocation, null);
    }

    @Document(id = 1751825615959L, value = "Teleport to the last `one` location in specified `dimension` in the location history.")
    @CommandNode("back 1")
    private static int $back(@CommandSource ServerPlayer player, Dimension targetDimension) {
        return BackService.teleportToLocation(player, 1, targetDimension);
    }

    @Document(id = 1751825608994L, value = "Teleport to the last `N` location in specified `dimension` in the location history.")
    @CommandNode("back")
    private static int $back(@CommandSource ServerPlayer player, int lastNLocation, Dimension targetDimension) {
        return BackService.teleportToLocation(player, lastNLocation, targetDimension);
    }

}
