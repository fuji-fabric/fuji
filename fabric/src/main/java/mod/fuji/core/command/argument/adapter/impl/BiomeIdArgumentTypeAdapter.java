package mod.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.argument.wrapper.impl.BiomeId;
import java.util.List;
import net.minecraft.commands.arguments.ResourceLocationArgument;
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
        return ResourceLocationArgument.id();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        return new BiomeId(ResourceLocationArgument.getId(context, commandArgument.getArgumentName()));
    }

    @Override
    protected @NotNull RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super
            .makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.identifiers(Registries.BIOME));
    }
}
