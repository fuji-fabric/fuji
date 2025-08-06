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
    String description = "<orange>This is the first line.\n<orange>This is the second line.";
    List<String> nextRankNodes = new ArrayList<>();

    public static RankNode make(String id, String displayName, List<String> nextRankNodes)  {
        RankNode rankNode = new RankNode();
        rankNode.id = id;
        rankNode.displayName = displayName;
        rankNode.nextRankNodes = nextRankNodes;
        return rankNode;
    }

    List<RankRequirement> requirements = new ArrayList<>() {
        {
            this.add(new RankRequirement("Requires 16 dirt blocks in your inventory.", List.of("has-item? %player:name% minecraft:dirt 16")));
            this.add(new RankRequirement("Requires 8 apples in your inventory.", List.of("has-item? %player:name% minecraft:apple 8")));
            this.add(new RankRequirement("Requires 4 diamonds in your inventory.", List.of("has-item? %player:name% minecraft:diamond 4")));
        }
    };

    Events events = new Events();
    @Data
    @NoArgsConstructor
    public static class Events {
        List<String> onEnterThisRankNodeCommands = new ArrayList<>() {
            {
                this.add("lp user %player:name% permission set group.rank_id");
                this.add("send-broadcast <#FFA1F5>Player %player:name% has been ranked up to %fuji:rank_displayname_raw%");
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
