package lisp.evaluator;

import mod.fuji.evaluator.evaluator.node.LispNumber;
import mod.fuji.evaluator.evaluator.node.LispObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class LispBlockEvaluationTest {

    @Test
    @Disabled
    void testSingleBlock() {
        LispObject actual = EvaluatorUtils.evaluate("""
            (block)
            """);
        LispObject expected = LispNumber.of(123);
        assertEquals(expected, actual);
    }
}
