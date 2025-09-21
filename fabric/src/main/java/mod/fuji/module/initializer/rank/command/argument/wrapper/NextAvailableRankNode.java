package mod.fuji.module.initializer.rank.command.argument.wrapper;

import mod.fuji.core.command.argument.wrapper.abst.SingularValue;
import mod.fuji.module.initializer.rank.structure.RankNode;

public class NextAvailableRankNode extends SingularValue<RankNode> {

    public NextAvailableRankNode(RankNode value) {
        super(value);
    }
}
