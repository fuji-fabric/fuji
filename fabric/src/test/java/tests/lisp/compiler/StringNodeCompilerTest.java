package tests.lisp.compiler;

import mod.fuji.module.initializer.evaluator.evaluator.node.LispListNode;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNode;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispStringNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class StringNodeCompilerTest {

    @Test
    void testSingleStringNode() {
        LispNode actual = CompilerUtils.compile("\"abc\"");
        LispNode expected = LispListNode.of(
            LispStringNode.of("\"abc\"")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testEscapeStringNode() {
        LispNode actual = CompilerUtils.compile("\"a\\bc\"");
        LispNode expected = LispListNode.of(
            LispStringNode.of("\"a\\bc\"")
        );
        assertEquals(expected, actual);
    }

}
