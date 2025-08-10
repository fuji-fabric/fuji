package io.github.sakurawald.fuji.module.initializer.leaderboard.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
import io.github.sakurawald.fuji.module.initializer.leaderboard.structure.LeaderBoardTimeWindow;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class LeaderBoardTimeWindowArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull Argument argument) {
        String string = StringArgumentType.getString(context, argument.getArgumentName());
        return LeaderBoardTimeWindow.valueOf(string);
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(LeaderBoardTimeWindow.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("leaderboard-time-window");
    }

    @Override
    public @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.enums(LeaderBoardTimeWindow::values));
    }
}
