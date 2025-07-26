package io.github.sakurawald.fuji.module.initializer.leaderboard.structure;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderBoardData {

    String leaderboardId;

    List<LeaderBoardCache> caches = new ArrayList<>();

    public static LeaderBoardData of(String leaderboardId) {
        LeaderBoardData leaderBoardData = new LeaderBoardData();
        leaderBoardData.setLeaderboardId(leaderboardId);
        leaderBoardData.setCaches(new ArrayList<>());
        return leaderBoardData;
    }
}
