package io.github.sakurawald.fuji.module.initializer.teleport_warmup;

import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.bossbar.BossBarTicket;
import io.github.sakurawald.fuji.core.structure.TeleportTicket;
import io.github.sakurawald.fuji.core.document.descriptor.MetaDescriptor;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.teleport_warmup.config.model.TeleportWarmupConfigModel;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Document(id = 1751826791752L, value = """
    Adds a warmup cooldown before player teleportation.
    """)
@ColorBox(id = 1751980526151L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ How it works?
    Inside the vanilla Minecraft, there is a teleport function for teleportation.
    We listen on this teleport function, and wrap it with a warmup cooldown.

    ◉ How can I specify different cooldown time for different commands?
    You need to use `command_warmup` module.
    The `command_cooldown` module will simply treats `all` the teleportation request the same.
    That's because we only know there is a teleport request.
    But we didn't even know who creates this teleport request.
    So we have to treat `all` teleportation the same.

    ◉ What's the point of teleport warmup.
    The main purpose is to prevent the `abuse` of `teleport commands` in vanilla Minecraft.
    Like, use teleport commands to escape death.
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
