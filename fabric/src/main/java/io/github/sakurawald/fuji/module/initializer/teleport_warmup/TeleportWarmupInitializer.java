package io.github.sakurawald.fuji.module.initializer.teleport_warmup;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.EntityHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.DocStringProvider;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.document.descriptor.MetaDescriptor;
import io.github.sakurawald.fuji.core.document.descriptor.PermissionDescriptor;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.message.player.PlayerTeleportPreEvent;
import io.github.sakurawald.fuji.core.manager.impl.bossbar.BossBarManager;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.core.structure.TeleportTicket;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.teleport_warmup.config.model.TeleportWarmupConfigModel;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

@Document(id = 1751826791752L, value = """
    Adds a warmup cooldown before player teleportation.
    """)
@ColorBox(id = 1751980526151L, color = ColorBox.ColorBoxTypes.NOTE, value = """
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
    <green>NOTE: The `admin players` can bypass the teleport warmup.
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

    public static final BaseConfigurationHandler<TeleportWarmupConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, TeleportWarmupConfigModel.class);

    @SuppressWarnings("RedundantIfStatement")
    public static boolean shouldApplyTeleportWarmup(ServerWorld destinationDimension, ServerPlayerEntity player) {
        /* Skip the teleport warmup if target dimension is not in the list of effective dimensions */
        if (!config.model().dimension.effective_dimensions.contains(RegistryHelper.getIdAsString(destinationDimension))) {
            return false;
        }

        // NOTE: Make it friendly for respawn teleport. (For an immediate respawn teleport, the age == 0)
        if (EntityHelper.getAge(player) <= 3) {
            return false;
        }

        /* Skip the teleport warmup for admin players. */
        if (CommandHelper.Requirement.isAdmin(player)) {
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

    @EventConsumer(injectorPriority = EventConsumer.LOWEST, consumerPriority =EventConsumer.LOWEST)
    @SuppressWarnings("UnnecessaryReturnStatement")
    private static void handlePlayerPreTeleportEvent(PlayerTeleportPreEvent event) {
        if (event.getCallbackInfo().isCancelled()) return;

        ServerPlayerEntity player = event.getPlayer();
        ServerWorld destinationDimension = event.getDestinationDimension();

        if (!TeleportWarmupInitializer.shouldApplyTeleportWarmup(destinationDimension, player)) {
            return;
        }

        /* Add a new ticket if none exists. */
        Optional<TeleportTicket> existingTeleportTicket = BossBarManager.findBossbarTicket(TeleportTicket.class, player);
        if (existingTeleportTicket.isEmpty()) {

            //set warmup seconds to LP permission seconds or default config seconds
            int warmupDurationMs = (int) (TeleportWarmupInitializer.getWarmupSeconds(player) * 1000);

            TeleportTicket teleportTicket = TeleportTicket.make(
                player
                , GlobalPos.of(player)
                , new GlobalPos(destinationDimension, event.getDestinationX(), event.getDestinationY(), event.getDestinationZ(), event.getDestinationYaw(), event.getDestinationPitch())
                , warmupDurationMs
                , TeleportWarmupInitializer.config.model().interruptible
                , event.getPositionFlags()
            );
            BossBarManager.addTicket(teleportTicket);
            event.getCallbackInfo().cancel();
            return;
        }

        if (!existingTeleportTicket.get().isCompleted()) {
            TextHelper.sendTextByKey(player, "teleport_warmup.another_teleportation_in_progress");
            event.getCallbackInfo().cancel();
            return;
        }

        // Let this teleport proceed.
    }
}
