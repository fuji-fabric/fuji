package mod.fuji.module.initializer.leaderboard.structure;

import mod.fuji.core.document.annotation.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderBoardDescriptor {

    @Document(id = 1753466744942L, value = """
        The `unique` id for this `leaderboard`.
        """)
    String leaderboardId;

    String displayName;

    @Document(id = 1753466735721L, value = """
        This option is a `string` which will be evaluated to a `numeric value`.
        The `string` value is typically a `placeholder`.
        """)
    String scoreProvider;

}
