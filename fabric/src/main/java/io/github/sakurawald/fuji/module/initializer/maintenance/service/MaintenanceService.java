package io.github.sakurawald.fuji.module.initializer.maintenance.service;

import io.github.sakurawald.fuji.core.auxiliary.RandomUtil;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.executor.CommandExecutor;
import io.github.sakurawald.fuji.core.command.executor.structure.ExtendedCommandSource;
import io.github.sakurawald.fuji.module.initializer.maintenance.MaintenanceModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.maintenance.config.model.MaintenanceConfigModel;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class MaintenanceService {

    public static boolean getMaintenanceModeStatus() {
        return MaintenanceModuleInitializer.config.model().isMaintenanceModeStatus();
    }

    @SuppressWarnings({"RedundantIfStatement", "BooleanMethodIsAlwaysInverted"})
    public static boolean canJoinNow(@NotNull ServerPlayerEntity player) {
        if (!getMaintenanceModeStatus()) return true;
        if (CommandHelper.Requirement.isOperator(player)) return true;
        if (CommandHelper.Requirement.isAdmin(player.getCommandSource())) return true;
        if (LuckpermsHelper.hasPermission(player.getUuid(), MaintenanceModuleInitializer.MAINTENANCE_BYPASS_PERMISSION)) return true;

        return false;
    }

    public static void processMaintenanceModeOnPlayerJoined(@NotNull ServerPlayerEntity player) {
        if (!canJoinNow(player)) {
            kickPlayer(player);
        }
    }

    public static void kickPlayer(@NotNull ServerPlayerEntity player) {
        Text reasonText = TextHelper.getTextByKey(player, "maintenance.disconnect");
        PlayerHelper.disconnectPlayer(player, reasonText);
    }

    public static void setMaintenanceModeStatus(boolean status) {
        MaintenanceModuleInitializer.config.model().setMaintenanceModeStatus(status);
        MaintenanceModuleInitializer.config.writeStorage();
        processMaintenanceModeEvents();
    }

    private static void processMaintenanceModeEvents() {
        /* Compute commands. */
        boolean currentStatus = getMaintenanceModeStatus();
        MaintenanceConfigModel.Events events = MaintenanceModuleInitializer.config.model().getEvents();
        List<String> commands;
        if (currentStatus) {
            commands = events.getOnEnterMaintenanceModeCommands();
        } else {
            commands = events.getOnLeaveMaintenanceModeCommands();
        }

        /* Execute commands. */
        ExtendedCommandSource extendedCommandSource = ExtendedCommandSource.asConsole(ServerHelper.getServer().getCommandSource());
        CommandExecutor.executeBatch(extendedCommandSource, commands);
    }

    public static Text getEffectiveMaintenanceMessageText() {
        String message = RandomUtil.drawList(MaintenanceModuleInitializer.config.model().getMaintenanceMessages());
        return TextHelper.getTextByValue(null, message);
    }

}
