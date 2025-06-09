package io.github.sakurawald.module.mixin.language;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "setClientOptions", at = @At("HEAD"))
    public void putClientSideLanguage(@NotNull SyncedClientOptions clientInformation, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        TextHelper.setClientSideLanguageCode(player.getGameProfile().getName(), clientInformation.comp_1951());
    }
}
