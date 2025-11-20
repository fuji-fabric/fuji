package mod.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.PlayerTeleportPreEvent;
import java.util.Set;
import net.minecraft.world.entity.Relative;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@PhasedMixinTemplate
@Mixin(value = ServerPlayer.class)
public abstract class PlayerTeleportPreEventMixin {

    #if MC_VER <= MC_1_21
    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void $producePlayerPreTeleportEvent(ServerWorld serverWorld, double d, double e, double f, float g, float h, CallbackInfo ci)
    {
        producePlayerPreTeleportEvent(serverWorld, d, e, f, g, h, Set.of(), ci);
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z", at = @At("HEAD"), cancellable = true)
    private void $producePlayerPreTeleportEvent(ServerWorld serverWorld, double d, double e, double f, Set<PositionFlag> set, float g, float h, CallbackInfoReturnable<Boolean> cir) {
        producePlayerPreTeleportEvent(serverWorld, d, e, f, g, h, Set.of(), cir);
    }

    #elif MC_VER > MC_1_21

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z", at = @At("HEAD"), cancellable = true)
    private void $producePlayerPreTeleportEvent(ServerLevel serverWorld, double d, double e, double f, Set<Relative> set, float g, float h, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        producePlayerPreTeleportEvent(serverWorld, d, e, f, g, h, set, cir);
    }
    #endif

    @EventProducer(PlayerTeleportPreEvent.class)
    @Unique
    private void producePlayerPreTeleportEvent(ServerLevel serverWorld, double d, double e, double f, float g, float h, Set<Relative> set, CallbackInfo ci) {
        final ServerPlayer player = (ServerPlayer) (Object) this;
        PlayerTeleportPreEvent event = new PlayerTeleportPreEvent(ci, player, serverWorld, d, e, f, g, h, set);
        EventManager.dispatchEvent(PlayerTeleportPreEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }


}
