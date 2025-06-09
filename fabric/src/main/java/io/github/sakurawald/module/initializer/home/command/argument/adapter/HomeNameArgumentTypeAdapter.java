package io.github.sakurawald.module.initializer.home.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import io.github.sakurawald.core.structure.SpatialPose;
import io.github.sakurawald.module.initializer.home.HomeInitializer;
import io.github.sakurawald.module.initializer.home.command.argument.wrapper.HomeName;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Map;

public class HomeNameArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    public Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return new HomeName(StringArgumentType.getString(context, argument.getArgumentName()));
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(HomeName.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("home-name");
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName).suggests((context, builder) -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player == null) return builder.buildFuture();

                Map<String, SpatialPose> name2position = HomeInitializer.withHomes(player);
                name2position.keySet().forEach(builder::suggest);
                return builder.buildFuture();
            }
        );
    }
}
