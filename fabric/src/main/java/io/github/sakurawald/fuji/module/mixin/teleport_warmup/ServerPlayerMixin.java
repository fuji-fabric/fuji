package io.github.sakurawald.fuji.module.mixin.teleport_warmup;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.core.manager.impl.bossbar.BossBarTicket;
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

    @SuppressWarnings({"CancellableInjectionUsage", "UnnecessaryReturnStatement"})
    #if MC_VER <= MC_1_21
    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z", at = @At("HEAD"), cancellable = true)
    void interceptTeleportAndAddTicket(ServerWorld destinationDimension, double x, double y, double z, Set<PositionFlag> set, float yaw, float pitch, CallbackInfoReturnable<Boolean> cir)
    #elif MC_VER > MC_1_21
    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    void interceptTeleportAndAddTicket(ServerWorld destinationDimension, double x, double y, double z, Set<PositionFlag> set, float yaw, float pitch, boolean bl, CallbackInfoReturnable<Boolean> cir)
    #endif
    {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (!TeleportWarmupInitializer.shouldApplyTeleportWarmup(destinationDimension, player)) {
            return;
        }

        /* Add a new ticket if none exists. */
        Optional<BossBarTicket> existingTeleportTicket = TeleportWarmupInitializer.getExistingTeleportTicket(player);
        if (existingTeleportTicket.isEmpty()) {

            //set warmup seconds to LP permission seconds or default config seconds
            int warmupDurationMs = (int) (TeleportWarmupInitializer.getWarmupSeconds(player) * 1000);

            TeleportTicket teleportTicket = TeleportTicket.make(
                player
                , GlobalPos.of(player)
                , new GlobalPos(destinationDimension, x, y, z, yaw, pitch)
                , warmupDurationMs
                , TeleportWarmupInitializer.config.model().interruptible
                , set
            );
            Managers.getBossBarManager().addTicket(teleportTicket);
            cir.cancel();
            return;
        }

        if (!existingTeleportTicket.get().isCompleted()) {
            TextHelper.sendTextByKey(player, "teleport_warmup.another_teleportation_in_progress");
            cir.cancel();
            return;
        }

        // Let this teleport proceed.
    }

}
