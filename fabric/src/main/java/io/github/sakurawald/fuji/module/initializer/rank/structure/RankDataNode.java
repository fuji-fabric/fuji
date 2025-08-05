package io.github.sakurawald.fuji.module.initializer.rank.structure;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RankDataNode {

    String currentRankNodeId;

    public static RankDataNode make() {
        return new RankDataNode();
    }
}
