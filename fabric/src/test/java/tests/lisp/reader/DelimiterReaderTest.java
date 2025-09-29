package tests.lisp.reader;

import java.util.List;
import mod.fuji.module.initializer.evaluator.reader.structure.StringRange;
import mod.fuji.module.initializer.evaluator.reader.token.Token;
import mod.fuji.module.initializer.evaluator.reader.token.TokenType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class DelimiterReaderTest {

    @Test
    void testCompatSymbolAndString() {
        List<Token> actual = ReaderUtil.readInputString("(a\"b\")");
        List<Token> expected = List.of(
            Token.of(TokenType.BEGIN_LIST, StringRange.of(0, 1), "("),
            Token.of(TokenType.SYMBOL, StringRange.of(1, 2), "a"),
            Token.of(TokenType.STRING, StringRange.of(2, 5), "\"b\""),
            Token.of(TokenType.END_LIST, StringRange.of(5, 6), ")")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testCompatNumberAndString() {
        List<Token> actual = ReaderUtil.readInputString("(123\"456\")");
        List<Token> expected = List.of(
            Token.of(TokenType.BEGIN_LIST, StringRange.of(0, 1), "("),
            Token.of(TokenType.NUMBER, StringRange.of(1, 4), "123"),
            Token.of(TokenType.STRING, StringRange.of(4, 9), "\"456\""),
            Token.of(TokenType.END_LIST, StringRange.of(9, 10), ")")
        );
        assertEquals(expected, actual);
    }

}
