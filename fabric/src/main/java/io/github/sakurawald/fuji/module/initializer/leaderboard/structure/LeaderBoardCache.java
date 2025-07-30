package io.github.sakurawald.fuji.module.initializer.leaderboard.structure;

import io.github.sakurawald.fuji.core.auxiliary.ChronosUtil;
import io.github.sakurawald.fuji.module.initializer.leaderboard.service.LeaderBoardService;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderBoardCache {

    @NotNull String playerName;

    @NotNull List<LeaderBoardSnapshot> snapshots = LeaderBoardSnapshot.makeDefaultList();

    public static LeaderBoardCache of(String playerName) {
        return new LeaderBoardCache(playerName, LeaderBoardSnapshot.makeDefaultList());
    }

    public LeaderBoardSnapshot getSnapshot(@NotNull LeaderBoardTimeWindow timeWindow) {
        return this.snapshots
            .stream()
            .filter(it -> it.getTimeWindow().equals(timeWindow))
            .findFirst()
            .orElseGet(() -> {
                LeaderBoardSnapshot newValue = LeaderBoardSnapshot.of(timeWindow);
                this.snapshots.add(newValue);
                return newValue;
            });
    }

    public void updateSnapshots(long updateTime, int newScore) {
        ZonedDateTime now = ChronosUtil.getZonedDateTime();
        updateSnapshot(newScore, LeaderBoardTimeWindow.HOURLY, ChronosUtil.toTimestamp(ChronosUtil.Boundary.getBeginningOfCurrentHour(now)));
        updateSnapshot(newScore, LeaderBoardTimeWindow.DAILY, ChronosUtil.toTimestamp(ChronosUtil.Boundary.getBeginningOfTheDay(now)));
        updateSnapshot(newScore, LeaderBoardTimeWindow.WEEKLY, ChronosUtil.toTimestamp(ChronosUtil.Boundary.getBeginningOfCurrentWeek(now, LeaderBoardService.getBeginningOfTheWeek())));
        updateSnapshot(newScore, LeaderBoardTimeWindow.MONTHLY, ChronosUtil.toTimestamp(ChronosUtil.Boundary.getBeginningOfCurrentMonth(now)));
        updateSnapshot(newScore, LeaderBoardTimeWindow.YEARLY, ChronosUtil.toTimestamp(ChronosUtil.Boundary.getBeginningOfCurrentYear(now)));
        updateSnapshot(newScore, LeaderBoardTimeWindow.ALL_TIME, 0L);
    }

    private void updateSnapshot(int newScore, @NotNull LeaderBoardTimeWindow timeWindow, long beginningOfCurrentTimeWindow) {
        /* Get the snapshot with specified time window. */
        LeaderBoardSnapshot snapshot = this.getSnapshot(timeWindow);

        /* Update the current score with new score. */
        snapshot.setCurrentScore(newScore);

        /* Initialize the beginning of current time window and previous score. */
        if (snapshot.getBeginningOfCurrentTimeWindow() == null) {
            snapshot.setBeginningOfCurrentTimeWindow(beginningOfCurrentTimeWindow);
            snapshot.setPreviousScore(0);
        } else if (beginningOfCurrentTimeWindow != snapshot.getBeginningOfCurrentTimeWindow()) {
            /* Update the beginning of time window variable with new one. */
            snapshot.setBeginningOfCurrentTimeWindow(beginningOfCurrentTimeWindow);

            /* Copy the current score to previous score, if the beginning of current time window is changed. */
            snapshot.setPreviousScore(snapshot.getCurrentScore());
        }
    }

}
