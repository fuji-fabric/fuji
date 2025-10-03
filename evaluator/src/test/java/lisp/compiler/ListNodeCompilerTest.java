package lisp.compiler;

import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ListNodeCompilerTest {

    @Test
    void testSingleListNode() {
        LispObject actual = CompilerUtils.compile("()");
        LispObject expected = LispList.of(
            LispList.of()
        );
        assertEquals(expected, actual);
    }

    @Test
    void testNestedListNodes() {
        LispObject actual = CompilerUtils.compile("((()))");
        LispObject expected = LispList.of(
            LispList.of(
                LispList.of(
                    LispList.of(

                    )
                )
            )
        );
        assertEquals(expected, actual);
    }

    @Test
    void testListNodeBranches() {
        LispObject actual = CompilerUtils.compile("(() () ())");
        LispObject expected = LispList.of(
            LispList.of(
                LispList.of(),
                LispList.of(),
                LispList.of()
            )
        );
        assertEquals(expected, actual);
    }
}
