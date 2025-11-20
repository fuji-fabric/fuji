package mod.fuji.module.initializer.leaderboard.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.module.initializer.leaderboard.structure.LeaderBoardTimeWindow;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class LeaderBoardTimeWindowArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        String string = StringArgumentType.getString(context, commandArgument.getArgumentName());
        return LeaderBoardTimeWindow.valueOf(string);
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(LeaderBoardTimeWindow.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("leaderboard-time-window");
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.enums(LeaderBoardTimeWindow::values));
    }
}
