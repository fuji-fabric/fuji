package mod.fuji.module.mixin.core.event;

import mod.fuji.annotation.PhasedMixinTemplate;
import mod.fuji.auxiliary.WeaverUtil;
import mod.fuji.core.event.EventManager;
import mod.fuji.core.event.annotation.EventProducer;
import mod.fuji.core.event.message.player.PlayerTeleportPreEvent;
import java.util.Set;
import mod.fuji.core.structure.RelativeFlagsWrapper;
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
    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void $producePlayerPreTeleportEvent(ServerLevel serverWorld, double d, double e, double f, float g, float h, CallbackInfo ci)
    {
        producePlayerPreTeleportEvent(serverWorld, d, e, f, g, h, RelativeFlagsWrapper.empty(), ci);
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z", at = @At("HEAD"), cancellable = true)
    private void $producePlayerPreTeleportEvent(ServerLevel serverWorld, double d, double e, double f, Set<net.minecraft.world.entity.RelativeMovement> set, float g, float h, CallbackInfoReturnable<Boolean> cir) {
        producePlayerPreTeleportEvent(serverWorld, d, e, f, g, h, RelativeFlagsWrapper.of(set), cir);
    }

    #elif MC_VER > MC_1_21

    /** This injector doesn't cover the teleportation initialized by portal entities.
     * If some other mods call the teleport(TeleportTransition teleportTransition) method directly, then this injector will not cover it.
     * It's better to ignore these kinds of teleportation.
     **/
    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z", at = @At("HEAD"), cancellable = true)
    private void $producePlayerPreTeleportEvent(ServerLevel serverWorld, double d, double e, double f, Set<net.minecraft.world.entity.Relative> set, float g, float h, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        producePlayerPreTeleportEvent(serverWorld, d, e, f, g, h, RelativeFlagsWrapper.of(set), cir);
    }
    #endif


    @EventProducer(PlayerTeleportPreEvent.class)
    @Unique
    private void producePlayerPreTeleportEvent(ServerLevel serverWorld, double d, double e, double f, float g, float h, RelativeFlagsWrapper flags, CallbackInfo ci) {
        final ServerPlayer player = (ServerPlayer) (Object) this;
        PlayerTeleportPreEvent event = new PlayerTeleportPreEvent(ci, player, serverWorld, d, e, f, g, h, flags);
        EventManager.dispatchEvent(PlayerTeleportPreEvent.class, event, WeaverUtil.TOKEN_PLACEHOLDER);
    }

}
