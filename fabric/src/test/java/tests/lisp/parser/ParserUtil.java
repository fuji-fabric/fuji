package tests.lisp.parser;

import java.util.List;
import mod.fuji.module.initializer.evaluator.formatter.PrettyFormatter;
import mod.fuji.module.initializer.evaluator.reader.LispReader;
import mod.fuji.module.initializer.evaluator.reader.token.Token;
import org.jetbrains.annotations.NotNull;

public class ParserUtil {
    static @NotNull List<Token> parseInputString(@NotNull String input) {
        LispReader lispReader = new LispReader(input);
        List<Token> parse = lispReader.read();
        PrettyFormatter.prettyPrint(parse);
        return parse;
    }
}
