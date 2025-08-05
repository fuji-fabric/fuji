package io.github.sakurawald.fuji.module.initializer.rank;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.PlayerHelper;
import io.github.sakurawald.fuji.core.auxiliary.minecraft.TextHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.rank.command.argument.wrapper.NextAvailableRankNode;
import io.github.sakurawald.fuji.module.initializer.rank.command.argument.wrapper.PreviousAvailableRankNode;
import io.github.sakurawald.fuji.module.initializer.rank.config.model.RankConfigModel;
import io.github.sakurawald.fuji.module.initializer.rank.config.model.RankDataModel;
import io.github.sakurawald.fuji.module.initializer.rank.service.RankService;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankNode;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;


@Document(id = 1754411151804L, value = """
    This module provides the rank up system.
    """)
public class RankInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<RankConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, RankConfigModel.class);
    public static final BaseConfigurationHandler<RankDataModel> data = new ObjectConfigurationHandler<>("rank-data.json", RankDataModel.class);

    @Document(id = 1754412528895L, value = "List all defined `rank nodes`.")
    @CommandNode("rank list all-rank-nodes")
    @CommandRequirement(level = 4)
    private static int $listAllRankNodes(@CommandSource ServerCommandSource source) {
        TextHelper.sendTextByKey(source, "rank.list.all_rank_nodes", RankService.getAllRankIds());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1754412670219L, value = "Query the info of the specified `rank node`.")
    @CommandNode("rank info")
    @CommandRequirement(level = 4)
    private static int $info(@CommandSource ServerCommandSource source, RankNode rankNode) {
        TextHelper.sendTextByKey(source, "rank.rank_node.id", rankNode.getId());
        TextHelper.sendTextByKey(source, "rank.rank_node.display_name", rankNode.getDisplayName());

        List<String> nextRankNodes = rankNode.getNextRankNodes();
        if (nextRankNodes.isEmpty()) {
            TextHelper.sendTextByKey(source, "rank.rank_node.next_nodes", TextHelper.Operators.visitString(RankService.getNoRankStatusText()));
        } else {
            TextHelper.sendTextByKey(source, "rank.rank_node.next_nodes", nextRankNodes);
        }
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1754414840437L, value = "List all available `starting rank nodes` for the specified player.")
    @CommandNode("rank list starting-rank-nodes")
    @CommandRequirement(level = 4)
    private static int $listStartingRankNodes(@CommandSource ServerPlayerEntity player, ServerPlayerEntity target) {
        String playerName = PlayerHelper.getPlayerName(target);
        List<String> availableStartingRankNodes = RankService
            .getAvailableStartingRankNodes(target)
            .stream()
            .map(RankNode::getId)
            .toList();
        TextHelper.sendTextByKey(player, "rank.list.starting_rank_nodes", playerName, availableStartingRankNodes);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1754415572673L, value = "Query the rank progress of the specified player.")
    @CommandNode("rank progress")
    @CommandRequirement(level = 4)
    private static int $rankProgress(@CommandSource ServerCommandSource source, ServerPlayerEntity target) {
        return RankService
            .getCurrentRankNode(target)
            .map(currentRankNode -> $info(target.getCommandSource(), currentRankNode))
            .orElseGet(() -> {
                String playerName = PlayerHelper.getPlayerName(target);
                TextHelper.sendTextByKey(source, "rank.progress.no_rank", playerName);
                return CommandHelper.Return.FAIL;
            });
    }

    @Document(id = 1754420858807L, value = "Query the rank progress.")
    @CommandNode("rank progress")
    private static int $rankProgress(@CommandSource ServerPlayerEntity player) {
        return $rankProgress(player.getCommandSource(), player);
    }

    @Document(id = 1754417962937L, value = "Set the rank for specified player.")
    @CommandNode("rank set")
    @CommandRequirement(level = 4)
    private static int $setRank(@CommandSource ServerCommandSource source, ServerPlayerEntity target, RankNode rankNode) {
        RankService.setCurrentRankNode(target, rankNode);
        TextHelper.sendTextByKey(source, "rank.set", PlayerHelper.getPlayerName(target), rankNode.getId());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1754418507342L, value = "Rank up to the next available rank node.")
    @CommandNode("rank up")
    private static int $rankUp(@CommandSource ServerPlayerEntity player, NextAvailableRankNode nextRank, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(player, confirm, () -> {
            RankNode $nextRank = nextRank.getValue();
            RankService.setCurrentRankNode(player, $nextRank);
            TextHelper.sendTextByKey(player, "rank.up", $nextRank.getDisplayName());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1754423529102L, value = "Rank down to the previous available rank node.")
    @CommandNode("rank down")
    private static int $rankDown(@CommandSource ServerPlayerEntity player, PreviousAvailableRankNode previousRank, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(player, confirm, () -> {
            RankNode $previousRank = previousRank.getValue();
            RankService.setCurrentRankNode(player, $previousRank);
            TextHelper.sendTextByKey(player, "rank.down", $previousRank.getDisplayName());
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1754421296792L, value = "Set the specified player's rank to none.")
    @CommandNode("rank remove")
    @CommandRequirement(level = 4)
    private static int $removeRank(@CommandSource ServerCommandSource source, ServerPlayerEntity player, Optional<Boolean> confirm) {
        return CommandHelper.Pattern.withCommandConfirmed(source, confirm, () -> {
            RankService.setCurrentRankNode(player, null);
            TextHelper.sendTextByKey(source, "rank.remove", PlayerHelper.getPlayerName(player));
            return CommandHelper.Return.SUCCESS;
        });
    }

    @Document(id = 1754424553830L, value = "List all available `next rank nodes` for the specified player.")
    @CommandNode("rank list next-rank-nodes")
    @CommandRequirement(level = 4)
    private static int $listNextRankNodes(@CommandSource ServerPlayerEntity player, ServerPlayerEntity target) {
        String playerName = PlayerHelper.getPlayerName(target);
        List<String> ids = RankService.getNextAvailableRankNodes(target).stream().map(RankNode::getId).toList();
        TextHelper.sendTextByKey(player, "rank.list.next_rank_nodes", playerName, ids);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1754424619470L, value = "List all available `previous rank nodes` for the specified player.")
    @CommandNode("rank list previous-rank-nodes")
    @CommandRequirement(level = 4)
    private static int $listPreviousRankNodes(@CommandSource ServerPlayerEntity player, ServerPlayerEntity target) {
        String playerName = PlayerHelper.getPlayerName(target);
        List<String> ids = RankService.getPreviousAvailableRankNodes(target).stream().map(RankNode::getId).toList();
        TextHelper.sendTextByKey(player, "rank.list.previous_rank_nodes", playerName, ids);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1754430022210L, value = "List all `walked rank nodes` for the specified player.")
    @CommandNode("rank list walked-rank-nodes")
    @CommandRequirement(level = 4)
    private static int $listWalkedRankNodes(@CommandSource ServerPlayerEntity player, ServerPlayerEntity target) {
        String playerName = PlayerHelper.getPlayerName(target);
        Set<String> ids = RankService.getWalkedRankNodeIds(target);
        TextHelper.sendTextByKey(player, "rank.list.walked_rank_nodes", playerName, ids);
        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void onInitialize() {
        RankService.computeRankGraph();
    }

    @Override
    protected void onReload() {
        RankService.computeRankGraph();
    }
}
