package mod.fuji.module.initializer.leaderboard;

import mod.fuji.core.auxiliary.minecraft.CommandHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.core.command.annotation.CommandNode;
import mod.fuji.core.command.annotation.CommandRequirement;
import mod.fuji.core.command.annotation.CommandSource;
import mod.fuji.core.config.handler.abst.BaseConfigurationHandler;
import mod.fuji.core.config.handler.impl.ObjectConfigurationHandler;
import mod.fuji.core.document.annotation.ColorBox;
import mod.fuji.core.document.annotation.Document;
import mod.fuji.core.event.annotation.EventConsumer;
import mod.fuji.core.event.message.player.PlayerJoinedEvent;
import mod.fuji.core.event.message.player.PlayerLeftEvent;
import mod.fuji.core.manager.Managers;
import mod.fuji.core.service.paged_text.PagedMessageText;
import mod.fuji.module.initializer.ModuleInitializer;
import mod.fuji.module.initializer.leaderboard.config.model.LeaderBoardConfigModel;
import mod.fuji.module.initializer.leaderboard.config.model.LeaderBoardDataModel;
import mod.fuji.module.initializer.leaderboard.job.UpdateLeaderboardsJob;
import mod.fuji.module.initializer.leaderboard.service.LeaderBoardService;
import mod.fuji.module.initializer.leaderboard.structure.LeaderBoardDescriptor;
import mod.fuji.module.initializer.leaderboard.structure.LeaderBoardSnapshot;
import mod.fuji.module.initializer.leaderboard.structure.LeaderBoardTimeWindow;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Document(id = 1753466282781L, value = """
    This module defining a `leaderboard`.
    The `score provider` of a `leaderboard` can be a `placeholder`, `statistics` or `objective`.
    The `time window` of a `leaderboard` can be `hourly`, `daily`, `weekly`, `monthly`, `yearly` and `all_time`.
    You can display the `leaderboard` using `placeholders` or `commands`.
    """)
@ColorBox(id = 1753491862403L, color = ColorBox.ColorBoxTypes.NOTE, value = """
    ◉ How it works?
    1. You need to define a `leaderboard` in the config file.
    2. The defined `leaderboards` will be `updated` if:
    2.a. All leaderboards will be updated when a player `joins` or `leaves` the server.
    2.b. All leaderboards will be updated when the `Leaderboard Updater Job` is `fired`.
    2.c. You can use `/leaderboard update-all` command to update manually.
    3. You can `display` the leaderboard in various ways.
    3.a. You can use `placeholders` to display the `leaderboard`.
    3.b. You can use `commands` to display the `leaderboard`.
    """)
@ColorBox(id = 1753498010821L, color = ColorBox.ColorBoxTypes.TIP, value = """
    ◉ You can use a `placeholder` to fetch data from `statistics`.
    For example:
    1. `%player:statistic_raw minecraft:deaths%`
    2. `%player:statistic_raw minecraft:killed minecraft:zombie%`

    See more `statistics` in: https://minecraft.wiki/w/Statistics

    ◉ You can use a `placeholder` to fetch data from `objective`.
    For example: `%player:objective [objective]%`
    """)
@ColorBox(id = 1753492108539L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
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
@ColorBox(id = 1753497840128L, color = ColorBox.ColorBoxTypes.EXAMPLE, value = """
    ◉ List the specified `leaderboard` with `lowest first` order.
    Issue: `/leaderboard lowest death-board ALL_TIME`

    ◉ List the specified `leaderboard` with `highest first` order.
    Issue: `/leaderboard highest zombie-kill-board ALL_TIME`

    ◉ List the specified `leaderboard` with `highest first` order and `daily` time window.
    Issue: `/leaderboard highest zombie-kill-board DAILY`

    ◉ Award the top player.
    Issue:
    1. `/run as console send-broadcast <green>The greatest zombie killer this week is %fuji:highest_n_name zombie-kill-board 1 weekly%`
    2. `/run as console give %fuji:highest_n_name zombie-kill-board 1 weekly% minecraft:diamond 1`
    """)
public class LeaderBoardInitializer extends ModuleInitializer {

    public static final BaseConfigurationHandler<LeaderBoardConfigModel> config = ObjectConfigurationHandler.ofModule(BaseConfigurationHandler.CONFIG_JSON_LITERAL, LeaderBoardConfigModel.class);

    public static final BaseConfigurationHandler<LeaderBoardDataModel> data = ObjectConfigurationHandler.ofModule("leaderboard-data.json", LeaderBoardDataModel.class);

    @Document(id = 1753467248049L, value = "Update all `leaderboards` for `online players`.")
    @CommandNode("leaderboard update-all")
    @CommandRequirement(level = 4)
    private static int $updateAll(@CommandSource ServerCommandSource source) {
        LeaderBoardService.updateLeaderBoards();
        TextHelper.sendTextByKey(source, "leaderboard.update.all");
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1753493701376L, value = "List the lowest N players for specified leaderboard.")
    @CommandNode("leaderboard lowest")
    @CommandRequirement(level = 4)
    private static int $lowest(@CommandSource ServerPlayerEntity player, @NotNull LeaderBoardDescriptor leaderboard, @NotNull LeaderBoardTimeWindow timeWindow, Optional<Integer> pageSize) {
        printLeaderBoardAsPagedMessage(player, leaderboard, timeWindow, pageSize, false);
        return CommandHelper.Return.SUCCESS;
    }

    @Document(id = 1753496925314L, value = "List the highest N players for specified leaderboard.")
    @CommandNode("leaderboard highest")
    @CommandRequirement(level = 4)
    private static int $highest(@CommandSource ServerPlayerEntity player, @NotNull LeaderBoardDescriptor leaderboard, @NotNull LeaderBoardTimeWindow timeWindow, Optional<Integer> pageSize) {
        printLeaderBoardAsPagedMessage(player, leaderboard, timeWindow, pageSize, true);
        return CommandHelper.Return.SUCCESS;
    }

    private static void printLeaderBoardAsPagedMessage(ServerPlayerEntity player, @NotNull LeaderBoardDescriptor leaderboard, @NotNull LeaderBoardTimeWindow timeWindow, Optional<Integer> pageSize, boolean reversed) {
        Integer $pageSize = pageSize
            .filter(i -> i != 0)
            .orElseGet(LeaderBoardService::getDefaultPageSize);

        TextHelper.sendTextByKey(player, "leaderboard.list.header", leaderboard.getDisplayName(), timeWindow.toLanguageValue(player));
        List<LeaderBoardSnapshot> leaderBoardSnapshots = new ArrayList<>(LeaderBoardService.getLeaderBoardSnapshots(leaderboard, timeWindow));
        if (reversed) {
            leaderBoardSnapshots.sort(Comparator.comparing(LeaderBoardSnapshot::getEffectiveScore).reversed());
        } else {
            leaderBoardSnapshots.sort(Comparator.comparing(LeaderBoardSnapshot::getEffectiveScore));
        }
        PagedMessageText pagedMessageText = PagedMessageText.makePagedMessageText(player, leaderBoardSnapshots, $pageSize, (entity, index, pageBuilder) -> {
            int numbering = index + 1;
            String playerName = entity.getOwnerCache().getPlayerName();
            int score = entity.getEffectiveScore();
            Text entryText = TextHelper.getTextByKey(player, "leaderboard.list.entry", numbering, playerName, score);
            pageBuilder.append(entryText);
        });
        pagedMessageText.sendPage(player, 0);
    }

    @Override
    protected void registerPlaceholders() {
        LeaderBoardPlaceholders.registerLowestNPlayerNamePlaceholder();
        LeaderBoardPlaceholders.registerHighestNPlayerNamePlaceholder();

        LeaderBoardPlaceholders.registerLowestNScorePlaceholder();
        LeaderBoardPlaceholders.registerHighestNScorePlaceholder();
    }

    @Override
    protected void onInitialize() {
        Managers.getScheduleManager().addJob(new UpdateLeaderboardsJob());
    }

    @EventConsumer
    private static void updateLeaderBoardOnPlayerJoined(PlayerJoinedEvent event) {
        LeaderBoardService.updateLeaderBoard(event.getPlayer());
    }

    @EventConsumer
    private static void updateLeaderBoardOnPlayerLeft(PlayerLeftEvent event) {
        LeaderBoardService.updateLeaderBoard(event.getPlayer());
    }

}
