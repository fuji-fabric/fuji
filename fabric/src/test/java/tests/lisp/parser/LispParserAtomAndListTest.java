package tests.lisp.parser;

import java.util.List;
import mod.fuji.module.initializer.evaluator.formatter.PrettyFormatter;
import mod.fuji.module.initializer.evaluator.parser.LispParser;
import mod.fuji.module.initializer.evaluator.parser.exception.ParserSyntaxException;
import mod.fuji.module.initializer.evaluator.parser.structure.StringRange;
import mod.fuji.module.initializer.evaluator.parser.token.Token;
import mod.fuji.module.initializer.evaluator.parser.token.TokenType;
import org.jetbrains.annotations.NotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class LispParserAtomAndListTest {

    private static @NotNull List<Token> parseInputString(@NotNull String input) {
        LispParser lispParser = new LispParser(input);
        List<Token> parse = lispParser.parse();
        PrettyFormatter.prettyPrint(parse);
        return parse;
    }

    @Test
    void testSingleList() {
        List<Token> actual = parseInputString("()");
        List<Token> expected = List.of(
            Token.of(TokenType.OPEN_PARENTHESES, StringRange.of(0, 1), "("),
            Token.of(TokenType.CLOSED_PARENTHESES, StringRange.of(1, 2), ")")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testUnclosedList() {
        assertThrows(ParserSyntaxException.class, () -> {
            parseInputString("(");
        });
    }

    @Test
    void testUnexpectedClosedParenthesis() {
        assertThrows(ParserSyntaxException.class, () -> {
            parseInputString(")");
        });
    }

    @Test
    void testSingleAtomInList() {
        List<Token> actual = parseInputString("(123)");
        List<Token> expected = List.of(
            Token.of(TokenType.OPEN_PARENTHESES, StringRange.of(0, 1), "("),
            Token.of(TokenType.IDENTIFIER, StringRange.of(1, 4), "123"),
            Token.of(TokenType.CLOSED_PARENTHESES, StringRange.of(4, 5), ")")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testSingleIdentifier() {
        List<Token> actual = parseInputString("123");
        List<Token> expected = List.of(
            Token.of(TokenType.IDENTIFIER, StringRange.of(0, 3), "123")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testDoubleAtom() {
        assertThrows(ParserSyntaxException.class, () -> {
            parseInputString("abc def");
        });
    }

    @Test
    void testDoubleAtomInList() {
        List<Token> actual = parseInputString("(abc def)");
        List<Token> expected = List.of(
            Token.of(TokenType.OPEN_PARENTHESES, StringRange.of(0, 1), "("),
            Token.of(TokenType.IDENTIFIER, StringRange.of(1, 4), "abc"),
            Token.of(TokenType.IDENTIFIER, StringRange.of(5, 8), "def"),
            Token.of(TokenType.CLOSED_PARENTHESES, StringRange.of(8, 9), ")")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testTripleAtomInList() {
        List<Token> actual = parseInputString("(abc def ghi)");
        List<Token> expected = List.of(
            Token.of(TokenType.OPEN_PARENTHESES, StringRange.of(0, 1), "("),
            Token.of(TokenType.IDENTIFIER, StringRange.of(1, 4), "abc"),
            Token.of(TokenType.IDENTIFIER, StringRange.of(5, 8), "def"),
            Token.of(TokenType.IDENTIFIER, StringRange.of(9, 12), "ghi"),
            Token.of(TokenType.CLOSED_PARENTHESES, StringRange.of(12, 13), ")")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testSingleShortIdentifier() {
        List<Token> actual = parseInputString("a");
        List<Token> expected = List.of(
            Token.of(TokenType.IDENTIFIER, StringRange.of(0, 1), "a")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testShortIdentifiersInList() {
        List<Token> actual = parseInputString("(a b c d e)");
        List<Token> expected = List.of(
            Token.of(TokenType.OPEN_PARENTHESES, StringRange.of(0, 1), "("),
            Token.of(TokenType.IDENTIFIER, StringRange.of(1, 2), "a"),
            Token.of(TokenType.IDENTIFIER, StringRange.of(3, 4), "b"),
            Token.of(TokenType.IDENTIFIER, StringRange.of(5, 6), "c"),
            Token.of(TokenType.IDENTIFIER, StringRange.of(7, 8), "d"),
            Token.of(TokenType.IDENTIFIER, StringRange.of(9, 10), "e"),
            Token.of(TokenType.CLOSED_PARENTHESES, StringRange.of(10, 11), ")")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testAtomAndList() {
        List<Token> actual = parseInputString("(define (square x) (* x x))");
        List<Token> expected = List.of(
            Token.of(TokenType.OPEN_PARENTHESES, StringRange.of(0, 1), "("),
            Token.of(TokenType.IDENTIFIER, StringRange.of(1, 7), "define"),
            Token.of(TokenType.OPEN_PARENTHESES, StringRange.of(8, 9), "("),
            Token.of(TokenType.IDENTIFIER, StringRange.of(9, 15), "square"),
            Token.of(TokenType.IDENTIFIER, StringRange.of(16, 17), "x"),
            Token.of(TokenType.CLOSED_PARENTHESES, StringRange.of(17, 18), ")"),
            Token.of(TokenType.OPEN_PARENTHESES, StringRange.of(19, 20), "("),
            Token.of(TokenType.IDENTIFIER, StringRange.of(20, 21), "*"),
            Token.of(TokenType.IDENTIFIER, StringRange.of(22, 23), "x"),
            Token.of(TokenType.IDENTIFIER, StringRange.of(24, 25), "x"),
            Token.of(TokenType.CLOSED_PARENTHESES, StringRange.of(25, 26), ")"),
            Token.of(TokenType.CLOSED_PARENTHESES, StringRange.of(26, 27), ")")
        );
        assertEquals(expected, actual);
    }

}
