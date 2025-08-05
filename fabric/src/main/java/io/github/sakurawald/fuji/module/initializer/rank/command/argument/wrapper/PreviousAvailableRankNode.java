package io.github.sakurawald.fuji.module.initializer.rank.command.argument.wrapper;

import io.github.sakurawald.fuji.core.command.argument.wrapper.abst.SingularValue;
import io.github.sakurawald.fuji.module.initializer.rank.structure.RankNode;

public class PreviousAvailableRankNode extends SingularValue<RankNode> {
    public PreviousAvailableRankNode(RankNode value) {
        super(value);
    }
}
