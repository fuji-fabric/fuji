package io.github.sakurawald.fuji.core.command.argument.adapter.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
#if MC_VER <= MC_1_20_1
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.GreedyString;
import io.github.sakurawald.fuji.core.command.argument.wrapper.impl.NotSupportedType;
#elif MC_VER > MC_1_20_1
import net.minecraft.command.argument.ScoreboardSlotArgumentType;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
#endif

import net.minecraft.server.command.ServerCommandSource;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ScoreboardSlotArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        #if MC_VER <= MC_1_20_1
        return StringArgumentType.greedyString();
        #elif MC_VER > MC_1_20_1
        return ScoreboardSlotArgumentType.scoreboardSlot();
        #endif

    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        #if MC_VER <= MC_1_20_1
        return new GreedyString(StringArgumentType.getString(context, argument.getArgumentName()));
        #elif MC_VER > MC_1_20_1
        return ScoreboardSlotArgumentType.getScoreboardSlot(context, commandArgument.getArgumentName());
        #endif
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        #if MC_VER <= MC_1_20_1
        return List.of(NotSupportedType.class);
        #elif MC_VER > MC_1_20_1
        return List.of(ScoreboardDisplaySlot.class);
        #endif
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("scoreboard-slot");
    }
}
