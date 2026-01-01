package mod.fuji.module.mixin.afk;

import mod.fuji.module.initializer.afk.service.AfkService;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

    @Unique
    private final ServerPlayer player = (ServerPlayer) (Object) this;

    @Inject(method = "resetLastActionTime", at = @At("TAIL"))
    public void resetLastActionTime(CallbackInfo ci) {
        AfkService.receiveAction(player);
    }

}
