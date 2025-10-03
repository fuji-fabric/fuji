package tests.lisp.evaluator;

import mod.fuji.module.initializer.evaluator.evaluator.compiler.exception.LispCompilationException;
import mod.fuji.module.initializer.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNumber;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class ListFunctionEvaluatorTest {

    @Test
    void testEmptyListEvaluation() {
        LispObject actual = EvaluatorUtils.evaluate("()");
        LispObject expected = LispEnvironment.NIL;
        assertEquals(expected, actual);
    }

    @Test
    void testNumberFunctionName() {
        assertThrows(LispCompilationException.class, () -> {
            EvaluatorUtils.evaluate("(1 b c)");
        });
    }

    @Test
    void testStringFunctionName() {
        assertThrows(LispCompilationException.class, () -> {
            EvaluatorUtils.evaluate("(\"a\" b c)");
        });
    }

    @Test
    void testFunctionArgsEvaluationOrder() {
        assertThrows(LispEvaluationException.class, () -> {
            EvaluatorUtils.evaluate("(a b c)");
        });
    }

    @Test
    void testAdderFunctionCall() {
        LispObject actual = EvaluatorUtils.evaluate("(+ 1 2 3)");
        LispObject expected = LispNumber.of(6);
        assertEquals(expected, actual);
    }

    @Test
    void testNestedAdderFunctionCall() {
        LispObject actual = EvaluatorUtils.evaluate("(+ 1 2 3 (+ 4 5))");
        LispObject expected = LispNumber.of(15);
        assertEquals(expected, actual);
    }

    @Test
    void testCombinedFunctionCall() {
        LispObject actual = EvaluatorUtils.evaluate("(+ 3 (* 4 5))");
        LispObject expected = LispNumber.of(23);
        assertEquals(expected, actual);
    }

    @Test
    void testIncompatibleArgumentTypeForAdderFunction() {
        assertThrows(LispEvaluationException.class, () -> {
            EvaluatorUtils.evaluate("(+ 1 2 \"3\")");
        });
    }

}
