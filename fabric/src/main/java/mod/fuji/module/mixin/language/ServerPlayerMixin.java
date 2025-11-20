package mod.fuji.module.mixin.language;

import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerPlayer.class)
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
    @Inject(method = "updateOptions", at = @At("HEAD"))
    void putClientSideLanguage(net.minecraft.server.level.ClientInformation clientInformation, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        String playerName = PlayerHelper.getPlayerName(player);
        String languageCode =  clientInformation.language();
        TextHelper.Loader.setPlayerLanguageCode(playerName, languageCode);
    }
    #endif

}
