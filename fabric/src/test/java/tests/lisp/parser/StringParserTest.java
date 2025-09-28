package tests.lisp.parser;

import java.util.List;
import mod.fuji.module.initializer.evaluator.parser.exception.ParserSyntaxException;
import mod.fuji.module.initializer.evaluator.parser.structure.StringRange;
import mod.fuji.module.initializer.evaluator.parser.token.Token;
import mod.fuji.module.initializer.evaluator.parser.token.TokenType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class StringParserTest {

    @Test
    void testSimpleString() {
        List<Token> actual = ParserUtil.parseInputString("\"abc\"");
        List<Token> expected = List.of(
            Token.of(TokenType.STRING, StringRange.of(0, 5), "\"abc\"")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testEmptyString() {
        List<Token> actual = ParserUtil.parseInputString("\"\"");
        List<Token> expected = List.of(
            Token.of(TokenType.STRING, StringRange.of(0, 2), "\"\"")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testDoubleQuoteCharacterInSymbolName() {
        assertThrows(ParserSyntaxException.class, () -> {
            ParserUtil.parseInputString("abc\"");
        });
    }

    @Test
    void testUnclosedString() {
        assertThrows(ParserSyntaxException.class, () -> {
            ParserUtil.parseInputString("\"abc");
        });
    }

    @Test
    void testStringContainsBlankCharacters() {
        List<Token> actual = ParserUtil.parseInputString("\"abc   def\"");
        List<Token> expected = List.of(
            Token.of(TokenType.STRING, StringRange.of(0, 11), "\"abc   def\"")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testDoubleQuoteEscape() {
        List<Token> actual = ParserUtil.parseInputString("\"a \\\"b c\"");
        List<Token> expected = List.of(
            Token.of(TokenType.STRING, StringRange.of(0, 9), "\"a \\\"b c\"")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testBackSlashEscape() {
        List<Token> actual = ParserUtil.parseInputString("\"a \\\\ b\"");
        List<Token> expected = List.of(
            Token.of(TokenType.STRING, StringRange.of(0, 8), "\"a \\\\ b\"")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testUnnecessaryEscape() {
        List<Token> actual = ParserUtil.parseInputString("\"\\ \\ \\ \"");
        List<Token> expected = List.of(
            Token.of(TokenType.STRING, StringRange.of(0, 8), "\"\\ \\ \\ \"")
        );
        assertEquals(expected, actual);
    }

}
