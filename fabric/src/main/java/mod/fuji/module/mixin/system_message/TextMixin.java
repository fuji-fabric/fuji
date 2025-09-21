package mod.fuji.module.mixin.system_message;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.module.initializer.system_message.SystemMessageInitializer;
import mod.fuji.module.initializer.system_message.structure.SystemMessageRule;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Text.class, priority = EventConsumer.HIGHEST)
public interface TextMixin {

    @ModifyReturnValue(method = "translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;", at = @At("RETURN"))
    private static MutableText modifyTranslatableText(MutableText original, @Local(argsOnly = true) String key, @Local(argsOnly = true) Object[] args) {
        return modifyScreenText(null, original, key, args);
    }

    @ModifyReturnValue(method = "translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;", at = @At("RETURN"))
    private static MutableText modifyTranslatableText(MutableText original, @Local(argsOnly = true) String key) {
        return modifyScreenText(null, original, key);
    }

    @Unique
    private static @NotNull MutableText modifyScreenText(@Nullable ServerPlayerEntity receiverPlayer, @NotNull MutableText fallbackText, @NotNull String translatableKey, Object... args) {
        Optional<SystemMessageRule> applicableRule = SystemMessageInitializer.findApplicableRule(translatableKey);
        return applicableRule
            .filter(SystemMessageRule::isScreenText)
            .flatMap(it -> SystemMessageInitializer.computeTranslatableTextResult(it, receiverPlayer, translatableKey, args))
            .orElse(fallbackText);
    }
}
