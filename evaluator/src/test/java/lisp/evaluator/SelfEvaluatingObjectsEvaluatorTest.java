package lisp.evaluator;

import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.node.LispNumber;
import mod.fuji.evaluator.evaluator.node.LispObject;
import mod.fuji.evaluator.evaluator.node.LispString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SelfEvaluatingObjectsEvaluatorTest {

    @Test
    void evalSingleNumber() {
        LispObject actual = EvaluatorUtils.evaluate("123.456");
        LispObject expect = LispNumber.of(123.456);
        Assertions.assertEquals(expect, actual);
    }

    @Test
    void evalSingleString() {
        LispObject actual = EvaluatorUtils.evaluate("\"123\"");
        LispObject expect = LispString.of("\"123\"");
        Assertions.assertEquals(expect, actual);
    }

    @Test
    void evalSingleNil() {
        LispObject actual = EvaluatorUtils.evaluate("nil");
        LispObject expect = LispEnvironment.NIL;
        Assertions.assertEquals(expect, actual);
    }
}
