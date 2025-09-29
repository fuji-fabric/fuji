package tests.lisp.compiler;

import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispSymbol;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class SymbolNodeCompilerTest {

    @Test
    void testSingleSymbolNode() {
        LispObject actual = CompilerUtils.compile("abc");
        LispObject expected = LispList.of(
            LispSymbol.of("abc")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testDoubleSymbolNodes() {
        LispObject actual = CompilerUtils.compile("(abc def)");
        LispObject expected = LispList.of(
            LispList.of(
                LispSymbol.of("abc"),
                LispSymbol.of("def")
            )
        );
        assertEquals(expected, actual);
    }

    @Test
    void testEscapeSymbolNode() {
        LispObject actual = CompilerUtils.compile("a\\bc");
        LispObject expected = LispList.of(
            LispSymbol.of("a\\bc")
        );
        assertEquals(expected, actual);
    }
}
