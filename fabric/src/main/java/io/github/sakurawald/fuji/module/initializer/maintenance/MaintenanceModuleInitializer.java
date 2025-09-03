package io.github.sakurawald.fuji.module.initializer.maintenance;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.impl.PlayerEvents;
import io.github.sakurawald.fuji.core.event.impl.on_demand.QueryServerMetadataEvent;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.maintenance.config.model.MaintenanceConfigModel;
import io.github.sakurawald.fuji.module.initializer.maintenance.service.MaintenanceService;
import java.util.Optional;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;


@Document(id = 1756285767531L, value = """
    This module provides a `maintenance mode`, to prevent players joining the server during maintenance.
    """)
public class MaintenanceModuleInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<MaintenanceConfigModel> config = ObjectConfigurationHandler
        .ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, MaintenanceConfigModel.class);

    @DocStringProvider(id = 1756286747383L, value = """
        Joins the server while the server is during `maintenance mode`.
        """)
    public static final PermissionDescriptor MAINTENANCE_BYPASS_PERMISSION = new PermissionDescriptor("fuji.maintenance.bypass", 1756286747383L);

    @CommandNode("maintenance on")
    @CommandRequirement(level = 4)
    private static int $on(@CommandSource ServerCommandSource source) {
        MaintenanceService.setMaintenanceModeStatus(true);
        TextHelper.sendTextByKey(source, "maintenance.on");
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("maintenance off")
    @CommandRequirement(level = 4)
    private static int $off(@CommandSource ServerCommandSource source) {
        MaintenanceService.setMaintenanceModeStatus(false);
        TextHelper.sendTextByKey(source, "maintenance.off");
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("maintenance kick-all")
    @CommandRequirement(level = 4)
    private static int $kickAll(@CommandSource ServerCommandSource source) {
        PlayerHelper.Lookup.getOnlinePlayers()
            .forEach(player -> {
                if (!MaintenanceService.canJoinNow(player)) {
                    MaintenanceService.kickPlayer(player);
                }
            });
        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void onInitialize() {
        PlayerEvents.ON_PLAYER_JOINED.register(MaintenanceService::processMaintenanceModeOnPlayerJoined);
    }

    @EventConsumer(priority = 2000)
    private static void modifyMaintenanceMotd(QueryServerMetadataEvent event) {
        if (!MaintenanceService.getMaintenanceModeStatus()) {
            return;
        }

        ServerMetadata original = event.getServerMetadata();
        Text text = MaintenanceService.getEffectiveMaintenanceMessageText();
        Optional<ServerMetadata.Players> players = original.comp_1274();
        Optional<ServerMetadata.Version> version = original.comp_1275();
        Optional<ServerMetadata.Favicon> icon = original.comp_1276();
        ServerMetadata newValue = new ServerMetadata(text, players, version, icon, original.secureChatEnforced());
        event.setServerMetadata(newValue);
    }

}
