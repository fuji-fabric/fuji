package mod.fuji.module.mixin.color.sign;

import com.llamalad7.mixinextras.sugar.Local;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.ServerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.auxiliary.minecraft.WorldHelper;
import mod.fuji.core.structure.GlobalBlockPos;
import mod.fuji.module.initializer.color.sign.ColorSignInitializer;
import mod.fuji.module.initializer.color.sign.structure.SignCache;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
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

    public SignBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @ModifyVariable(method = "setText", at = @At("HEAD"), argsOnly = true)
    @NotNull
    SignText parseTextWhenSetText(@NotNull SignText signText, @Local(argsOnly = true) boolean isFront) {
        // NOTE: The function will be called in client and server. When install fuji in client side, and edit sign blocks in online server, the server variable will be null.
        if (ServerHelper.getServer() == null) {
            return signText;
        }

        if (!WorldHelper.isServerWorld(this.world)) return signText;


        /* Parse input strings. */
        Text[] messages = signText.getMessages(false);
        Text[] newMessages = new Text[messages.length];
        for (int i = 0; i < messages.length; i++) {
            /* Get the line string from the sign text. */
            // NOTE: The messages[i] may be null if you write nothing.
            if (messages[i] == null) messages[i] = Text.literal("");
            AtomicReference<String> lineString = new AtomicReference<>(messages[i].getString());

            /* Stripe style tags. */
            if (ColorSignInitializer.config.model().requires_corresponding_permission_to_use_style_tag) {
                Optional
                    .ofNullable(getEditor())
                    .ifPresent(editorUUID -> {
                        Optional<ServerPlayerEntity> playerOpt = PlayerHelper.Lookup.getOnlinePlayerByUuid(editorUUID);
                        if (playerOpt.isPresent()) {
                            ServerPlayerEntity player = playerOpt.get();
                            lineString.set(ColorSignInitializer.stripeStyleTags(player, lineString.get()));
                        }
                    });
            }

            /* Set the sign texts using parsed texts. */
            newMessages[i] = TextHelper.Parsers.parseString(TextHelper.Parsers.MINI_MESSAGE_ONLY_PARSER, lineString.get());
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
