package mod.fuji.module.initializer.leaderboard.structure;

import java.util.concurrent.CopyOnWriteArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderBoardData {

    String leaderboardId;

    CopyOnWriteArrayList<LeaderBoardCache> caches = new CopyOnWriteArrayList<>();

    public static LeaderBoardData make(String leaderboardId) {
        LeaderBoardData leaderBoardData = new LeaderBoardData();
        leaderBoardData.setLeaderboardId(leaderboardId);
        leaderBoardData.setCaches(new CopyOnWriteArrayList<>());
        return leaderBoardData;
    }
}
