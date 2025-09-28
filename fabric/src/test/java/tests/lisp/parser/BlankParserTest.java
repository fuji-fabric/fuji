package tests.lisp.parser;

import java.util.List;
import mod.fuji.module.initializer.evaluator.Reader.structure.StringRange;
import mod.fuji.module.initializer.evaluator.Reader.token.Token;
import mod.fuji.module.initializer.evaluator.Reader.token.TokenType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class BlankParserTest {

    @Test
    void testCompatSymbolAndString() {
        List<Token> actual = ParserUtil.parseInputString("(a\"b\")");
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
        List<Token> actual = ParserUtil.parseInputString("(123\"456\")");
        List<Token> expected = List.of(
            Token.of(TokenType.BEGIN_LIST, StringRange.of(0, 1), "("),
            Token.of(TokenType.NUMBER, StringRange.of(1, 4), "123"),
            Token.of(TokenType.STRING, StringRange.of(4, 9), "\"456\""),
            Token.of(TokenType.END_LIST, StringRange.of(9, 10), ")")
        );
        assertEquals(expected, actual);
    }

}
