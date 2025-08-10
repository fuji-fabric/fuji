package io.github.sakurawald.fuji.module.initializer.leaderboard.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.module.initializer.leaderboard.service.LeaderBoardService;
import io.github.sakurawald.fuji.module.initializer.leaderboard.structure.LeaderBoardDescriptor;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class LeaderBoardDescriptorArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull Argument argument) {
        String leaderBoardDescriptorId = StringArgumentType.getString(context, argument.getArgumentName());

        Optional<LeaderBoardDescriptor> leaderBoardDescriptor = LeaderBoardService.findLeaderBoardDescriptor(leaderBoardDescriptorId);
        if (leaderBoardDescriptor.isEmpty()) {
            TextHelper.sendTextByKey(context.getSource(), "leaderboard.not_found", leaderBoardDescriptorId);
            throw new AbortCommandExecutionException();
        }

        return leaderBoardDescriptor.get();
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(LeaderBoardDescriptor.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("leaderboard");
    }

    @Override
    public @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(() -> LeaderBoardService
                .getLeaderBoardDescriptors()
                .stream()
                .map(LeaderBoardDescriptor::getLeaderboardId)
                .toList()));
    }
}
