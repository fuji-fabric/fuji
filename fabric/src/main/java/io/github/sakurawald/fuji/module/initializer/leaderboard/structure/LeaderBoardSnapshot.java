package io.github.sakurawald.fuji.module.initializer.leaderboard.structure;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderBoardSnapshot {

    @NotNull LeaderBoardTimeWindow timeWindow;
    @Nullable Long beginningOfCurrentTimeWindow;
    @Nullable Integer previousScore;
    @Nullable Integer currentScore;

    transient LeaderBoardCache ownerCache;

    public static LeaderBoardSnapshot of(@NotNull LeaderBoardTimeWindow timeWindowType) {
        LeaderBoardSnapshot leaderBoardSnapshot = new LeaderBoardSnapshot();
        leaderBoardSnapshot.timeWindow = timeWindowType;
        return leaderBoardSnapshot;
    }

    public static List<LeaderBoardSnapshot> makeDefaultList() {
        ArrayList<LeaderBoardSnapshot> list = new ArrayList<>();
        list.add(LeaderBoardSnapshot.of(LeaderBoardTimeWindow.HOURLY));
        list.add(LeaderBoardSnapshot.of(LeaderBoardTimeWindow.DAILY));
        list.add(LeaderBoardSnapshot.of(LeaderBoardTimeWindow.WEEKLY));
        list.add(LeaderBoardSnapshot.of(LeaderBoardTimeWindow.MONTHLY));
        list.add(LeaderBoardSnapshot.of(LeaderBoardTimeWindow.YEARLY));
        list.add(LeaderBoardSnapshot.of(LeaderBoardTimeWindow.ALL_TIME));
        return list;
    }

    public boolean hasDistance() {
        return this.getPreviousScore() != null
            && this.getCurrentScore() != null;
    }

    public int getDistance() {
        return Math.abs(this.currentScore - this.previousScore);
    }
}
