package tests.lisp.evaluator;

import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class LispSymbolEvaluationTest {

    @Test
    void testDereferenceSymbolVariableValue() {
        LispObject actual = EvaluatorUtils.evaluate("*test-version*");
        LispObject expected = LispString.of("1.0.0");
        assertEquals(expected, actual);
    }
}
