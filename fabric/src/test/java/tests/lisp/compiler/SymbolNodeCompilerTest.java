package tests.lisp.compiler;

import mod.fuji.module.initializer.evaluator.evaluator.node.LispListNode;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNode;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispStringNode;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispSymbolNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class SymbolNodeCompilerTest {

    @Test
    void testSingleSymbolNode() {
        LispNode actual = CompilerUtils.compile("abc");
        LispNode expected = LispListNode.of(
            LispSymbolNode.of("abc")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testDoubleSymbolNodes() {
        LispNode actual = CompilerUtils.compile("(abc def)");
        LispNode expected = LispListNode.of(
            LispListNode.of(
                LispSymbolNode.of("abc"),
                LispSymbolNode.of("def")
            )
        );
        assertEquals(expected, actual);
    }

    @Test
    void testEscapeSymbolNode() {
        LispNode actual = CompilerUtils.compile("a\\bc");
        LispNode expected = LispListNode.of(
            LispSymbolNode.of("a\\bc")
        );
        assertEquals(expected, actual);
    }
}
