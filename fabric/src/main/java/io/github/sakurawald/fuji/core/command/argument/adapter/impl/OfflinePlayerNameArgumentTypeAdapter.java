package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.OfflinePlayerName;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class OfflinePlayerNameArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    public ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(PlayerHelper::getOfflinePlayerNames));
    }

    @Override
    public Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        String offlinePlayerName = StringArgumentType.getString(context, argument.getArgumentName());

        return PlayerHelper
            .getOfflineGameProfileByName(offlinePlayerName)
            .map($gameProfile -> new OfflinePlayerName($gameProfile.getName()))
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(context.getSource(), "player.unknown_player");
                return new AbortCommandExecutionException();
            });
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(OfflinePlayerName.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("offline-player");
    }
}
