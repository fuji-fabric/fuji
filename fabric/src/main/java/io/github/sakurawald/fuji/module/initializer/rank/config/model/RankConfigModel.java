package io.github.sakurawald.fuji.module.initializer.rank.config.model;

import io.github.sakurawald.fuji.module.initializer.rank.structure.RankNode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RankConfigModel {

    String startingRankNodeId = "newbie";

    String noRankStatusText = "<grey>[No Rank]";

    List<RankNode> graph = new ArrayList<>() {
        {
            /* Define the orphan nodes. */
            RankNode newbie = RankNode.make("newbie", "<dark_grey>Newbie", List.of());
            RankNode branchFirst = RankNode.make("branch-1", "<pink>Branch-1", List.of());
            RankNode branchFirstFirst = RankNode.make("branch-1-1", "<pink>Branch-1-1", List.of());
            RankNode branchFirstSecond = RankNode.make("branch-1-2", "<pink>Branch-1-2", List.of());
            RankNode branchFirstThird = RankNode.make("branch-1-3", "<pink>Branch-1-3", List.of());

            RankNode branchSecond = RankNode.make("branch-2", "<aqua>Branch-2", List.of());
            RankNode branchSecondFirst = RankNode.make("branch-2-1", "<aqua>Branch-2-1", List.of());
            RankNode branchSecondSecond = RankNode.make("branch-2-2", "<aqua>Branch-2-2", List.of());

            RankNode expert = RankNode.make("expert", "<purple>Expert", List.of());


            /* Add the nodes. */
            this.add(newbie);
            this.add(branchFirst);
            this.add(branchFirstFirst);
            this.add(branchFirstSecond);
            this.add(branchFirstThird);
            this.add(branchSecond);
            this.add(branchSecondFirst);
            this.add(branchSecondSecond);
            this.add(expert);

            /* Connect the nodes. */
            newbie.setNextRankNodes(new ArrayList<>(List.of("branch-1", "branch-2")));

            branchFirst.setNextRankNodes(new ArrayList<>(List.of("branch-1-1", "branch-1-2", "branch-1-3")));
            branchSecond.setNextRankNodes(new ArrayList<>(List.of("branch-2-1", "branch-2-2")));

            branchFirstFirst.setNextRankNodes(new ArrayList<>(List.of("expert")));
            branchFirstSecond.setNextRankNodes(new ArrayList<>(List.of("expert")));
            branchFirstThird.setNextRankNodes(new ArrayList<>(List.of("expert")));

            branchSecondFirst.setNextRankNodes(new ArrayList<>(List.of("expert")));
            branchSecondSecond.setNextRankNodes(new ArrayList<>(List.of("expert")));

        }
    };

}
