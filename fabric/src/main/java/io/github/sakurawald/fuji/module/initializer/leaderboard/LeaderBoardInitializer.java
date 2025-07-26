package io.github.sakurawald.fuji.module.initializer.leaderboard;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.impl.PlayerEvents;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.leaderboard.config.model.LeaderBoardConfigModel;
import io.github.sakurawald.fuji.module.initializer.leaderboard.config.model.LeaderBoardDataModel;
import io.github.sakurawald.fuji.module.initializer.leaderboard.service.LeaderBoardService;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1753466282781L, value = """
    This module allows you to define a `leaderboard`.
    You can display the `leaderboard` using `placeholders` or `commands`.
    """)
public class LeaderBoardInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<LeaderBoardConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, LeaderBoardConfigModel.class);

    public static final BaseConfigurationHandler<LeaderBoardDataModel> data = new ObjectConfigurationHandler<>("leaderboard-data.json", LeaderBoardDataModel.class);

    @Document(id = 1753467248049L, value = "Update all `leaderboards` for `online players`.")
    @CommandNode("leaderboard update all")
    @CommandRequirement(level = 4)
    private static int $updateAll(@CommandSource ServerCommandSource source) {
        LeaderBoardService.updateLeaderBoards();
        return CommandHelper.Return.SUCCESS;
    }

    @Override
    protected void registerPlaceholder() {
        LeaderBoardPlaceholders.registerLowestNPlayerNamePlaceholder();
        LeaderBoardPlaceholders.registerHighestNPlayerNamePlaceholder();

        LeaderBoardPlaceholders.registerLowestNScorePlaceholder();
        LeaderBoardPlaceholders.registerHighestNScorePlaceholder();
    }

    @Override
    protected void onInitialize() {
        PlayerEvents.ON_PLAYER_JOINED.register(LeaderBoardService::updateLeaderBoard);
        PlayerEvents.ON_PLAYER_LEAVE.register(LeaderBoardService::updateLeaderBoard);
    }
}
