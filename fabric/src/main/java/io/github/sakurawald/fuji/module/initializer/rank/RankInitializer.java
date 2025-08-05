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
import io.github.sakurawald.fuji.module.initializer.rank.config.model.RankConfigModel;
import io.github.sakurawald.fuji.module.initializer.rank.config.model.RankDataModel;
import io.github.sakurawald.fuji.module.initializer.rank.service.RankService;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankNode;
import java.util.List;
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
        TextHelper.sendTextByKey(source, "rank.rank_node.next_nodes", rankNode.getNextRankNodes());
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

    @Document(id = 1754417962937L, value = "Set the rank for specified player.")
    @CommandNode("rank set")
    @CommandRequirement(level = 4)
    private static int $setRank(@CommandSource ServerCommandSource source, ServerPlayerEntity target, RankNode rankNode) {
        RankService.setCurrentRankNode(target, rankNode);
        TextHelper.sendTextByKey(source, "rank.set", PlayerHelper.getPlayerName(target), rankNode.getId());
        return CommandHelper.Return.SUCCESS;
    }

}
