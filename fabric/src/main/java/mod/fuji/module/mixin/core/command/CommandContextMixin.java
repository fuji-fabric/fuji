package mod.fuji.module.mixin.core.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import mod.fuji.core.command.extension.CommandContextAccessor;
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
