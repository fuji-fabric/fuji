package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.representation.IdentifierArgumentTypeIR;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.BiomeId;
import java.util.List;
import mod.fuji.core.structure.IdentifierIR;
import net.minecraft.core.registries.Registries;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class BiomeIdArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(BiomeId.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("biome");
    }

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return IdentifierArgumentTypeIR.makeArgumentType();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        var nativeValue = IdentifierArgumentTypeIR.makeArgumentValue(context, commandArgument);
        return new BiomeId(IdentifierIR.of(nativeValue));
    }

    @Override
    protected @NotNull RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super
            .makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.identifiers(Registries.BIOME));
    }
}
