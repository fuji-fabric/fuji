package io.github.sakurawald.fuji.module.initializer.home.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.module.initializer.home.command.argument.wrapper.HomeName;
import io.github.sakurawald.fuji.module.initializer.home.service.HomeService;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class HomeNameArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    public Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        String homeName = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return new HomeName(homeName);
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(HomeName.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("home-name");
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName).suggests((context, builder) -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player == null) return builder.buildFuture();

                String playerName = PlayerHelper.getPlayerName(player);
                HomeService
                    .withHomeMap(playerName)
                    .keySet()
                    .forEach(builder::suggest);
                return builder.buildFuture();
            }
        );
    }
}
