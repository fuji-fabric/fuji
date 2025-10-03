package lisp.reader;

import java.util.List;
import mod.fuji.evaluator.reader.structure.StringRange;
import mod.fuji.evaluator.reader.token.Token;
import mod.fuji.evaluator.reader.token.TokenType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class BoolReaderTest {

    /**
 *         The <code>boolean value</code> is a <code>symbol</code>.
        The <code>nil</code> symbol is treated as <code>false value</code>.
        Any data object other than <code>nil</code> symbol is treated as <code>true value</code>.
        All the assumptions are made by <code>boolean test functions</code>.
        The <code>boolean test functions</code> describe the <code>truth value</code>.

 **/
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
