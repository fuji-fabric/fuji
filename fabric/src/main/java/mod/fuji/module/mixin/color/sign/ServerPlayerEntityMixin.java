package mod.fuji.module.mixin.color.sign;

import mod.fuji.core.structure.GlobalBlockPos;
import mod.fuji.module.initializer.color.sign.ColorSignInitializer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

    @Unique
    @NotNull
    final ServerPlayer player = (ServerPlayer) (Object) this;

    // NOTE: In lower MC versions like MC 1.20.1, if there are `<rb>` tag in the sign, then the `openEditSignScreen` method will not be called.
    @Inject(method = "openTextEdit", at = @At("HEAD"))
    private void restoreInputLineStringsOnClientSide(@NotNull SignBlockEntity signBlockEntity, boolean isFront, @NotNull CallbackInfo ci) {
        ColorSignInitializer
            .readSignCache(new GlobalBlockPos(signBlockEntity.getLevel(), signBlockEntity.getBlockPos()))
            .ifPresent(signCache -> {
                /* Modify the text of the sign. */
                List<String> inputLineStrings = isFront ? signCache.getFrontLines() : signCache.getBackLines();
                Component[] outputLineTexts = {Component.empty(), Component.empty(), Component.empty(), Component.empty()};

                for (int i = 0; i < inputLineStrings.size(); i++) {
                    String inputLineString = inputLineStrings.get(i);

                    // Escape from Mojang sign editor.
                    inputLineString = inputLineString.replace("<", "\\<")
                        .replace(">", "\\>");

                    // Restore the line string.
                    outputLineTexts[i] = Component.literal(inputLineString);
                }

                /* Send the update packet. */
                boolean facingFront = signBlockEntity.isFacingFrontText(player);
                SignText oldSignText = signBlockEntity.getText(facingFront);
                SignText newSignText = new SignText(outputLineTexts, outputLineTexts, oldSignText.getColor(), oldSignText.hasGlowingText());
                signBlockEntity.setText(newSignText, facingFront);
                player.connection.send(signBlockEntity.getUpdatePacket());
            });
    }

}
