package mod.fuji.module.mixin.command_bundle;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import mod.fuji.module.initializer.command_bundle.accessor.CommandContextAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = CommandContext.class, remap = false)
public abstract class CommandContextMixin<S> implements CommandContextAccessor<S> {

    // TODO(Ravel): Could not determine a single target
    @Shadow
    @Final
    private Map<String, ParsedArgument<S, ?>> arguments;

    @Override
    public Map<String, ParsedArgument<S, ?>> fuji$getArguments() {
        return arguments;
    }
}
