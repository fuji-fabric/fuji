package mod.fuji.module.mixin.system_message;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.module.initializer.system_message.SystemMessageInitializer;
import mod.fuji.module.initializer.system_message.structure.SystemMessageRule;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Component.class, priority = EventConsumer.HIGHEST)
public interface TextMixin {

    @ModifyReturnValue(method = "translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;", at = @At("RETURN"))
    private static MutableComponent modifyTranslatableText(MutableComponent original, @Local(argsOnly = true) String key, @Local(argsOnly = true) Object[] args) {
        return modifyScreenText(null, original, key, args);
    }

    @ModifyReturnValue(method = "translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;", at = @At("RETURN"))
    private static MutableComponent modifyTranslatableText(MutableComponent original, @Local(argsOnly = true) String key) {
        return modifyScreenText(null, original, key);
    }

    @Unique
    private static @NotNull MutableComponent modifyScreenText(@Nullable ServerPlayer receiverPlayer, @NotNull MutableComponent fallbackText, @NotNull String translatableKey, Object... args) {
        Optional<SystemMessageRule> applicableRule = SystemMessageInitializer.findApplicableRule(translatableKey);
        return applicableRule
            .filter(SystemMessageRule::isScreenText)
            .flatMap(it -> SystemMessageInitializer.computeTranslatableTextResult(it, receiverPlayer, translatableKey, args))
            .orElse(fallbackText);
    }
}
