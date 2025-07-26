package io.github.sakurawald.fuji.module.initializer.leaderboard;

import io.github.sakurawald.fuji.core.auxiliary.minecraft.CommandHelper;
import io.github.sakurawald.fuji.core.command.annotation.CommandNode;
import io.github.sakurawald.fuji.core.command.annotation.CommandRequirement;
import io.github.sakurawald.fuji.core.command.annotation.CommandSource;
import io.github.sakurawald.fuji.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.fuji.core.document.annotation.ColorBox;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.core.event.impl.PlayerEvents;
import io.github.sakurawald.fuji.core.manager.Managers;
import io.github.sakurawald.fuji.module.initializer.ModuleInitializer;
import io.github.sakurawald.fuji.module.initializer.leaderboard.config.model.LeaderBoardConfigModel;
import io.github.sakurawald.fuji.module.initializer.leaderboard.config.model.LeaderBoardDataModel;
import io.github.sakurawald.fuji.module.initializer.leaderboard.job.UpdateLeaderboardsJob;
import io.github.sakurawald.fuji.module.initializer.leaderboard.service.LeaderBoardService;
import net.minecraft.server.command.ServerCommandSource;

@Document(id = 1753466282781L, value = """
    This module allows you to define a `leaderboard`.
    It can be a `hourly`, `daily`, `weekly`, `monthly`, `yearly` and `all_time` leaderboard.
    You can display the `leaderboard` using `placeholders` or `commands`.
    """)
@ColorBox(id = 1753491862403L, color = ColorBox.ColorBlockTypes.NOTE, value = """
    ◉ How it works?
    1. You need to define a `leaderboard` in the config file.
    2. The defined `leaderboards` will be updated if:
    2.a. All leaderboards will be updated when a player `joins` or `leaves` the server.
    2.b. All leaderboards will be updated when the `Leaderboard Updater Job` is `fired`.
    2.c. You can use `/leaderboard update-all` command to update manually.
    3. You can `display` the leaderboard in various ways.
    3.a. You can use `placeholders` to display the `leaderboard`.
    3.b. You can use `commands` to display the `leaderboard`.
    """)
@ColorBox(id = 1753492108539L, color = ColorBox.ColorBlockTypes.EXAMPLE, value = """
    ◉ How to define a `leaderboard`?
    To define a `leaderboard`, you need to specify:
    1. `Leaderboard ID`: The unique name for this leaderboard.
    2. `Score Provider`: It's a string which evaluates to a `numeric value`. (Typically a `placeholder`)

    ◉ What is the `lowest N` and `highest N`.
    For any leaderboard, all its `scores` are `sorted` in `natural order`.
    The `natural order` is the order that from `lowest` to `highest`. (e.g. `0`, `1`, `2`, `3` ...)

    You can use a `lowest placeholder` to select `the lowest N score`.
    You can use a `highest placeholder` to select `the highest N score`.

    For example, you may want to `select` the `lowest score` for `death-board`, and `highest score` for `zombie-kill-board`.
    1. `%fuji:lowest_n_score death-board 1 all_time%`
    2. `%fuji:highest_n_score zombie-kill-board 1 all_time%`
    """)
public class LeaderBoardInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<LeaderBoardConfigModel> config = new ObjectConfigurationHandler<>(BaseConfigurationHandler.CONFIG_JSON, LeaderBoardConfigModel.class);

    public static final BaseConfigurationHandler<LeaderBoardDataModel> data = new ObjectConfigurationHandler<>("leaderboard-data.json", LeaderBoardDataModel.class);

    @Document(id = 1753467248049L, value = "Update all `leaderboards` for `online players`.")
    @CommandNode("leaderboard update-all")
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
        Managers.getScheduleManager().scheduleJob(new UpdateLeaderboardsJob());
    }
}
