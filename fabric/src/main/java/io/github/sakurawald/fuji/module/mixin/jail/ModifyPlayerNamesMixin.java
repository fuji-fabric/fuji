package io.github.sakurawald.fuji.module.mixin.jail;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.jail.JailInitializer;
import io.github.sakurawald.fuji.module.initializer.jail.service.JailService;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ServerPlayerEntity.class, priority = 1000 + 1000)
public class ModifyPlayerNamesMixin {

    @ModifyReturnValue(method = "getPlayerListName", at = @At("RETURN"))
    Text modifyPlayerListName(Text original) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        String playerName = PlayerHelper.getPlayerName(player);
        return JailService.getActiveJailRecord(playerName)
            .map(it -> TextHelper.getTextByValue(player, JailInitializer.config.model().getJailedPlayerTabListText()))
            .orElse(original);
    }

}
