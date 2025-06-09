package io.github.sakurawald.module.mixin.core.command;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContextBuilder;
import io.github.sakurawald.core.command.extension.CommandContextBuilderExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CommandDispatcher.class)
public class CommandDispatcherMixin<S> {

    // apply patch: https://github.com/Mojang/brigadier/pull/142
    @SuppressWarnings({"unchecked", "rawtypes"})
    @ModifyVariable(method = "parseNodes", at = @At(value = "STORE"), ordinal = 2, remap = false)
    CommandContextBuilder passChildContextAfterRedirect(CommandContextBuilder<S> childContext, @Local(ordinal = 1) CommandContextBuilder<S> parentContext) {
        CommandContextBuilderExtension<S> accessor = (CommandContextBuilderExtension<S>) childContext;
        accessor.fuji$withArguments(parentContext.getArguments());
        return childContext;
    }
}
