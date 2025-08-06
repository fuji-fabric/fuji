package io.github.sakurawald.fuji.module.mixin.command_toolbox.nickname;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.module.initializer.command_toolbox.nickname.NicknameInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Unique
    @NotNull
    final PlayerEntity player = (PlayerEntity) (Object) this;

    @ModifyReturnValue(method = "getDisplayName", at = @At("RETURN"))
    Text modifyDisplayName(Text original) {
        String playerName = PlayerHelper.getPlayerName(player);
        String preferredNicknameFormat = NicknameInitializer.data.model().format.player2format.get(playerName);
        if (preferredNicknameFormat != null) {
            return TextHelper.getTextByValue(null, preferredNicknameFormat);
        }
        return original;
    }

}
