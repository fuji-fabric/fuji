package io.github.sakurawald.fuji.module.initializer.teleport_warmup;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.descriptor.MetaDescriptor;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.bossbar.BossBarTicket;
import io.github.sakurawald.fuji.core.structure.TeleportTicket;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.teleport_warmup.config.model.TeleportWarmupConfigModel;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

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

    ◉ What's the purpose of teleport warmup.
    The main purpose is to prevent the `abuse` of `teleport commands` in vanilla Minecraft.
    Like, use teleport commands to escape death.
    """)
public class TeleportWarmupInitializer extends ModuleInitializer {

    @DocStringProvider(id = 1752000321033L, value = """
        To bypass all teleport warmup.
        """)
    public static final PermissionDescriptor TELEPORT_WARMUP_BYPASS_PERMISSION = new PermissionDescriptor("fuji.teleport_warmup.bypass", 1752000321033L);

    @DocStringProvider(id = 1752000334206L, value = """
        Specify the teleport warmup time in seconds for this player.
        """)
    public static final MetaDescriptor<Double> TELEPORT_WARMUP_TIME_META = new MetaDescriptor<>("fuji.teleport_warmup.warmup", Double::valueOf, 1752000334206L);

    public static final BaseConfigurationHandler<TeleportWarmupConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, TeleportWarmupConfigModel.class);

    public static Optional<BossBarTicket> getExistingTeleportTicket(@NotNull ServerPlayerEntity player) {
        return Managers.getBossBarManager()
            .getTickets()
            .stream()
            .filter(it -> it instanceof TeleportTicket teleportTicket
                    && teleportTicket.getPlayer().equals(player))
            .findFirst();
    }

    public static boolean shouldApplyTeleportWarmup(ServerWorld destinationDimension, ServerPlayerEntity player) {
        /* Skip the teleport warmup if target dimension is not in the list of effective dimensions */
        if (!config.model().dimension.effective_dimensions.contains(RegistryHelper.toIdString(destinationDimension))) {
            return false;
        }

        /* Skip the teleport warmup if the player is a fake-player. */
        // NOTE: For carpet mod, if you use `/player Alice spawn` to spawn a fake-player. It will initially be spawned in minecraft:overworld.
        // And then it will be teleported to the target dimension.
        if (!PlayerHelper.isRealPlayer(player)) {
            return false;
        }

        /* Skip the teleport warmup if the player has the bypass permission. */
        if (LuckpermsHelper.hasPermission(player.getUuid(), TELEPORT_WARMUP_BYPASS_PERMISSION)) {
            return false;
        }

        return true;
    }

    public static double getWarmupSeconds(ServerPlayerEntity player) {
        Optional<Double> warmupSecondsSpecifiedByMeta = LuckpermsHelper.getMeta(player.getUuid(), TELEPORT_WARMUP_TIME_META);
        return warmupSecondsSpecifiedByMeta
            .orElse(config.model().warmup_second);
    }
}
