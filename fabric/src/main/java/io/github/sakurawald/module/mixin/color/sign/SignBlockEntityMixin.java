package io.github.sakurawald.module.mixin.color.sign;

import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.service.style_striper.StyleStriper;
import io.github.sakurawald.core.structure.SpatialBlock;
import io.github.sakurawald.module.initializer.color.sign.ColorSignInitializer;
import io.github.sakurawald.module.initializer.color.sign.structure.SignCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin extends BlockEntity {

    @Unique
    private static final String STYLE_TYPE_SIGN = "sign";

    @Shadow
    public abstract @Nullable UUID getEditor();

    @Shadow
    public abstract boolean isPlayerFacingFront(PlayerEntity playerEntity);

    public SignBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @ModifyVariable(method = "setText", at = @At("HEAD"), argsOnly = true)
    @NotNull
    SignText method(@NotNull SignText signText) {
        Text[] messages = signText.getMessages(false);
        Text[] newMessages = new Text[messages.length];
        for (int i = 0; i < messages.length; i++) {
            String string = messages[i].getString();

            /* stripe style tags */
            if (ColorSignInitializer.config.model().requires_corresponding_permission_to_use_style_tag) {
                Optional<ServerPlayerEntity> playerOpt = ServerHelper.getPlayerByUuid(getEditor());
                if (playerOpt.isPresent()) {
                    string = StyleStriper.stripe(playerOpt.get(), STYLE_TYPE_SIGN, string);
                }
            }

            newMessages[i] = TextHelper.getTextByValue(null, string);
        }

        /* write cache */
        List<String> lines = Arrays.stream(messages)
            .map(Text::getString)
            .toList();
        ColorSignInitializer.writeSignCache(new SpatialBlock(getWorld(), getPos()), new SignCache(lines));

        /* return the modified text */
        return new SignText(newMessages, newMessages, signText.getColor(), signText.isGlowing());
    }
}
