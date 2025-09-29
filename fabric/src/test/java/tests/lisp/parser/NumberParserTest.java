package tests.lisp.parser;

import java.util.List;
import mod.fuji.module.initializer.evaluator.reader.exception.LispSyntaxException;
import mod.fuji.module.initializer.evaluator.reader.structure.StringRange;
import mod.fuji.module.initializer.evaluator.reader.token.Token;
import mod.fuji.module.initializer.evaluator.reader.token.TokenType;
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
        assertThrows(LispSyntaxException.class, () -> ParserUtil.parseInputString("123 456"));
    }

    @Test
    void testSingleNumberInList() {
        List<Token> actual = ParserUtil.parseInputString("(123)");
        List<Token> expected = List.of(
            Token.of(TokenType.BEGIN_LIST, StringRange.of(0, 1), "("),
            Token.of(TokenType.NUMBER, StringRange.of(1, 4), "123"),
            Token.of(TokenType.END_LIST, StringRange.of(4, 5), ")")
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
            Token.of(TokenType.SYMBOL, StringRange.of(0, 3), "1+2")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testNumberAndSymbolInList() {
        List<Token> actual = ParserUtil.parseInputString("(123 abc 456 def 789)");
        List<Token> expected = List.of(
            Token.of(TokenType.BEGIN_LIST, StringRange.of(0, 1), "("),
            Token.of(TokenType.NUMBER, StringRange.of(1, 4), "123"),
            Token.of(TokenType.SYMBOL, StringRange.of(5, 8), "abc"),
            Token.of(TokenType.NUMBER, StringRange.of(9, 12), "456"),
            Token.of(TokenType.SYMBOL, StringRange.of(13, 16), "def"),
            Token.of(TokenType.NUMBER, StringRange.of(17, 20), "789"),
            Token.of(TokenType.END_LIST, StringRange.of(20, 21), ")")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testSingleDecimalPointCharacter() {
        List<Token> actual = ParserUtil.parseInputString("123.456");
        List<Token> expected = List.of(
            Token.of(TokenType.NUMBER, StringRange.of(0, 7), "123.456")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testDoubleDecimalPointCharacter() {
        List<Token> actual = ParserUtil.parseInputString("123.456.");
        List<Token> expected = List.of(
            Token.of(TokenType.SYMBOL, StringRange.of(0, 8), "123.456.")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testLeadingDecimalPointCharacter() {
        List<Token> actual = ParserUtil.parseInputString(".123");
        List<Token> expected = List.of(
            Token.of(TokenType.NUMBER, StringRange.of(0, 4), ".123")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testLeadingDecimalPointCharacterSymbol() {
        List<Token> actual = ParserUtil.parseInputString(".123.");
        List<Token> expected = List.of(
            Token.of(TokenType.SYMBOL, StringRange.of(0, 5), ".123.")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testNumberFollowingDecimalPoint() {
        List<Token> actual = ParserUtil.parseInputString("123.");
        List<Token> expected = List.of(
            Token.of(TokenType.NUMBER, StringRange.of(0, 4), "123.")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testSymbolNamePrecedingWithNumber() {
        List<Token> actual = ParserUtil.parseInputString("123.a");
        List<Token> expected = List.of(
            Token.of(TokenType.SYMBOL, StringRange.of(0, 5), "123.a")
        );
        assertEquals(expected, actual);
    }
}
