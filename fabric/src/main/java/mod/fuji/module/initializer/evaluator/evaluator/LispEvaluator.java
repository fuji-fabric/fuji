package mod.fuji.module.initializer.evaluator.evaluator;

import com.google.errorprone.annotations.Keep;
import java.util.ArrayList;
import java.util.List;
import mod.fuji.core.auxiliary.CollectionUtil;
import mod.fuji.core.auxiliary.LogUtil;
import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import org.jetbrains.annotations.NotNull;

public class LispEvaluator {

    @Keep
    final LispList AST;

    public LispEvaluator(LispList AST) {
        this.AST = AST;
    }

    public @NotNull LispObject eval()  {
        List<LispObject> values = new ArrayList<>();

        int size = AST.getNodes().size();
        LogUtil.debug("the children size of AST = {}", size);

        Environment environment = Environment.ofTopLevel();

        for (int i = 0; i < AST.getNodes().size(); i++) {
            LispObject lispObject = AST.getNodes().get(i);
            LispObject eval = lispObject.eval(environment);
            values.add(eval);
        }

        LogUtil.debug("values = {}", values);
        return CollectionUtil.getLast(values);
    }


}
