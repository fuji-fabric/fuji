package io.github.sakurawald.fuji.module.mixin.teleport_warmup;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.LuckpermsHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.structure.GlobalPos;
import io.github.sakurawald.fuji.core.structure.TeleportTicket;
import io.github.sakurawald.fuji.module.initializer.teleport_warmup.TeleportWarmupInitializer;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;

@Mixin(value = ServerPlayerEntity.class, priority = 1000 - 500)
public abstract class ServerPlayerMixin {

    @SuppressWarnings("CancellableInjectionUsage")

        #if MC_VER <= MC_1_21
    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z", at = @At("HEAD"), cancellable = true)
    public void interceptTeleportAndAddTicket(ServerWorld serverWorld, double x, double y, double z, Set<PositionFlag> set, float yaw, float pitch, CallbackInfoReturnable<Boolean> cir)
        #elif MC_VER > MC_1_21
    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    public void interceptTeleportAndAddTicket(ServerWorld serverWorld, double x, double y, double z, Set<PositionFlag> set, float yaw, float pitch, boolean bl, CallbackInfoReturnable<Boolean> cir)
        #endif
    {
        /* Skip the teleport warmup if target dimension is not inside effective dimensions */
        if (!TeleportWarmupInitializer.config.model().dimension.effective_dimensions.contains(RegistryHelper.ofString(serverWorld))) {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        /* Skip the teleport warmup if the player is a fake-player. */
        // If we try to spawn a fake-player in the end or nether, the fake-player will initially spawn in overworld
        // and teleport to the target world. This will cause the teleport warmup to be triggered.
        if (!PlayerHelper.isRealPlayer(player)) return;

        /* Skip the teleport warmup if the player has the bypass permission. */
        if (LuckpermsHelper.hasPermission(player.getUuid(), TeleportWarmupInitializer.TELEPORT_WARMUP_BYPASS_PERMISSION)) {
            return;
        }

        /* Add a new teleport ticker if no exists. */
        TeleportTicket ticket = TeleportWarmupInitializer.getTeleportTicket(player);
        if (ticket == null) {
            Optional<Integer> permission_warmup_time = LuckpermsHelper.getMeta(player.getUuid(), TeleportWarmupInitializer.TELEPORT_WARMUP_TIME_META);

            //set warmup seconds to LP permission seconds or default config seconds
            int warmup_seconds = permission_warmup_time.orElse(TeleportWarmupInitializer.config.model().warmup_second);

            ticket = TeleportTicket.make(
                player
                , GlobalPos.of(player)
                , new GlobalPos(serverWorld, x, y, z, yaw, pitch)
                , warmup_seconds * 1000
                , TeleportWarmupInitializer.config.model().interruptible
                , set
            );
            Managers.getBossBarManager().addTicket(ticket);
            cir.cancel();
        } else if (!ticket.isCompleted()) {
            TextHelper.sendActionBarByKey(player, "teleport_warmup.another_teleportation_in_progress");
            cir.cancel();
        }

        // yeah, let's do teleport now.
    }

}
