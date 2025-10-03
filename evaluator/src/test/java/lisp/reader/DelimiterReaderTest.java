package lisp.reader;

import java.util.List;
import mod.fuji.evaluator.reader.structure.StringRange;
import mod.fuji.evaluator.reader.token.Token;
import mod.fuji.evaluator.reader.token.TokenType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DelimiterReaderTest {

    @Test
    void testStringFollowingSymbolInList() {
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
    void testStringFollowingNumberInList() {
        List<Token> actual = ReaderUtil.readInputString("(123\"456\")");
        List<Token> expected = List.of(
            Token.of(TokenType.BEGIN_LIST, StringRange.of(0, 1), "("),
            Token.of(TokenType.NUMBER, StringRange.of(1, 4), "123"),
            Token.of(TokenType.STRING, StringRange.of(4, 9), "\"456\""),
            Token.of(TokenType.END_LIST, StringRange.of(9, 10), ")")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testSingleStringFollowingNumber() {
        List<Token> actual = ReaderUtil.readInputString("123\"456\"");
        List<Token> expected = List.of(
            Token.of(TokenType.NUMBER, StringRange.of(0, 3), "123"),
            Token.of(TokenType.STRING, StringRange.of(3, 8), "\"456\"")
        );
        assertEquals(expected, actual);
    }

    @Test
    @Disabled("FIXME")
    void testDoubleStringFollowingNumber() {
        List<Token> actual = ReaderUtil.readInputString("123\"456\"\"789\"");
        List<Token> expected = List.of(
            Token.of(TokenType.NUMBER, StringRange.of(0, 3), "123"),
            Token.of(TokenType.STRING, StringRange.of(3, 8), "\"456\""),
            Token.of(TokenType.STRING, StringRange.of(9, 14), "\"789\"")
        );
        assertEquals(expected, actual);
    }

    @Test
    @Disabled("FIXME")
    void testDoubleStringFollowingNumberAndEndsWithNumber() {
        List<Token> actual = ReaderUtil.readInputString("123\"456\"\"789\"0");
        List<Token> expected = List.of(
            Token.of(TokenType.NUMBER, StringRange.of(0, 3), "123"),
            Token.of(TokenType.STRING, StringRange.of(3, 8), "\"456\""),
            Token.of(TokenType.STRING, StringRange.of(9, 14), "\"789\""),
            Token.of(TokenType.NUMBER, StringRange.of(14, 15), "0")
        );
        assertEquals(expected, actual);
    }
}
