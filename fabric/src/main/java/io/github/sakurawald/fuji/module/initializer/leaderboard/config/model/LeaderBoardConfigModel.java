package io.github.sakurawald.fuji.module.initializer.leaderboard.config.model;

import io.github.sakurawald.fuji.module.initializer.leaderboard.structure.LeaderBoardDescriptor;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class LeaderBoardConfigModel {

    String leaderboardNoPlayerText = "<red>None Player";
    String leaderboardNoScoreText = "<yellow>---";

    DayOfWeek beginningOfTheWeek = DayOfWeek.SUNDAY;

    List<LeaderBoardDescriptor> leaderboardDescriptors = new ArrayList<>() {
        {
            this.add(new LeaderBoardDescriptor("death-board", "%player:statistic_raw minecraft:deaths%"));
            this.add(new LeaderBoardDescriptor("zombie-kill-board", "%player:statistic_raw minecraft:killed minecraft:zombie%"));
        }
    };

}
