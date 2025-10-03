package lisp.evaluator;

import java.util.List;
import mod.fuji.evaluator.evaluator.LispEvaluator;
import mod.fuji.evaluator.evaluator.compiler.LispCompiler;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispObject;
import mod.fuji.evaluator.reader.LispReader;
import mod.fuji.evaluator.reader.token.Token;
import org.jetbrains.annotations.NotNull;

public class EvaluatorUtils {

    public static @NotNull LispObject evaluate(@NotNull String code) {
        LispReader lispReader = new LispReader(code);
        List<Token> read = lispReader.read();

        LispCompiler lispCompiler = new LispCompiler(read);
        LispList AST = lispCompiler.compile();

        LispEvaluator lispEvaluator = new LispEvaluator(AST);

        return lispEvaluator.eval();
    }
}
