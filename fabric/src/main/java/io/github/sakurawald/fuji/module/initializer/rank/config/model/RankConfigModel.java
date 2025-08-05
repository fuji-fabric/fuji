package io.github.sakurawald.fuji.module.initializer.rank.config.model;

import io.github.sakurawald.fuji.module.initializer.rank.structure.RankNode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RankConfigModel {

    List<RankNode> graph = new ArrayList<>() {
        {
            /* Define the orphan nodes. */
            RankNode newbie = RankNode.make("newbie", "<dark_green>Newbie", null);
            RankNode branchFirst = RankNode.make("branch-1", "<green>Branch-1", null);
            RankNode branchFirstFirst = RankNode.make("branch-1-1", "<green>Branch-1-1", null);
            RankNode branchFirstSecond = RankNode.make("branch-1-2", "<green>Branch-1-2", null);
            RankNode branchFirstThird = RankNode.make("branch-1-3", "<green>Branch-1-3", null);

            RankNode branchSecond = RankNode.make("branch-2", "<green>Branch-2", null);
            RankNode branchSecondFirst = RankNode.make("branch-2-1", "<green>Branch-2-1", null);
            RankNode branchSecondSecond = RankNode.make("branch-2-2", "<green>Branch-2-2", null);

            /* Add the nodes. */
            this.add(newbie);
            this.add(branchFirst);
            this.add(branchFirstFirst);
            this.add(branchFirstSecond);
            this.add(branchFirstThird);
            this.add(branchSecond);
            this.add(branchSecondFirst);
            this.add(branchSecondSecond);

            /* Connect the nodes. */
            newbie.setNextRankNodes(new ArrayList<>(List.of("branch-1", "branch-2")));

            branchFirst.setNextRankNodes(new ArrayList<>(List.of("branch-1-1", "branch-1-2", "branch-1-3")));
            branchSecond.setNextRankNodes(new ArrayList<>(List.of("branch-2-1", "branch-2-2")));

        }
    };

}
