package tests.lisp.parser;

import java.util.List;
import mod.fuji.core.document.annotation.ForDeveloper;
import mod.fuji.module.initializer.evaluator.parser.structure.StringRange;
import mod.fuji.module.initializer.evaluator.parser.token.Token;
import mod.fuji.module.initializer.evaluator.parser.token.TokenType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class BoolParserTest {

    @ForDeveloper("""
        The `boolean value` is a `symbol`.
        The `nil` symbol is treated as `false value`.
        Any data object other than `nil` symbol is treated as `true value`.
        All the assumptions are made by `boolean test functions`.
        The `boolean test functions` describe the `truth value`.
        """)
    @Test
    void testFalseValueSymbol() {
        List<Token> actual = ParserUtil.parseInputString("nil");
        List<Token> expected = List.of(
            Token.of(TokenType.SYMBOL, StringRange.of(0, 3), "nil")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testFalseValueSymbolDenotation() {
        List<Token> actual = ParserUtil.parseInputString("()");
        List<Token> expected = List.of(
            Token.of(TokenType.OPEN_PARENTHESES, StringRange.of(0, 1), "("),
            Token.of(TokenType.CLOSED_PARENTHESES, StringRange.of(1, 2), ")")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testConventionalTrueValueSymbol() {
        List<Token> actual = ParserUtil.parseInputString("t");
        List<Token> expected = List.of(
            Token.of(TokenType.SYMBOL, StringRange.of(0, 1), "t")
        );
        assertEquals(expected, actual);
    }
}
