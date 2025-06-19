package io.github.sakurawald.module.mixin.color.sign;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.core.service.style_striper.StyleStriper;
import io.github.sakurawald.core.structure.GlobalBlockPos;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin extends BlockEntity {

    @Shadow
    public abstract @Nullable UUID getEditor();

    @Shadow
    public abstract boolean isPlayerFacingFront(PlayerEntity playerEntity);

    public SignBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @ModifyVariable(method = "setText", at = @At("HEAD"), argsOnly = true)
    @NotNull
    SignText parseTextWhenSetText(@NotNull SignText signText, @Local(ordinal = 0, argsOnly = true) boolean isFront) {
        /* Parse input strings. */
        Text[] messages = signText.getMessages(false);
        Text[] newMessages = new Text[messages.length];
        for (int i = 0; i < messages.length; i++) {
            String string = messages[i].getString();

            /* Stripe style tags. */
            if (ColorSignInitializer.config.model().requires_corresponding_permission_to_use_style_tag) {
                Optional<ServerPlayerEntity> playerOpt = ServerHelper.getPlayerByUuid(getEditor());
                if (playerOpt.isPresent()) {
                    string = StyleStriper.stripe(playerOpt.get(), ColorSignInitializer.STYLE_TYPE_SIGN, string);
                }
            }

            newMessages[i] = TextHelper.parseString(TextHelper.DEFAULT_PARSER, string);
        }

        /* Write sign cache. */
        List<String> lines = Arrays.stream(messages)
            .map(Text::getString)
            .toList();

        GlobalBlockPos globalBlockPos = new GlobalBlockPos(getWorld(), getPos());
        @Nullable SignCache signCache = ColorSignInitializer.readSignCache(globalBlockPos);
        if (signCache == null) signCache = new SignCache(List.of(), List.of());
        if (isFront) {
            signCache = signCache.withFrontLines(lines);
        } else {
            signCache = signCache.withBackLines(lines);
        }

        ColorSignInitializer.writeSignCache(globalBlockPos, signCache);

        /* Return the modified text. */
        return new SignText(newMessages, newMessages, signText.getColor(), signText.isGlowing());
    }
}
