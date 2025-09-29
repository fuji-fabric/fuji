package mod.fuji.module.initializer.evaluator.evaluator;

import com.google.errorprone.annotations.Keep;
import java.util.ArrayList;
import java.util.List;
import mod.fuji.core.auxiliary.CollectionUtil;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispListNode;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNode;
import org.jetbrains.annotations.NotNull;

public class LispEvaluator {

    @Keep
    final LispListNode AST;

    public LispEvaluator(LispListNode AST) {
        this.AST = AST;
    }

    public @NotNull LispNode eval()  {
        List<LispNode> values = new ArrayList<>();

        int size = AST.getNodes().size();
        LogUtil.warn("the children size of AST = {}", size);

        for (int i = 0; i < AST.getNodes().size(); i++) {
            LispNode lispNode = AST.getNodes().get(i);
            LispNode eval = lispNode.eval();
            values.add(eval);
        }

        LogUtil.warn("values = {}", values);
        return CollectionUtil.getLast(values);
    }


}
