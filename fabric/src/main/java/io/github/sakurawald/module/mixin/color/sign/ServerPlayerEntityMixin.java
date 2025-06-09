package io.github.sakurawald.module.mixin.color.sign;

import io.github.sakurawald.core.structure.SpatialBlock;
import io.github.sakurawald.module.initializer.color.sign.ColorSignInitializer;
import io.github.sakurawald.module.initializer.color.sign.structure.SignCache;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Unique
    @NotNull
    final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @Shadow
    public abstract ServerWorld getServerWorld();

    @Inject(method = "openEditSignScreen", at = @At("HEAD"))
    private void sendBlockStateUpdatePacketOfSerializedTextBeforeTheClientOpenTheEditScreen(@NotNull SignBlockEntity signBlockEntity, boolean bl, @NotNull CallbackInfo ci) {
        /* verify */
        if (ci.isCancelled()) return;

        /* update the sign text in server-side with the sign cahce value before the client-side open the sign editor screen. */
        SignCache signCache = ColorSignInitializer.readSignCache(new SpatialBlock(signBlockEntity.getWorld(), signBlockEntity.getPos()));
        if (signCache == null) return;

        Text[] newTextList = new Text[4];
        for (int i = 0; i < signCache.getLines().size(); i++) {
            String line = signCache.getLines().get(i);
            // escape from mojang sign editor
            line = line.replace("<", "\\<")
                .replace(">", "\\>");

            // restore the raw string.
            newTextList[i] = Text.literal(line);
        }

        boolean facing = signBlockEntity.isPlayerFacingFront(player);
        SignText originalSignText = signBlockEntity.getText(facing);
        SignText newSignText = new SignText(newTextList, newTextList, originalSignText.getColor(), originalSignText.isGlowing());
        signBlockEntity.setText(newSignText, facing);
        player.networkHandler.sendPacket(signBlockEntity.toUpdatePacket());
    }

}
