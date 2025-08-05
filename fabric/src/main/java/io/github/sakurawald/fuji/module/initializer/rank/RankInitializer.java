package io.github.sakurawald.fuji.module.initializer.rank;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
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
import net.minecraft.server.command.ServerCommandSource;


@Document(id = 1754411151804L, value = """
    This module provides the rank up system.
    """)
public class RankInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<RankConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, RankConfigModel.class);
    public static final BaseConfigurationHandler<RankDataModel> data = new ObjectConfigurationHandler<>("rank-data.json", RankDataModel.class);

    @Document(id = 1754412528895L, value = "List all defined `rank nodes`.")
    @CommandNode("rank list")
    @CommandRequirement(level = 4)
    private static int $list(@CommandSource ServerCommandSource source) {
        TextHelper.sendTextByKey(source, "rank.list", RankService.getAllRankIds());
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1754412670219L, value = "Query the info of specified `rank node`.")
    @CommandNode("rank info")
    @CommandRequirement(level = 4)
    private static int $info(@CommandSource ServerCommandSource source, RankNode rankNode) {
        TextHelper.sendTextByKey(source, "rank.rank_node.id", rankNode.getId());
        TextHelper.sendTextByKey(source, "rank.rank_node.display_name", rankNode.getDisplayName());
        TextHelper.sendTextByKey(source, "rank.rank_node.next_nodes", rankNode.getNextRankNodes());
        return CommandHelper.Return.SUCCESS;
    }
}
