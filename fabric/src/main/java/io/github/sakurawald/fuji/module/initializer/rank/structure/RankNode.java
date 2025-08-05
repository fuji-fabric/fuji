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

    Requirements requirements = new Requirements();
    @Data
    @NoArgsConstructor
    public static class Requirements {
        List<String> predicateCommands = new ArrayList<>() {
            {


            }
        };
    }

    Events events = new Events();
    @Data
    @NoArgsConstructor
    public static class Events {
        List<String> onEnterThisRankNodeCommands = new ArrayList<>() {
            {
                this.add("lp user %player:name% permission set group.rank_id");
                this.add("send-broadcast <pink>Player %player:name% has been ranked up to ");
            }
        };
        List<String> onLeaveThisRankNodeCommands = new ArrayList<>() {
            {
                this.add("lp user %player:name% permission unset group.rank_id");
            }
        };
        List<String> onFirstEnterThisRankNodeCommands = new ArrayList<>() {
            {
                this.add("when-online %player:name% send-message %player:name% <orange>You have received the ranked up bonus!");
                this.add("when-online %player:name% give %player:name% minecraft:apple 1");

            }
        };
    }

}
