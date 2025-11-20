package mod.fuji.module.initializer.rank.command.argument.adapter;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.argument.adapter.abst.BaseArgumentTypeAdapter;
import mod.fuji.core.command.argument.structure.CommandArgument;
import mod.fuji.core.command.exception.AbortCommandExecutionException;
import mod.fuji.module.initializer.rank.command.argument.wrapper.NextAvailableRankNode;
import mod.fuji.module.initializer.rank.service.RankService;
import mod.fuji.module.initializer.rank.structure.RankNode;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class NextAvailableRankNodesArgumentTypeAdapter extends BaseArgumentTypeAdapter {
    @Override
    protected ArgumentType<?> makeArgumentType() {
        return StringArgumentType.string();
    }

    @Override
    protected Object makeArgumentValue(@NotNull CommandContext<CommandSourceStack> context, @NotNull CommandArgument commandArgument) {
        String rankId = StringArgumentType.getString(context, commandArgument.getArgumentName());

        ServerPlayer player = context.getSource().getPlayer();

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
    protected RequiredArgumentBuilder<CommandSourceStack, ?> makeRequiredArgumentBuilder(@NotNull String argumentName) {
        return super
            .makeRequiredArgumentBuilder(argumentName)
            .suggests(CommandHelper.Suggestion.iterable((context, builder) -> {
                ServerPlayer player = context.getSource().getPlayer();
                if (player == null) {
                    return List.of();
                }

                return RankService.getNextAvailableRankNodes(player).stream().map(RankNode::getId).toList();
            }));
    }

}
