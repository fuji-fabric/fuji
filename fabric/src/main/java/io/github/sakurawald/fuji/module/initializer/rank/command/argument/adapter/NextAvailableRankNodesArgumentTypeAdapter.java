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
import io.github.sakurawald.fuji.module.initializer.rank.command.argument.wrapper.NextAvailableRankNode;
import io.github.sakurawald.fuji.module.initializer.rank.service.RankService;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankNode;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class NextAvailableRankNodesArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentObject(CommandContext<ServerCommandSource> context, Argument argument) {
        String rankId = StringArgumentType.getString(context, argument.getArgumentName());

        ServerPlayerEntity player = context.getSource().getPlayer();

        Optional<RankNode> nextRankNode = RankService
            .getNextAvailableRankNodes(player)
            .stream()
            .filter(it -> it.getId().equals(rankId))
            .findFirst();

        return nextRankNode
            .map(NextAvailableRankNode::new)
            .orElseThrow(() -> {
                TextHelper.sendTextByKey(context.getSource(), "rank.up.unavailable_rank_node", rankId);
                return new AbortCommandExecutionException();
            });
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return List.of(NextAvailableRankNode.class);
    }

    @Override
    public List<String> getTypeStrings() {
        return List.of("next-available-rank-nodes");
    }

    @Override
    public RequiredArgumentBuilder<ServerCommandSource, ?> makeRequiredArgumentBuilder(String argumentName) {
        return super.makeRequiredArgumentBuilder(argumentName)
            .suggests((context, builder) -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player == null) {
                    return builder.buildFuture();
                }

                List<String> ids = RankService.getNextAvailableRankNodeIds(player);
                return CommandHelper.Suggestion.makeSuggestionsCompletableFuture(() -> ids, builder);
            });
    }

}
