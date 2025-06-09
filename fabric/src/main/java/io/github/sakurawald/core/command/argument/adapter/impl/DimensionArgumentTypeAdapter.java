package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.core.auxiliary.minecraft.ServerHelper;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.core.command.argument.wrapper.impl.Dimension;
import lombok.SneakyThrows;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class DimensionArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return DimensionArgumentType.dimension();
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
            /*
             The DimensionArgumentType.dimension() will not suggest the new registered dimension types.
             Each time the server started, the dimensions will be shared with client and server.
             */
        return super.makeRequiredArgumentBuilder(argumentName).suggests(
            (ctx, builder) -> {
                ServerHelper.getWorlds().forEach(it -> builder.suggest(RegistryHelper.ofString(it)));
                return builder.buildFuture();
            }
        );
    }

    @SneakyThrows(CommandSyntaxException.class)
    @Override
    public Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return new Dimension(DimensionArgumentType.getDimensionArgument(context, argument.getArgumentName()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Dimension.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("dimension", "world");
    }
}
