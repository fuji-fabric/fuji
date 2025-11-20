package mod.fuji.module.initializer.maintenance;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.descriptor.PermissionDescriptor;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.server.metadata.ModifyServerMetadataEvent;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.maintenance.config.model.MaintenanceConfigModel;
import mod.fuji.module.initializer.maintenance.service.MaintenanceService;
import java.util.Optional;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;


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
    private static int $on(@CommandSource CommandSourceStack source) {
        MaintenanceService.setMaintenanceModeStatus(true);
        TextHelper.sendTextByKey(source, "maintenance.on");
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("maintenance off")
    @CommandRequirement(level = 4)
    private static int $off(@CommandSource CommandSourceStack source) {
        MaintenanceService.setMaintenanceModeStatus(false);
        TextHelper.sendTextByKey(source, "maintenance.off");
        return CommandHelper.Return.SUCCESS;
    }

    @CommandNode("maintenance kick-all")
    @CommandRequirement(level = 4)
    private static int $kickAll(@CommandSource CommandSourceStack source) {
        PlayerHelper.Lookup.getOnlinePlayers()
            .forEach(player -> {
                if (!MaintenanceService.canJoinNow(player)) {
                    MaintenanceService.kickPlayer(player);
                }
            });
        return CommandHelper.Return.SUCCESS;
    }

    @EventConsumer(consumerPriority = 2000)
    private static void modifyMaintenanceMotd(ModifyServerMetadataEvent event) {
        if (!MaintenanceService.getMaintenanceModeStatus()) {
            return;
        }

        ServerStatus original = event.getServerMetadata();
        Component text = MaintenanceService.getEffectiveMaintenanceMessageText();
        Optional<ServerStatus.Players> players = original.players();
        Optional<ServerStatus.Version> version = original.version();
        Optional<ServerStatus.Favicon> icon = original.favicon();
        ServerStatus newValue = new ServerStatus(text, players, version, icon, original.enforcesSecureChat());
        event.setServerMetadata(newValue);
    }

}
