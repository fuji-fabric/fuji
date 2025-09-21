package mod.fuji.module.initializer.leaderboard.structure;

import mod.fuji.core.document.annotation.Document;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderBoardSnapshot {

    @Document(id = 1753493148676L, value = """
        The `time window` of this `snapshot`.
        """)
    LeaderBoardTimeWindow timeWindow;

    @Document(id = 1753493186554L, value = """
        The `beginning time` of the `time window`.
        If `this value` differs from `the latest value`, then:
        1. This value will be updated with `the latest value`.
        2. The current score will be copied to the previous score.
        """)
    @Nullable Long beginningOfCurrentTimeWindow;

    @Nullable Integer previousScore;
    @Nullable Integer currentScore;

    @ToString.Exclude transient LeaderBoardCache ownerCache;

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

    public boolean hasEffectiveScore() {
        return this.getPreviousScore() != null
            && this.getCurrentScore() != null;
    }

    public int getEffectiveScore() {
        return Math.abs(this.currentScore - this.previousScore);
    }
}
