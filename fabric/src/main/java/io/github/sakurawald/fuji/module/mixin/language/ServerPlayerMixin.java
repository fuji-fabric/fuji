package io.github.sakurawald.fuji.module.mixin.language;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin {

    #if MC_VER <= MC_1_20_1
    @Inject(method = "setClientSettings", at = @At("HEAD"))
    void putClientSideLanguage(net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket clientSettingsC2SPacket, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        String playerName = PlayerHelper.getPlayerName(player);
        String languageCode = clientSettingsC2SPacket.language();
        TextHelper.Loader.setPlayerLanguageCode(playerName, languageCode);
    }
    #elif MC_VER > MC_1_20_1
    @Inject(method = "setClientOptions", at = @At("HEAD"))
    void putClientSideLanguage(net.minecraft.network.packet.c2s.common.SyncedClientOptions clientInformation, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        String playerName = PlayerHelper.getPlayerName(player);
        String languageCode =  clientInformation.comp_1951();
        TextHelper.Loader.setPlayerLanguageCode(playerName, languageCode);
    }
    #endif

}
