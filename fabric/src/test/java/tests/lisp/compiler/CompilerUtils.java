package tests.lisp.compiler;

import java.util.List;
import mod.fuji.module.initializer.evaluator.evaluator.compiler.LispCompiler;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.reader.LispReader;
import mod.fuji.module.initializer.evaluator.reader.token.Token;
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
