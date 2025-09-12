package io.github.sakurawald.fuji.module.mixin.afk;

import io.github.sakurawald.fuji.module.initializer.afk.service.AfkService;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Unique
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @Inject(method = "updateLastActionTime", at = @At("TAIL"))
    public void $updateLastActionTime(CallbackInfo ci) {
        AfkService.countAction(player);
    }

}
