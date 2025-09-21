package mod.fuji.module.mixin.color.sign;

import mod.fuji.core.structure.GlobalBlockPos;
import mod.fuji.module.initializer.color.sign.ColorSignInitializer;
import mod.fuji.module.initializer.color.sign.structure.SignCache;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Unique
    @NotNull
    final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    // NOTE: In lower MC versions like MC 1.20.1, if there are `<rb>` tag in the sign, then the `openEditSignScreen` method will not be called.
    @Inject(method = "openEditSignScreen", at = @At("HEAD"))
    private void sendBlockStateUpdatePacketOfSerializedTextBeforeTheClientOpenTheEditScreen(@NotNull SignBlockEntity signBlockEntity, boolean isFront, @NotNull CallbackInfo ci) {
        /* Update the sign text in server-side with the SignCache value before the client-side open the sign editor screen. */
        SignCache signCache = ColorSignInitializer.readSignCache(new GlobalBlockPos(signBlockEntity.getWorld(), signBlockEntity.getPos()));
        if (signCache == null) return;

        /* Modify the text of the sign. */
        List<String> trueLines = isFront ? signCache.getFrontLines() : signCache.getBackLines();
        Text[] newTextList = {Text.empty(), Text.empty(), Text.empty(), Text.empty()};

        for (int i = 0; i < trueLines.size(); i++) {
            String line = trueLines.get(i);
            // Escape from mojang sign editor.
            line = line.replace("<", "\\<")
                .replace(">", "\\>");

            // Restore the raw string.
            newTextList[i] = Text.literal(line);
        }

        /* Send update packet. */
        boolean facingFront = signBlockEntity.isPlayerFacingFront(player);
        SignText originalSignText = signBlockEntity.getText(facingFront);
        SignText newSignText = new SignText(newTextList, newTextList, originalSignText.getColor(), originalSignText.isGlowing());
        signBlockEntity.setText(newSignText, facingFront);
        player.networkHandler.sendPacket(signBlockEntity.toUpdatePacket());
    }

}
