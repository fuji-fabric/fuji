package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.core.command.argument.wrapper.impl.DimensionType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class DimensionTypeArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return new DimensionType(StringArgumentType.getString(context, argument.getArgumentName()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(DimensionType.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("dimension-type", "world-type");
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName).suggests(CommandHelper.Suggestion.identifiers(RegistryKeys.DIMENSION_TYPE));
    }
}
