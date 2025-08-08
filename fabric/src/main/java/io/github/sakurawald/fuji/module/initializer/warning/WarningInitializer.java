package io.github.sakurawald.fuji.module.initializer.warning;


import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.Duration;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.core.event.impl.PlayerEvents;
import io.github.sakurawald.fuji.core.service.date_parser.DateParser;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.warning.config.model.WarningConfigModel;
import io.github.sakurawald.fuji.module.initializer.warning.config.model.WarningDataModel;
import io.github.sakurawald.fuji.module.initializer.warning.gui.WarningGui;
import io.github.sakurawald.fuji.module.initializer.warning.service.WarningService;
import io.github.sakurawald.fuji.module.initializer.warning.structure.PlayerWarnings;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Document(id = 1751827033037L, value = """
    This module provides the `information management` for `staffs`.
    A shared `warning GUI` to `track` the behaviours of players, between `staffs`.
    You can `create` a `warning` for a `player`.
    All `staffs` can `view` the `warnings` of a `player`.

    You can use `warnings` to `track` the behaviours of a `player`.
    """)
@ColorBox(id = 1751870593979L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    You can use `command_scheduler` module, to define a `job`.
    To execute `/warning clear-all --confirm true` command automatically. (e.g. every week)
    """)
@ColorBox(id = 1751870597904L, color = ColorBox.ColorBlockTypes.TIPS, value = """
    You can use `command_bundle` module, to define `template` for `warnings`.
    """)
@ColorBox(id = 1754643107428L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ Create a `permanent warning` for a player.
    Issue: `/warning create Steve Steal items.`

    ◉ Create a `temporal warning` for a player.
    Issue: `/warning create-temp Steve 3d Steal items.`

    ◉ List all the warnings of a player.
    Issue: `/warning list Steve`

    ◉ Open the `Warning GUI`.
    Issue: `/warning gui`
    """)
public class WarningInitializer extends ModuleInitializer {

    @DocStringProvider(id = 1752000385223L, value = "To `create` a new `warning` for a `player`.")
    public static PermissionDescriptor CREATE_WARNINGS_PERMISSION = new PermissionDescriptor("fuji.warning.create", 1752000385223L);

    @DocStringProvider(id = 1752000399374L, value = "To `read` the `warnings` of a `player`.")
    public static PermissionDescriptor READ_WARNINGS_PERMISSION = new PermissionDescriptor("fuji.warning.read", 1752000399374L);

    @DocStringProvider(id = 1752000453811L, value = "To `update` the `warnings` of a `player`.")
    public static PermissionDescriptor UPDATE_WARNINGS_PERMISSION = new PermissionDescriptor("fuji.warning.update", 1752000453811L);

    @DocStringProvider(id = 1752000473509L, value = "To `delete` an existed `warning` of a `player`.")
    public static PermissionDescriptor DELETE_WARNINGS_PERMISSION = new PermissionDescriptor("fuji.warning.delete", 1752000473509L);

    @DocStringProvider(id = 1752000488085L, value = "When a `player` with `warnings` join/leave the server, you will get notified.")
    public static PermissionDescriptor NOTIFY_WARNINGS_PERMISSION = new PermissionDescriptor("fuji.warning.notify", 1752000488085L);

    public static final BaseConfigurationHandler<WarningConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, WarningConfigModel.class);

    public static final BaseConfigurationHandler<WarningDataModel> data = new ObjectConfigurationHandler<>("warning-data.json", WarningDataModel.class);

    @Document(id = 1751827034962L, value = "Open the warning GUI.")
    @CommandNode("warning")
    @CommandRequirement(level = 4)
    private static int $warningRoot(@CommandSource ServerPlayerEntity player) {
        $warningGui(player);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751827036716L, value = "Open the warning GUI.")
    @CommandNode("warning gui")
    @CommandRequirement(level = 4)
    private static int $warningGui(@CommandSource ServerPlayerEntity player) {
        List<String> offlinePlayerNames = PlayerHelper.Cache.getOfflinePlayerNames();
        new WarningGui(null, player, offlinePlayerNames, 0)
            .open();
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751827038512L, value = "Create a new warning for the player.")
    @CommandNode("warning create")
    @CommandRequirement(level = 4)
    private static int $createWarning(@CommandSource ServerCommandSource source, OfflinePlayerName targetPlayer, GreedyString warning) {
        return $createTemporalWarning(source, targetPlayer, new Duration(null), warning);
    }

    @Document(id = 1754620576300L, value = "Create a new warning with expiration for the player.")
    @CommandNode("warning create-temp")
    @CommandRequirement(level = 4)
    private static int $createTemporalWarning(@CommandSource ServerCommandSource source, OfflinePlayerName targetPlayer, Duration duration, GreedyString warning) {
        String creatorName = source.getName();
        String targetPlayerName = targetPlayer.getValue();
        String warningDescription = warning.getValue();

        Long expirationTimestamp = DateParser.parseIntoExpirationTimestamp(duration.getValue());
        String expirationDate = ChronosUtil.Formatter.formatDate(expirationTimestamp);

        WarningService.createWarning(creatorName, targetPlayerName, warningDescription, expirationTimestamp);
        TextHelper.sendTextByKey(source, "warning.created", targetPlayerName, expirationDate);
        return CommandHelper.Return.SUCCESS;
    }


    @Document(id = 1751827040456L, value = "List the warnings of a player.")
    @CommandNode("warning list")
    @CommandRequirement(level = 4)
    private static int $listWarning(@CommandSource ServerCommandSource source, OfflinePlayerName targetPlayer) {
        String targetPlayerName = targetPlayer.getValue();
        PlayerWarnings playerWarnings = WarningService.getPlayerWarnings(targetPlayerName);
        TextHelper.sendTextByKey(source, "warning.list.message", targetPlayerName, playerWarnings.getWarnings().size());

        playerWarnings.getWarnings().forEach(warning -> {
            warning
                .asLore(source)
                .forEach(source::sendMessage);

            source.sendMessage(TextHelper.TEXT_EMPTY);
        });

        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1751827043371L, value = "Clear the warnings of a player.")
    @CommandNode("warning clear")
    @CommandRequirement(level = 4)
    private static int $clearWarning(@CommandSource ServerCommandSource source, OfflinePlayerName targetPlayer, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(source, confirm, () -> {
            String targetPlayerName = targetPlayer.getValue();
            int originalSize = WarningService.clearWarnings(targetPlayerName);

            TextHelper.sendTextByKey(source, "warning.clear", originalSize, targetPlayerName);
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1751827045167L, value = "Clear all warnings for all players.")
    @CommandNode("warning clear-all")
    @CommandRequirement(level = 4)
    private static int $clearAllWarnings(@CommandSource ServerCommandSource source, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(source, confirm, () -> {
            WarningService.clearAllWarnings();

            TextHelper.sendTextByKey(source, "warning.clear_all");
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Override
    protected void onInitialize() {
        PlayerEvents.ON_PLAYER_JOINED.register(player -> WarningService.processNotify(player, true));
        PlayerEvents.ON_PLAYER_LEAVE.register(player -> WarningService.processNotify(player, false));
    }

    @Override
    protected void registerPlaceholders() {
        WarningPlaceholders.registerLastWarningCreatedDatePlaceholder();
        WarningPlaceholders.registerLastWarningCreatorNamePlaceholder();
        WarningPlaceholders.registerLastWarningExpirationDatePlaceholder();
        WarningPlaceholders.registerLastWarningReasonPlaceholder();
    }
}
