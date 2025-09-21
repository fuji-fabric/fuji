package mod.fuji.module.mixin.core.command;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import mod.fuji.core.command.extension.CommandContextBuilderExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(CommandContextBuilder.class)
public abstract class CommandContextBuilderMixin<S> implements CommandContextBuilderExtension<S> {

    @Accessor(value = "arguments", remap = false)
    public abstract Map<String, ParsedArgument<S, ?>> getArguments();

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Unique
    public CommandContextBuilder<S> fuji$withArguments(Map<String, ParsedArgument<S, ?>> arguments) {
        getArguments().putAll(arguments);
        return (CommandContextBuilder) (Object) this;
    }
}
