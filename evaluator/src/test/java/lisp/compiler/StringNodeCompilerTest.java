package lisp.compiler;

import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispObject;
import mod.fuji.evaluator.evaluator.node.LispString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class StringNodeCompilerTest {

    @Test
    void testSingleStringNode() {
        LispObject actual = CompilerUtils.compile("\"abc\"");
        LispObject expected = LispList.of(
            LispString.of("\"abc\"")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testEscapeStringNode() {
        LispObject actual = CompilerUtils.compile("\"a\\bc\"");
        LispObject expected = LispList.of(
            LispString.of("\"a\\bc\"")
        );
        assertEquals(expected, actual);
    }

}
