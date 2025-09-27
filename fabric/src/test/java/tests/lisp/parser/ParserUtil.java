package tests.lisp.parser;

import java.util.List;
import mod.fuji.module.initializer.evaluator.formatter.PrettyFormatter;
import mod.fuji.module.initializer.evaluator.parser.LispParser;
import mod.fuji.module.initializer.evaluator.parser.token.Token;
import org.jetbrains.annotations.NotNull;

public class ParserUtil {
    static @NotNull List<Token> parseInputString(@NotNull String input) {
        LispParser lispParser = new LispParser(input);
        List<Token> parse = lispParser.parse();
        PrettyFormatter.prettyPrint(parse);
        return parse;
    }
}
