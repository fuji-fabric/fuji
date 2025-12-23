package mod.fuji.module.mixin.color.sign;

import com.llamalad7.mixinextras.sugar.Local;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.structure.GlobalBlockPos;
import mod.fuji.module.initializer.color.ColorInitializer;
import mod.fuji.module.initializer.color.sign.ColorSignInitializer;
import mod.fuji.module.initializer.color.sign.structure.SignCache;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
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

    @Shadow
    public abstract @Nullable UUID getPlayerWhoMayEdit();

    public SignBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @ModifyVariable(method = "setText", at = @At("HEAD"), argsOnly = true)
    @NotNull
    SignText processInputLineStrings(@NotNull SignText signText, @Local(argsOnly = true) boolean isFront) {
        // NOTE: Only process the sign text when there is a logic server in current session.
        if (ServerHelper.getServer() == null) return signText;
        if (!WorldHelper.isServerWorld(this.level)) return signText;

        /* Process input line texts. */
        Component[] inputLineTexts = signText.getMessages(false);
        Component[] outputLineTexts = new Component[inputLineTexts.length];
        String[] inputLineStrings = new String[inputLineTexts.length];

        for (int i = 0; i < inputLineTexts.length; i++) {
            /* Map the line text into line string. */
            // NOTE: The inputLineTexts[i] may be null if you write nothing.
            if (inputLineTexts[i] == null) {
                inputLineTexts[i] = Component.literal("");
            }
            AtomicReference<String> inputLineString = new AtomicReference<>(TextHelper.Operators.getString(inputLineTexts[i]));

            /* Rewrite the input line string. */
            inputLineString.getAndUpdate(ColorInitializer::rewriteColorCodes);

            /* Stripe style tags. */
            if (ColorSignInitializer.config.model().requires_corresponding_permission_to_use_style_tag) {
                Optional
                    .ofNullable(getPlayerWhoMayEdit())
                    .ifPresent(editorUUID -> {
                        Optional<ServerPlayer> editingPlayer = PlayerHelper.Lookup.getOnlinePlayerByUuid(editorUUID);
                        editingPlayer.ifPresent(player -> {
                            inputLineString.getAndUpdate(it -> ColorSignInitializer.stripeStyleTags(player, it));
                        });
                    });
            }

            /* Update the final line texts and line strings. */
            outputLineTexts[i] = TextHelper.Parsers.parseString(TextHelper.Parsers.MINI_MESSAGE_ONLY_PARSER, inputLineString.get());
            inputLineStrings[i] = inputLineString.get();
        }

        /* Write sign cache. */
        writeSignCache(isFront, inputLineStrings);

        /* Return the output line texts. */
        return new SignText(outputLineTexts, outputLineTexts, signText.getColor(), signText.hasGlowingText());
    }

    @Unique
    private void writeSignCache(boolean isFront, String[] inputLines) {
        List<String> $inputLines = Arrays
            .stream(inputLines)
            .toList();

        @NotNull GlobalBlockPos globalBlockPos = new GlobalBlockPos(getLevel(), getBlockPos());
        @NotNull SignCache signCache = ColorSignInitializer
            .readSignCache(globalBlockPos)
            .orElseGet(() -> new SignCache(List.of(), List.of()));

        if (isFront) {
            signCache = signCache.withFrontLines($inputLines);
        } else {
            signCache = signCache.withBackLines($inputLines);
        }

        ColorSignInitializer.writeSignCache(globalBlockPos, signCache);
    }

}
