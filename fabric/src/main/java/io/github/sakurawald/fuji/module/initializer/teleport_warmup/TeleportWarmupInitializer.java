package io.github.sakurawald.fuji.module.initializer.teleport_warmup;

import io.github.sakurawald.fuji.core.annotation.Document;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.bossbar.BossBarTicket;
import io.github.sakurawald.fuji.core.structure.TeleportTicket;
import io.github.sakurawald.fuji.core.structure.descriptor.MetaDescriptor;
import io.github.sakurawald.fuji.core.structure.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.teleport_warmup.config.model.TeleportWarmupConfigModel;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Document("""
    Adds a warmup cooldown before player teleportation.
    """)
public class TeleportWarmupInitializer extends ModuleInitializer {

    public static final PermissionDescriptor TELEPORT_WARMUP_BYPASS_PERMISSION = new PermissionDescriptor("fuji.teleport_warmup.bypass", """
        To bypass all teleport warmup.
        """);

    public static final MetaDescriptor<Integer> TELEPORT_WARMUP_TIME_META = new MetaDescriptor<>("fuji.teleport_warmup.warmup", Integer::valueOf, """
        Specify the teleport warmup time in seconds for this player.
        """);

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
