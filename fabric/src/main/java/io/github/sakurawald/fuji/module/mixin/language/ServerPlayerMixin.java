package io.github.sakurawald.fuji.module.mixin.language;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

#if MC_VER <= MC_1_20_1
#elif MC_VER > MC_1_20_1
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
#endif

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin {

    #if MC_VER <= MC_1_20_1
    // The client only sends its language code to the server after MC 1.20.1 version.
    #elif MC_VER > MC_1_20_1
    @Inject(method = "setClientOptions", at = @At("HEAD"))
    public void putClientSideLanguage(@NotNull SyncedClientOptions clientInformation, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        TextHelper.setClientSideLanguageCode(player.getGameProfile().getName(), clientInformation.comp_1951());
    }
    #endif

}
