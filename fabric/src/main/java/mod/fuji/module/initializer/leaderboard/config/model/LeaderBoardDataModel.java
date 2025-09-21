package mod.fuji.module.initializer.leaderboard.config.model;

import mod.fuji.module.initializer.leaderboard.structure.LeaderBoardData;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LeaderBoardDataModel {

    List<LeaderBoardData> leaderboardData = new ArrayList<>();

}
