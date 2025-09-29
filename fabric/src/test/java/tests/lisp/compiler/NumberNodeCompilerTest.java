package tests.lisp.compiler;

import mod.fuji.module.initializer.evaluator.evaluator.node.LispList;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNumber;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class NumberNodeCompilerTest {

    @Test
    void testSingleNumberNode() {
        LispObject actual = CompilerUtils.compile("123");
        LispObject expected = LispList.of(
            LispNumber.of(123)
        );
        assertEquals(expected, actual);
    }

    @Test
    void testSingleNumberNodeInList() {
        LispObject actual = CompilerUtils.compile("(123)");
        LispObject expected = LispList.of(
            LispList.of(
                LispNumber.of(123)
            )
        );
        assertEquals(expected, actual);
    }

    @Test
    void testDoubleNumberNodesInList() {
        LispObject actual = CompilerUtils.compile("(123 456)");
        LispObject expected = LispList.of(
            LispList.of(
                LispNumber.of(123),
                LispNumber.of(456)
            )
        );
        assertEquals(expected, actual);
    }
}
