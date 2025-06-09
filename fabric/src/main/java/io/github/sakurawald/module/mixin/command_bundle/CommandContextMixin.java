package io.github.sakurawald.module.mixin.command_bundle;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import io.github.sakurawald.module.initializer.command_bundle.accessor.CommandContextAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = CommandContext.class, remap = false)
public abstract class CommandContextMixin<S> implements CommandContextAccessor<S> {

    @Shadow
    @Final
    private Map<String, ParsedArgument<S, ?>> arguments;

    @Override
    public Map<String, ParsedArgument<S, ?>> fuji$getArguments() {
        return arguments;
    }
}
