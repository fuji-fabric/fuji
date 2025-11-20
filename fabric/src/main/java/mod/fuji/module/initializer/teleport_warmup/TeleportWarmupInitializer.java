package mod.fuji.module.initializer.teleport_warmup;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.EntityHelper;
import mod.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.RegistryHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.document.descriptor.MetaDescriptor;
import mod.fuji.core.document.descriptor.PermissionDescriptor;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerTeleportPreEvent;
import mod.fuji.core.service.bossbar.BossBarManager;
import mod.fuji.core.structure.GlobalPos;
import mod.fuji.core.structure.TeleportTicket;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.teleport_warmup.config.model.TeleportWarmupConfigModel;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

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
@ColorBox(id = 1758083431300L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ The `teleport warmup` will NOT be applied if...
    1. The target dimension is not defined in the `effective dimensions` list.
    2. The target player's `age <= 3`
    3. The target player is a `fake player`.
    4. The target player has the `warmup bypass permission`.
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
    public static boolean shouldApplyTeleportWarmup(ServerLevel destinationDimension, ServerPlayer player) {
        /* Skip the teleport warmup if target dimension is not in the list of effective dimensions */
        if (!config.model().dimension.effective_dimensions.contains(RegistryHelper.getIdAsString(destinationDimension))) {
            return false;
        }

        // NOTE: Make it friendly for respawn teleport. (For an immediate respawn teleport, the age == 0)
        if (EntityHelper.getAge(player) <= 3) {
            return false;
        }

        /* Skip the teleport warmup for admin players. */
        if (config.model().admin_players_can_bypass && CommandHelper.Requirement.isAdmin(player)) {
            return false;
        }

        /* Skip the teleport warmup if the player is a fake-player. */
        // NOTE: For carpet mod, if you use `/player Alice spawn` to spawn a fake-player. It will initially be spawned in minecraft:overworld.
        // And then it will be teleported to the target dimension.
        if (!PlayerHelper.Kind.isRealPlayer(player)) {
            return false;
        }

        /* Skip the teleport warmup if the player has the bypass permission. */
        if (LuckpermsHelper.hasPermission(player.getUUID(), TELEPORT_WARMUP_BYPASS_PERMISSION)) {
            return false;
        }

        return true;
    }

    public static double getWarmupSeconds(ServerPlayer player) {
        Optional<Double> warmupSecondsSpecifiedByMeta = LuckpermsHelper.getMeta(player.getUUID(), TELEPORT_WARMUP_TIME_META);
        return warmupSecondsSpecifiedByMeta
            .orElse(config.model().warmup_second);
    }

    @EventConsumer(injectorPriority = EventConsumer.LOWEST, consumerPriority =EventConsumer.LOWEST)
    @SuppressWarnings("UnnecessaryReturnStatement")
    private static void handlePlayerPreTeleportEvent(PlayerTeleportPreEvent event) {
        if (event.getCallbackInfo().isCancelled()) return;

        ServerPlayer player = event.getPlayer();
        ServerLevel destinationDimension = event.getDestinationDimension();

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
