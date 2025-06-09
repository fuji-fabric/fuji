package io.github.sakurawald.module.mixin.command_toolbox.nickname;

import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.module.initializer.command_toolbox.nickname.NicknameInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Unique
    @NotNull
    final PlayerEntity player = (PlayerEntity) (Object) this;

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    void modifyDisplayName(@NotNull CallbackInfoReturnable<Text> cir) {
        String format = NicknameInitializer.getData().model().format.player2format.get(player.getGameProfile().getName());

        if (format != null) {
            cir.setReturnValue(TextHelper.getTextByValue(null, format));
        }
    }

}
