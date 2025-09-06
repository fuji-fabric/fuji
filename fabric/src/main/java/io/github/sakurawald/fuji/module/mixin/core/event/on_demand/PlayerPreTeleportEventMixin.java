package io.github.sakurawald.fuji.module.mixin.core.event.on_demand;

import io.github.sakurawald.annotation.PhasedMixinTemplate;
import io.github.sakurawald.auxiliary.WeaverUtil;
import io.github.sakurawald.fuji.core.event.EventManager;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.message.impl.on_demand.PlayerPreTeleportEvent;
import java.util.Set;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(value = ServerPlayerEntity.class)
public abstract class PlayerPreTeleportEventMixin {

    #if MC_VER <= MC_1_21
    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V", at = @At("HEAD"))
    private void $producePlayerPreTeleportEvent(ServerWorld serverWorld, double d, double e, double f, float g, float h, CallbackInfo ci)
    {
        producePlayerPreTeleportEvent(serverWorld, d, e, f, g, h, Set.of(), ci);
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z", at = @At("HEAD"))
    private void $producePlayerPreTeleportEvent(ServerWorld serverWorld, double d, double e, double f, Set<PositionFlag> set, float g, float h, CallbackInfoReturnable<Boolean> cir) {
        producePlayerPreTeleportEvent(serverWorld, d, e, f, g, h, Set.of(), cir);
    }

    #elif MC_VER > MC_1_21

    @Inject(method = "teleport", at = @At("HEAD"), cancellable = true)
    private void $producePlayerPreTeleportEvent(ServerWorld serverWorld, double d, double e, double f, Set<PositionFlag> set, float g, float h, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        producePlayerPreTeleportEvent(serverWorld, d, e, f, g, h, set, cir);
    }
    #endif

    @EventProducer(PlayerPreTeleportEvent.class)
    @Unique
    private void producePlayerPreTeleportEvent(ServerWorld serverWorld, double d, double e, double f, float g, float h, Set<PositionFlag> set, CallbackInfo ci) {
        final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        PlayerPreTeleportEvent event = new PlayerPreTeleportEvent(ci, player, serverWorld, d, e, f, g, h, set);
        EventManager.dispatchEvent(PlayerPreTeleportEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }


}
