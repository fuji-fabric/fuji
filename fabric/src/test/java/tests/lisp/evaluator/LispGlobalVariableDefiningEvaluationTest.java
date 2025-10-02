package tests.lisp.evaluator;

import mod.fuji.module.initializer.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispNumber;
import mod.fuji.module.initializer.evaluator.evaluator.node.LispObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class LispGlobalVariableDefiningEvaluationTest {

    @Test
    void testSingleDefineVariableForm() {
        LispObject actual = EvaluatorUtils.evaluate("""
            (progn (defvar a 123)
                   a)
            """);
        LispObject expected = LispNumber.of(123);
        assertEquals(expected, actual);
    }

    @Test
    void testDoubleDefineVariableForm() {
        LispObject actual = EvaluatorUtils.evaluate("""
            (progn (defvar a 123)
                   (defvar a 456)
                   a)
            """);
        LispObject expected = LispNumber.of(123);
        assertEquals(expected, actual);
    }

    @Test
    void testDefvarAndDefparameterForms() {
        LispObject actual = EvaluatorUtils.evaluate("""
            (progn (defvar a 123)
                   (defvar a 456)
                   (defparameter a 789)
                   (defvar a 0)
                   a)
            """);
        LispObject expected = LispNumber.of(789);
        assertEquals(expected, actual);
    }

    @Test
    void testSingleDefconstantForm() {
        LispObject actual = EvaluatorUtils.evaluate("""
            (progn (defconstant a 123)
                   a)
            """);
        LispObject expected = LispNumber.of(123);
        assertEquals(expected, actual);
    }

    @Test
    void testRedefineConstant() {
        assertThrows(LispEvaluationException.class, () -> {
            EvaluatorUtils.evaluate("""
            (progn (defconstant a 123)
                   (defconstant a 456)
                   a)
            """);
        });
    }

    @Test
    void testProclaimConstant() {
        assertThrows(LispEvaluationException.class, () -> {
            EvaluatorUtils.evaluate("""
            (progn (defconstant a 123)
                   (defparameter a 456)
                   a)
            """);
        });
    }

    @Test
    void testConstantOverride() {
        LispObject actual = EvaluatorUtils.evaluate("""
            (progn (defparameter a 123)
                   (defconstant a 456)
                   a)
            """);
        LispObject expected = LispNumber.of(456);
        assertEquals(expected, actual);
    }

}
