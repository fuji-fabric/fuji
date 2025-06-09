package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import lombok.SneakyThrows;
import net.minecraft.command.argument.GameModeArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.GameMode;

import java.util.List;

public class GamemodeArgumentTypeAdapter extends BaseArgumentTypeAdapter {

    @Override
    protected ArgumentType<?> makeArgumentType() {
        return GameModeArgumentType.gameMode();
    }

    @SneakyThrows
    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return GameModeArgumentType.getGameMode(context, argument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(GameMode.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("gamemode");
    }
}
