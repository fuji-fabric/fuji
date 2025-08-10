package io.github.sakurawald.fuji.module.initializer.rank.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.Argument;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.module.initializer.rank.service.RankService;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankNode;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

public class RankNodeArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull Argument argument) {
        String rankId = StringArgumentType.getString(context,argument.getArgumentName());
        return RankService
            .findRankNode(rankId)
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(context.getSource(), "rank.rank_node.not_found", rankId);
                return new AbortCommandExecutionException();
            });
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(RankNode.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("rank-id");
    }

    @Override
    public @NotNull RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable(RankService::getAllRankIds));
    }
}
