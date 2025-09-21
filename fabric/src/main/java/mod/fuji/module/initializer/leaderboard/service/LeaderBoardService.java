package mod.fuji.module.initializer.leaderboard.service;


import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.core.auxiliary.minecraft.PlayerHelper;
import mod.fuji.core.auxiliary.minecraft.TextHelper;
import mod.fuji.module.initializer.leaderboard.LeaderBoardInitializer;
import mod.fuji.module.initializer.leaderboard.structure.LeaderBoardCache;
import mod.fuji.module.initializer.leaderboard.structure.LeaderBoardData;
import mod.fuji.module.initializer.leaderboard.structure.LeaderBoardDescriptor;
import mod.fuji.module.initializer.leaderboard.structure.LeaderBoardSnapshot;
import mod.fuji.module.initializer.leaderboard.structure.LeaderBoardTimeWindow;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class LeaderBoardService {

    public static List<LeaderBoardDescriptor> getLeaderBoardDescriptors() {
        return LeaderBoardInitializer.config.model().getLeaderboardDescriptors();
    }

    public static DayOfWeek getBeginningOfTheWeek() {
        return LeaderBoardInitializer.config.model().getBeginningOfTheWeek();
    }

    public static Optional<LeaderBoardDescriptor> findLeaderBoardDescriptor(String leaderBoardId) {
        return getLeaderBoardDescriptors()
            .stream()
            .filter(it -> it.getLeaderboardId().equals(leaderBoardId))
            .findFirst();
    }

    public static int getDefaultPageSize() {
        return LeaderBoardInitializer.config.model().getPageSize();
    }

    public static List<LeaderBoardSnapshot> getLeaderBoardSnapshots(@NotNull LeaderBoardDescriptor descriptor, @NotNull LeaderBoardTimeWindow timeWindow) {
        return withLeaderBoardData(descriptor, leaderBoardData -> leaderBoardData
            .getCaches()
            .stream()
            .map(it -> {
                LeaderBoardSnapshot snapshot = it.getSnapshot(timeWindow);
                snapshot.setOwnerCache(it);
                return snapshot;
            })
            .filter(LeaderBoardSnapshot::hasEffectiveScore)
            .toList());
    }

    private static Optional<LeaderBoardSnapshot> getRankN(@NotNull LeaderBoardDescriptor descriptor, int rankN, boolean reversed, @NotNull LeaderBoardTimeWindow timeWindow) {
        List<LeaderBoardSnapshot> copyOfSnapshots = new ArrayList<>(getLeaderBoardSnapshots(descriptor, timeWindow));
        if (rankN > copyOfSnapshots.size() || rankN <= 0) {
            return Optional.empty();
        }

        copyOfSnapshots.sort(Comparator.comparing(LeaderBoardSnapshot::getEffectiveScore));

        LeaderBoardSnapshot snapshot;
        if (reversed) {
            snapshot = copyOfSnapshots.get(copyOfSnapshots.size() - rankN);
        } else {
            snapshot = copyOfSnapshots.get(rankN - 1);
        }

        return Optional.of(snapshot);
    }

    public static Optional<LeaderBoardSnapshot> getLowestN(@NotNull LeaderBoardDescriptor descriptor, int lowestN, @NotNull LeaderBoardTimeWindow timeWindow) {
        return getRankN(descriptor, lowestN, false, timeWindow);
    }

    public static Optional<LeaderBoardSnapshot> getHighestN(@NotNull LeaderBoardDescriptor descriptor, int highestN, @NotNull LeaderBoardTimeWindow timeWindow) {
        return getRankN(descriptor, highestN, true, timeWindow);
    }

    public static void updateLeaderBoards() {
        PlayerHelper.Lookup
            .getOnlinePlayers()
            .forEach(LeaderBoardService::updateLeaderBoard);
    }

    public static void updateLeaderBoard(ServerPlayerEntity player) {
        LogUtil.debug("Update leaderboards for player {}.", PlayerHelper.getPlayerName(player));
        getLeaderBoardDescriptors()
            .forEach(descriptor -> updateLeaderBoard(descriptor, player));

        LeaderBoardInitializer.data.writeStorage();
    }

    public static void updateLeaderBoard(LeaderBoardDescriptor leaderBoardDescriptor, ServerPlayerEntity player) {
        String leaderboardId = leaderBoardDescriptor.getLeaderboardId();
        String playerName = PlayerHelper.getPlayerName(player);
        LogUtil.debug("Update leaderboard {} for player {}.", leaderboardId, playerName);

        String inputString = leaderBoardDescriptor.getScoreProvider();
        String outputString = TextHelper.Parsers.parsePlaceholderString(player, inputString);

        int score;
        try {
            score = Integer.parseInt(outputString);
        } catch (NumberFormatException e) {
            LogUtil.warn("Failed to parse the score provider: leaderboard = {}, player = {}", leaderBoardDescriptor.getScoreProvider(), playerName);
            return;
        }

        withLeaderBoardCache(leaderBoardDescriptor, playerName, cache -> {
            long updateTime = System.currentTimeMillis();
            cache.updateSnapshots(updateTime, score);
            return null;
        });
    }

    public static Text getNonePlayerText() {
        return TextHelper.getTextByValue(null, LeaderBoardInitializer.config.model().getLeaderboardNoPlayerText());
    }

    public static Text getNoScoreText() {
        return TextHelper.getTextByValue(null, LeaderBoardInitializer.config.model().getLeaderboardNoScoreText());
    }

    private static <T> T withLeaderBoardData(LeaderBoardDescriptor descriptor, Function<LeaderBoardData, T> function) {
        LeaderBoardData leaderBoardData = LeaderBoardInitializer.data.model()
            .getLeaderboardData()
            .stream()
            .filter(it -> it.getLeaderboardId().equals(descriptor.getLeaderboardId()))
            .findFirst()
            .orElseGet(() -> {
                LeaderBoardData newValue = LeaderBoardData.make(descriptor.getLeaderboardId());
                LeaderBoardInitializer.data.model().getLeaderboardData().add(newValue);
                return newValue;
            });

        return function.apply(leaderBoardData);
    }

    private static <T> T withLeaderBoardCache(LeaderBoardDescriptor descriptor, String playerName, Function<LeaderBoardCache, T> function) {
        return withLeaderBoardData(descriptor, (leaderBoardData) -> {
            LeaderBoardCache leaderBoardCache = leaderBoardData
                .getCaches()
                .stream()
                .filter(it -> it.getPlayerName().equals(playerName))
                .findFirst()
                .orElseGet(() -> {
                    LeaderBoardCache newValue = LeaderBoardCache.of(playerName);
                    leaderBoardData.getCaches().add(newValue);
                    return newValue;
                });

            return function.apply(leaderBoardCache);
        });
    }

}
