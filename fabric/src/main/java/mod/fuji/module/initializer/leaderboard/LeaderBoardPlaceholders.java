package mod.fuji.module.initializer.leaderboard;

import mod.fuji.core.auxiliary.StringUtil;
import mod.fuji.core.auxiliary.minecraft.PlaceholderHelper;
import mod.fuji.core.document.annotation.DocStringProvider;
import mod.fuji.core.document.descriptor.PlaceholderDescriptor;
import mod.fuji.module.initializer.leaderboard.service.LeaderBoardService;
import mod.fuji.module.initializer.leaderboard.structure.LeaderBoardArgumentsParseResult;
import mod.fuji.module.initializer.leaderboard.structure.LeaderBoardDescriptor;
import mod.fuji.module.initializer.leaderboard.structure.LeaderBoardTimeWindow;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;

public class LeaderBoardPlaceholders {

    @DocStringProvider(id = 1753468955141L, value = """
        Returns the `lowest N` player name of specified `leaderboard id`.

        The syntax is `%fuji:lowest_n_name \\<leaderboard-id\\> \\<n\\> \\<time-window\\>`
        The `time window` can be `hourly`, `daily`, `weekly`, `monthly`, `yearly` and `all_time`.

        For example:
        The `%fuji:lowest_n_name death-board 1 all_time%` returns `the name of the player with the least number of death` from the leaderboard with the id `death-board`.
        """)
    public static void registerLowestNPlayerNamePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("lowest_n_name", 1753468955141L);
        PlaceholderHelper.registerServerPlaceholder(descriptor, (server, args) -> {
            LeaderBoardArgumentsParseResult result = verifyRankNArguments(args);
            if (result.getErrorText() != null) {
                return result.getErrorText();
            }
            return LeaderBoardService
                .getLowestN(result.getLeaderBoardDescriptor(), result.getRankN(), result.getTimeWindow())
                .map(it -> Component.nullToEmpty(it.getOwnerCache().getPlayerName()))
                .orElseGet(LeaderBoardService::getNonePlayerText);
        });
    }

    @DocStringProvider(id = 1753471573263L, value = """
        Returns the `lowest N` score of specified `leaderboard id`.

        The syntax is `%fuji:lowest_n_score \\<leaderboard-id\\> \\<n\\> \\<time-window\\>`
        The `time window` can be `hourly`, `daily`, `weekly`, `monthly`, `yearly` and `all_time`.

        For example:
        The `%fuji:lowest_n_score death-board 1 all_time%` returns the `the least number of death score` from the leaderboard with the id `death-board`.
        """)
    public static void registerLowestNScorePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("lowest_n_score", 1753471573263L);
        PlaceholderHelper.registerServerPlaceholder(descriptor, (server, args) -> {
            LeaderBoardArgumentsParseResult result = verifyRankNArguments(args);
            if (result.getErrorText() != null) {
                return result.getErrorText();
            }
            return LeaderBoardService
                .getLowestN(result.getLeaderBoardDescriptor(), result.getRankN(), result.getTimeWindow())
                .map(it -> Component.nullToEmpty(String.valueOf(it.getEffectiveScore())))
                .orElseGet(LeaderBoardService::getNoScoreText);
        });
    }

    @DocStringProvider(id = 1753476135481L, value = """
        Returns the `highest N` player name of specified `leaderboard id`.

        The syntax is `%fuji:highest_n_name \\<leaderboard-id\\> \\<n\\> \\<time-window\\>`
        The `time window` can be `hourly`, `daily`, `weekly`, `monthly`, `yearly` and `all_time`.

        For example:
        The `%fuji:highest_n_name zombie-kill-board 1 all_time%` returns `the name of the player with the highest number of zombie kills` from the leaderboard with the id `zombie-kill-board`.
        """)
    public static void registerHighestNPlayerNamePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("highest_n_name", 1753476135481L);
        PlaceholderHelper.registerServerPlaceholder(descriptor, (server, args) -> {
            LeaderBoardArgumentsParseResult result = verifyRankNArguments(args);
            if (result.getErrorText() != null) {
                return result.getErrorText();
            }
            return LeaderBoardService
                .getHighestN(result.getLeaderBoardDescriptor(), result.getRankN(), result.getTimeWindow())
                .map(it -> Component.nullToEmpty(it.getOwnerCache().getPlayerName()))
                .orElseGet(LeaderBoardService::getNonePlayerText);
        });
    }

    @DocStringProvider(id = 1753476193458L, value = """
        Returns the `highest N` score of specified `leaderboard id`.

        The syntax is `%fuji:highest_n_score \\<leaderboard-id\\> \\<n\\> \\<time-window\\>`
        The `time window` can be `hourly`, `daily`, `weekly`, `monthly`, `yearly` and `all_time`.

        For example:
        The `%fuji:highest_n_score zombie-kill-board 1 all_time%` returns `the highest zombie kill score` from the leaderboard with the id `zombie-kill-board`.
        """)
    public static void registerHighestNScorePlaceholder() {
        PlaceholderDescriptor descriptor = new PlaceholderDescriptor("highest_n_score", 1753476193458L);
        PlaceholderHelper.registerServerPlaceholder(descriptor, (server, args) -> {
            LeaderBoardArgumentsParseResult result = verifyRankNArguments(args);
            if (result.getErrorText() != null) {
                return result.getErrorText();
            }
            return LeaderBoardService
                .getHighestN(result.getLeaderBoardDescriptor(), result.getRankN(), result.getTimeWindow())
                .map(it -> Component.nullToEmpty(String.valueOf(it.getEffectiveScore())))
                .orElseGet(LeaderBoardService::getNoScoreText);
        });
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    private static LeaderBoardArgumentsParseResult verifyRankNArguments(String args) {
        /* Verify arity. */
        List<String> stringArgs = PlaceholderHelper.splitArguments(args);
        if (stringArgs.isEmpty()) {
            return new LeaderBoardArgumentsParseResult(Component.literal("[NO-LEADER-BOARD-ID-SPECIFIED]"), null, null, null);
        }
        if (stringArgs.size() == 1) {
            return new LeaderBoardArgumentsParseResult(Component.literal("[NO-RANK-N-SPECIFIED]"), null, null, null);
        }
        if (stringArgs.size() == 2) {
            return new LeaderBoardArgumentsParseResult(Component.literal("[NO-TIME-WINDOW-SPECIFIED]"), null, null, null);
        }

        /* Parse the leaderboard id. */
        String leaderBoardId = stringArgs.get(0);
        Optional<LeaderBoardDescriptor> leaderBoardDescriptor = LeaderBoardService.findLeaderBoardDescriptor(leaderBoardId);
        if (leaderBoardDescriptor.isEmpty()) {
            return new LeaderBoardArgumentsParseResult(Component.literal("[SPECIFIED-LEADERBOARD-NOT-FOUND]"), null, null, null);
        }
        LeaderBoardDescriptor $leaderBoardDescriptor = leaderBoardDescriptor.get();

        /* Parse the rankN. */
        String rankNString = stringArgs.get(1);
        int rankN;
        try {
            rankN = Integer.parseInt(rankNString);
        } catch (NumberFormatException e) {
            return new LeaderBoardArgumentsParseResult(Component.literal("[FAILED-TO-PARSE-RANK-N-INTO-INTEGER]"), null, null, null);
        }

        /* Parse the time window. */
        String timeWindowString = stringArgs.get(2);
        LeaderBoardTimeWindow timeWindow;
        try {
            timeWindow = LeaderBoardTimeWindow.valueOf(StringUtil.toUpperCase(timeWindowString));
        } catch (IllegalArgumentException e) {
            return new LeaderBoardArgumentsParseResult(Component.literal("[INVALID-TIME-WINDOW]"), null, null, null);
        }


        return new LeaderBoardArgumentsParseResult(null, $leaderBoardDescriptor, rankN, timeWindow);
    }
}
