package mod.fuji.module.mixin.color.sign;

import mod.fuji.core.auxiliary.minecraft.EntityHelper;
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
    #if MC_VER < MC_26_3
    void restoreInputLineStringsOnClientSide(@NotNull SignBlockEntity signBlockEntity, boolean signTextSpecifier, @NotNull CallbackInfo ci)
    #elif MC_VER >= MC_26_3
    void restoreInputLineStringsOnClientSide(@NotNull SignBlockEntity signBlockEntity, net.minecraft.world.level.block.entity.SignTextSlot signTextSpecifier, @NotNull CallbackInfo ci)
    #endif
    {
        ColorSignInitializer
            .readSignCache(new GlobalBlockPos(signBlockEntity.getLevel(), signBlockEntity.getBlockPos()))
            .ifPresent(signCache -> {
                /* Modify the text of the sign. */
                boolean isFrontSide = EntityHelper.SignBlock.toFacingFront(signTextSpecifier);
                List<String> inputLineStrings = isFrontSide ? signCache.getFrontLines() : signCache.getBackLines();
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
                SignText oldSignText = EntityHelper.SignBlock.getFacingSignText(player, signBlockEntity);
                SignText newSignText = new SignText(EntityHelper.SignBlock.coerceLineTexts(outputLineTexts), EntityHelper.SignBlock.coerceLineTexts(outputLineTexts), oldSignText.getColor(), oldSignText.hasGlowingText());

                EntityHelper.SignBlock.updateSignText(signBlockEntity, a -> newSignText, isFrontSide);
                player.connection.send(signBlockEntity.getUpdatePacket());
            });
    }

}
