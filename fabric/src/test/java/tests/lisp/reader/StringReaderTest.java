package tests.lisp.reader;

import java.util.List;
import mod.fuji.module.initializer.evaluator.reader.exception.LispSyntaxException;
import mod.fuji.module.initializer.evaluator.reader.structure.StringRange;
import mod.fuji.module.initializer.evaluator.reader.token.Token;
import mod.fuji.module.initializer.evaluator.reader.token.TokenType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class StringReaderTest {

    @Test
    void testSimpleString() {
        List<Token> actual = ReaderUtil.readInputString("\"abc\"");
        List<Token> expected = List.of(
            Token.of(TokenType.STRING, StringRange.of(0, 5), "\"abc\"")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testEmptyString() {
        List<Token> actual = ReaderUtil.readInputString("\"\"");
        List<Token> expected = List.of(
            Token.of(TokenType.STRING, StringRange.of(0, 2), "\"\"")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testDoubleQuoteCharacterInSymbolName() {
        assertThrows(LispSyntaxException.class, () -> {
            ReaderUtil.readInputString("abc\"");
        });
    }

    @Test
    void testUnclosedString() {
        assertThrows(LispSyntaxException.class, () -> {
            ReaderUtil.readInputString("\"abc");
        });
    }

    @Test
    void testStringContainsBlankCharacters() {
        List<Token> actual = ReaderUtil.readInputString("\"abc   def\"");
        List<Token> expected = List.of(
            Token.of(TokenType.STRING, StringRange.of(0, 11), "\"abc   def\"")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testDoubleQuoteEscape() {
        List<Token> actual = ReaderUtil.readInputString("\"a \\\"b c\"");
        List<Token> expected = List.of(
            Token.of(TokenType.STRING, StringRange.of(0, 9), "\"a \\\"b c\"")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testBackSlashEscape() {
        List<Token> actual = ReaderUtil.readInputString("\"a \\\\ b\"");
        List<Token> expected = List.of(
            Token.of(TokenType.STRING, StringRange.of(0, 8), "\"a \\\\ b\"")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testUnnecessaryEscape() {
        List<Token> actual = ReaderUtil.readInputString("\"\\ \\ \\ \"");
        List<Token> expected = List.of(
            Token.of(TokenType.STRING, StringRange.of(0, 8), "\"\\ \\ \\ \"")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testEscapeInNumber() {
        List<Token> actual = ReaderUtil.readInputString("1\\23");
        List<Token> expected = List.of(
            Token.of(TokenType.SYMBOL, StringRange.of(0, 4), "1\\23")
        );
        assertEquals(expected, actual);
    }
}
