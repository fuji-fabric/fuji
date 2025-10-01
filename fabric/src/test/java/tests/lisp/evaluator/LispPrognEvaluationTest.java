package tests.lisp.evaluator;

import mod.fuji.module.initializer.evaluator.evaluator.node.LispNumber;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class LispPrognEvaluationTest {

    @Test
    void testSinglePrognForm() {
        LispObject actual = EvaluatorUtils.evaluate("""
            (progn 1 2 3)
            """);
        LispObject expected = LispNumber.of(3);
        assertEquals(expected, actual);
    }

}
