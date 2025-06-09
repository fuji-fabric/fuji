package io.github.sakurawald.module.initializer.teleport_warmup;

import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.core.manager.Managers;
import io.github.sakurawald.core.manager.impl.bossbar.BossBarTicket;
import io.github.sakurawald.core.structure.TeleportTicket;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.teleport_warmup.config.model.TeleportWarmupConfigModel;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TeleportWarmupInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<TeleportWarmupConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, TeleportWarmupConfigModel.class);

    public static @Nullable TeleportTicket getTeleportTicket(@NotNull ServerPlayerEntity player) {
        Optional<BossBarTicket> optValue = Managers.getBossBarManager().getTickets()
            .stream()
            .filter(it ->
                it instanceof TeleportTicket teleportTicket
                    && teleportTicket.getPlayer().equals(player))
            .findFirst();

        return (TeleportTicket) optValue.orElse(null);
    }
}
