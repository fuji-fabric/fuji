package tests.lisp.compiler;

import mod.fuji.module.initializer.evaluator.evaluator.node.LispListNode;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ListNodeCompilerTest {

    @Test
    void testSingleListNode() {
        LispNode actual = CompilerUtils.compile("()");
        LispNode expected = LispListNode.of(
            LispListNode.of()
        );
        assertEquals(expected, actual);
    }

    @Test
    void testNestedListNodes() {
        LispNode actual = CompilerUtils.compile("((()))");
        LispNode expected = LispListNode.of(
            LispListNode.of(
                LispListNode.of(
                    LispListNode.of(

                    )
                )
            )
        );
        assertEquals(expected, actual);
    }

}
