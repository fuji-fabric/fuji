package io.github.sakurawald.fuji.module.mixin.system_message;

import io.github.sakurawald.fuji.module.initializer.system_message.SystemMessageInitializer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Text.class)
public interface TextMixin {

    @Inject(method = "translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;", at = @At("RETURN"), cancellable = true)
    private static void hijackTranslatableText(String key, Object[] args, @NotNull CallbackInfoReturnable<MutableText> cir) {
        MutableText newValue = SystemMessageInitializer.modifyTranslatableText(key, args);
        if (newValue != null) cir.setReturnValue(newValue);
    }

    @Inject(method = "translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;", at = @At("RETURN"), cancellable = true)
    private static void hijackTranslatableText(String key, @NotNull CallbackInfoReturnable<MutableText> cir) {
        MutableText newValue = SystemMessageInitializer.modifyTranslatableText(key);
        if (newValue != null) cir.setReturnValue(newValue);
    }

}
