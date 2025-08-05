package io.github.sakurawald.fuji.module.initializer.rank.structure;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RankNode {

    boolean enable = true;
    String id;
    String displayName;
    List<String> nextRankNodes = new ArrayList<>();

    public static RankNode make(String id, String displayName, List<String> nextRankNodes)  {
        RankNode rankNode = new RankNode();
        rankNode.id = id;
        rankNode.displayName = displayName;
        rankNode.nextRankNodes = nextRankNodes;
        return rankNode;
    }

    Events events = new Events();
    @Data
    @NoArgsConstructor
    public static class Events {
        List<String> onEnterThisRankNodeCommands = new ArrayList<>();
        List<String> onLeaveThisRankNodeCommands = new ArrayList<>();
    }

}
