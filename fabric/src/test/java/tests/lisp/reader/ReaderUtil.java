package tests.lisp.reader;

import java.util.List;
import mod.fuji.module.initializer.evaluator.formatter.PrettyFormatter;
import mod.fuji.module.initializer.evaluator.reader.LispReader;
import mod.fuji.module.initializer.evaluator.reader.token.Token;
import org.jetbrains.annotations.NotNull;

public class ReaderUtil {
    static @NotNull List<Token> readInputString(@NotNull String input) {
        LispReader lispReader = new LispReader(input);
        List<Token> read = lispReader.read();
        PrettyFormatter.prettyPrint(read);
        return read;
    }
}
