package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.RegistryHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.WorldHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.Dimension;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
import java.util.List;
import lombok.SneakyThrows;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class DimensionArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return DimensionArgumentType.dimension();
    }

    @ForDeveloper("""
        1. The DimensionArgumentType.dimension() will not suggest the new registered dimensions, or un-registered dimensions.
        2. The dimension registry is synced when the client joins the server, and it's fixed.
        3. FIXME: When you call RequiredArgumentBuilder#suggests() method, the `/back {push|clear}` will also be suggested, even the command source has no permission to use it.
        """)
    @Override
    @NotNull
    protected RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(
            (ctx, builder) -> {
                WorldHelper.getWorlds().forEach(it -> builder.suggest(RegistryHelper.getIdAsString(it)));
                return builder.buildFuture();
            }
        );
    }

    @SneakyThrows(CommandSyntaxException.class)
    @Override
    public Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        return new Dimension(DimensionArgumentType.getDimensionArgument(context, commandArgument.getArgumentName()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(Dimension.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("dimension", "world");
    }

    @Override
    public boolean isVanillaMinecraftArgumentType() {
        return true;
    }
}
