package io.github.sakurawald.fuji.module.initializer.rank.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.core.command.exception.AbortCommandExecutionException;
import io.github.sakurawald.fuji.module.initializer.rank.command.argument.wrapper.NextAvailableRankNode;
import io.github.sakurawald.fuji.module.initializer.rank.service.RankService;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankNode;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class NextAvailableRankNodesArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<ServerCommandSource> context, @NotNull CommandArgument commandArgument) {
        String rankId = StringArgumentType.getString(context, commandArgument.getArgumentName());

        ServerPlayerEntity player = context.getSource().getPlayer();

        Optional<RankNode> nextRankNode = RankService
            .getNextAvailableRankNodes(player)
            .stream()
            .filter(it -> it.getId().equals(rankId))
            .findFirst();

        return nextRankNode
            .map(NextAvailableRankNode::new)
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(context.getSource(), "rank.unavailable_rank_node", rankId);
                return new AbortCommandExecutionException();
            });
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(NextAvailableRankNode.class);
    }

    @Override
    public List<String> getTypeNames() {
        return List.of("next-available-rank-nodes");
    }

    @Override
    @NotNull
    protected RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests((context, builder) -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player == null) {
                    return builder.buildFuture();
                }

                List<String> ids = RankService.getNextAvailableRankNodes(player).stream().map(RankNode::getId).toList();
                return CommandHelper.Suggestion.makeSuggestionsCompletableFuture(context, builder, () -> ids);
            });
    }

}
