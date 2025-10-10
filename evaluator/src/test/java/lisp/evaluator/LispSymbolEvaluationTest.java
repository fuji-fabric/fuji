package lisp.evaluator;

import mod.fuji.evaluator.evaluator.value.LispObject;
import mod.fuji.evaluator.evaluator.value.LispString;
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
