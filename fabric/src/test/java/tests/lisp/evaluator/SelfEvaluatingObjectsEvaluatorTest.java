package tests.lisp.evaluator;

import mod.fuji.module.initializer.evaluator.evaluator.context.Environment;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNumber;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispString;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispSymbol;
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
        LispObject expect = Environment.NIL;
        Assertions.assertEquals(expect, actual);
    }
}
