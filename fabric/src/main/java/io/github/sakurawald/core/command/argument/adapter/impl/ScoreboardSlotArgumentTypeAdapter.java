package io.github.sakurawald.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.core.command.argument.structure.Argument;
import net.minecraft.command.argument.ScoreboardSlotArgumentType;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public class ScoreboardSlotArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return ScoreboardSlotArgumentType.scoreboardSlot();
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        return ScoreboardSlotArgumentType.getScoreboardSlot(context, argument.getArgumentName());
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(ScoreboardDisplaySlot.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("scoreboard-slot");
    }
}
