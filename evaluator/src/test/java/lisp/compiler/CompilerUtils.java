package lisp.compiler;

import java.util.List;
import mod.fuji.evaluator.evaluator.compiler.LispCompiler;
import mod.fuji.evaluator.evaluator.value.LispObject;
import mod.fuji.evaluator.reader.LispReader;
import mod.fuji.evaluator.reader.token.Token;
import org.jetbrains.annotations.NotNull;

public class CompilerUtils {

    public static @NotNull LispObject compile(@NotNull String code) {
        LispReader lispReader = new LispReader(code);
        List<Token> read = lispReader.read();
        LispCompiler lispCompiler = new LispCompiler(read);
        LispObject compile = lispCompiler.compile();
        return compile;
    }

}
