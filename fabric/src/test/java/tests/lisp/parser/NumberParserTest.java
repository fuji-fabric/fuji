package tests.lisp.parser;

import java.util.List;
import mod.fuji.module.initializer.evaluator.parser.exception.ParserSyntaxException;
import mod.fuji.module.initializer.evaluator.parser.structure.StringRange;
import mod.fuji.module.initializer.evaluator.parser.token.Token;
import mod.fuji.module.initializer.evaluator.parser.token.TokenType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class NumberParserTest {

    @Test
    void testSingleNumber() {
        List<Token> actual = ParserUtil.parseInputString("123");
        List<Token> expected = List.of(
            Token.of(TokenType.NUMBER, StringRange.of(0, 3), "123")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testDoubleNumber() {
        assertThrows(ParserSyntaxException.class, () -> ParserUtil.parseInputString("123 456"));
    }

    @Test
    void testSingleNumberInList() {
        List<Token> actual = ParserUtil.parseInputString("(123)");
        List<Token> expected = List.of(
            Token.of(TokenType.OPEN_PARENTHESES, StringRange.of(0, 1), "("),
            Token.of(TokenType.NUMBER, StringRange.of(1, 4), "123"),
            Token.of(TokenType.CLOSED_PARENTHESES, StringRange.of(4, 5), ")")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testSingleSignCharacter() {
        List<Token> actual = ParserUtil.parseInputString("+123");
        List<Token> expected = List.of(
            Token.of(TokenType.NUMBER, StringRange.of(0, 4), "+123")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testDoubleSignCharacter() {
        List<Token> actual = ParserUtil.parseInputString("+123+4");
        List<Token> expected = List.of(
            Token.of(TokenType.SYMBOL, StringRange.of(0, 6), "+123+4")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testOneSignCharacter() {
        List<Token> actual = ParserUtil.parseInputString("+");
        List<Token> expected = List.of(
            Token.of(TokenType.SYMBOL, StringRange.of(0, 1), "+")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testInBetweenSignCharacter() {
        List<Token> actual = ParserUtil.parseInputString("1+2");
        List<Token> expected = List.of(
            Token.of(TokenType.SYMBOL, StringRange.of(0, 3), "+")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testNumberAndSymbolInList() {
        List<Token> actual = ParserUtil.parseInputString("(123 abc 456 def 789)");
        List<Token> expected = List.of(
            Token.of(TokenType.OPEN_PARENTHESES, StringRange.of(0, 1), "("),
            Token.of(TokenType.NUMBER, StringRange.of(1, 4), "123"),
            Token.of(TokenType.SYMBOL, StringRange.of(5, 8), "abc"),
            Token.of(TokenType.NUMBER, StringRange.of(9, 12), "456"),
            Token.of(TokenType.SYMBOL, StringRange.of(13, 16), "def"),
            Token.of(TokenType.NUMBER, StringRange.of(17, 20), "789"),
            Token.of(TokenType.CLOSED_PARENTHESES, StringRange.of(20, 21), ")")
        );
        assertEquals(expected, actual);
    }

//    @Test
//    void testSingleFloating() {
//        List<Token> actual = ParserUtil.parseInputString("123.456");
//        List<Token> expected = List.of(
//            Token.of(TokenType.NUMBER, StringRange.of(0, 3), "123")
//        );
//        assertEquals(expected, actual);
//    }
}
