package mod.fuji.evaluator.evaluator;

import java.util.ArrayList;
import java.util.List;
import mod.fuji.evaluator.auxiliary.CollectionUtil;
import mod.fuji.evaluator.auxiliary.LogUtil;
import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class LispEvaluator {

    final LispList AST;

    public LispEvaluator(LispList AST) {
        this.AST = AST;
    }

    public @NotNull LispObject eval()  {
        List<LispObject> values = new ArrayList<>();

        int size = AST.getObjects().size();
        LogUtil.debug("the children size of AST = {}", size);

        LispEnvironment environment = LispEnvironment.ofNullLexical();

        for (int i = 0; i < size; i++) {
            LispObject lispObject = AST.getObjects().get(i);
            LispObject eval = lispObject.eval(environment);
            values.add(eval);
        }

        LogUtil.debug("values = {}", values);
        return CollectionUtil.getLast(values);
    }


}
