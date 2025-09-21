package mod.fuji.module.initializer.leaderboard.config.model;

import mod.fuji.core.document.annotation.Document;
import mod.fuji.module.initializer.leaderboard.structure.LeaderBoardDescriptor;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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

    @Document(id = 1753496184086L, value = """
        The `default page size` for `/leaderboard lowest` and `/leaderboard highest` commands.
        """)
    int pageSize = 10;

    @Document(id = 1753491782458L, value = """
        A `leaderboard descriptor` is used to define a `leaderboard`.
        """)
    List<LeaderBoardDescriptor> leaderboardDescriptors = new ArrayList<>() {
        {
            this.add(new LeaderBoardDescriptor("death-board", "<dark_red>Death Board</dark_red>","%player:statistic_raw minecraft:deaths%"));
            this.add(new LeaderBoardDescriptor("zombie-kill-board", "<blue>Zombie Kills</blue>","%player:statistic_raw minecraft:killed minecraft:zombie%"));
        }
    };

}
