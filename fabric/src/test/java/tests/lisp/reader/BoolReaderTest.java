package tests.lisp.reader;

import java.util.List;
import mod.fuji.core.document.annotation.ForDeveloper;
import mod.fuji.module.initializer.evaluator.reader.structure.StringRange;
import mod.fuji.module.initializer.evaluator.reader.token.Token;
import mod.fuji.module.initializer.evaluator.reader.token.TokenType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class BoolReaderTest {

    @ForDeveloper("""
        The `boolean value` is a `symbol`.
        The `nil` symbol is treated as `false value`.
        Any data object other than `nil` symbol is treated as `true value`.
        All the assumptions are made by `boolean test functions`.
        The `boolean test functions` describe the `truth value`.
        """)
    @Test
    void testFalseValueSymbol() {
        List<Token> actual = ReaderUtil.readInputString("nil");
        List<Token> expected = List.of(
            Token.of(TokenType.SYMBOL, StringRange.of(0, 3), "nil")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testFalseValueSymbolDenotation() {
        List<Token> actual = ReaderUtil.readInputString("()");
        List<Token> expected = List.of(
            Token.of(TokenType.BEGIN_LIST, StringRange.of(0, 1), "("),
            Token.of(TokenType.END_LIST, StringRange.of(1, 2), ")")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testConventionalTrueValueSymbol() {
        List<Token> actual = ReaderUtil.readInputString("t");
        List<Token> expected = List.of(
            Token.of(TokenType.SYMBOL, StringRange.of(0, 1), "t")
        );
        assertEquals(expected, actual);
    }
}
