package io.github.sakurawald.fuji.module.mixin.system_message;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.sakurawald.fuji.module.initializer.system_message.SystemMessageInitializer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Text.class)
public interface TextMixin {

    @ModifyReturnValue(method = "translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;", at = @At("RETURN"))
    private static MutableText modifyTranslatableText(MutableText original, @Local(argsOnly = true) String key, @Local(argsOnly = true) Object[] args) {
        return SystemMessageInitializer
                .modifyTranslatableText(null, key, args)
                .orElse(original);
    }

    @ModifyReturnValue(method = "translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;", at = @At("RETURN"))
    private static MutableText modifyTranslatableText(MutableText original, @Local(argsOnly = true) String key) {
        return SystemMessageInitializer
                .modifyTranslatableText(null, key)
                .orElse(original);
    }

}
