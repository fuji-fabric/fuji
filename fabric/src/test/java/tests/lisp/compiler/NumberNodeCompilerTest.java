package tests.lisp.compiler;

import mod.fuji.module.initializer.evaluator.evaluator.node.LispListNode;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNode;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNumberNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class NumberNodeCompilerTest {

    @Test
    void testSingleNumberNode() {
        LispNode actual = CompilerUtils.compile("123");
        LispNode expected = LispListNode.of(
            LispNumberNode.of(123)
        );
        assertEquals(expected, actual);
    }

    @Test
    void testSingleNumberNodeInList() {
        LispNode actual = CompilerUtils.compile("(123)");
        LispNode expected = LispListNode.of(
            LispListNode.of(
                LispNumberNode.of(123)
            )
        );
        assertEquals(expected, actual);
    }

    @Test
    void testDoubleNumberNodesInList() {
        LispNode actual = CompilerUtils.compile("(123 456)");
        LispNode expected = LispListNode.of(
            LispListNode.of(
                LispNumberNode.of(123),
                LispNumberNode.of(456)
            )
        );
        assertEquals(expected, actual);
    }
}
