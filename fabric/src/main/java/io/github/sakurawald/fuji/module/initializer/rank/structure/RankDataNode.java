package io.github.sakurawald.fuji.module.initializer.rank.structure;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RankDataNode {

    String currentRankNodeId;
    Set<String> walkedRankNodeIds = new HashSet<>();

    public static RankDataNode make() {
        return new RankDataNode();
    }
}
