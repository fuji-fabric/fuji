package io.github.sakurawald.fuji.module.initializer.leaderboard.config.model;

import io.github.sakurawald.fuji.core.document.annotation.Document;
import io.github.sakurawald.fuji.module.initializer.leaderboard.structure.LeaderBoardDescriptor;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class LeaderBoardConfigModel {

    @Document(id = 1753491705006L, value = """
        The `text` to display when there is no data for specified `lowest N` or `highest N` player.
        """)
    String leaderboardNoPlayerText = "<red>None Player";

    @Document(id = 1753491679192L, value = """
        The `text` to display when there is no data for specified `score`.
        """)
    String leaderboardNoScoreText = "<yellow>---";

    @Document(id = 1753491756650L, value = """
        The beginning of the week used for `weekly` time window.
        """)
    DayOfWeek beginningOfTheWeek = DayOfWeek.SUNDAY;

    @Document(id = 1753491782458L, value = """
        A `leaderboard descriptor` is used to define a `leaderboard`.
        """)
    List<LeaderBoardDescriptor> leaderboardDescriptors = new ArrayList<>() {
        {
            this.add(new LeaderBoardDescriptor("death-board", "%player:statistic_raw minecraft:deaths%"));
            this.add(new LeaderBoardDescriptor("zombie-kill-board", "%player:statistic_raw minecraft:killed minecraft:zombie%"));
        }
    };

}
