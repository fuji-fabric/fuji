package lisp.evaluator;

import java.util.List;
import java.util.Optional;
import mod.fuji.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispNumber;
import mod.fuji.evaluator.evaluator.node.LispSymbol;
import mod.fuji.evaluator.evaluator.structure.lambda.LambdaList;
import mod.fuji.evaluator.evaluator.structure.lambda.parameter.OptionalParameterSpecifier;
import mod.fuji.evaluator.evaluator.structure.lambda.parameter.ParameterSpecifier;
import mod.fuji.evaluator.evaluator.structure.lambda.parameter.RequiredParameterSpecifier;
import mod.fuji.evaluator.evaluator.structure.lambda.parameter.RestParameterSpecifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class LambdaListTest {

    @Test
    void testNoArgsLambdaList() {
        List<ParameterSpecifier> actual = LambdaList.of(LispList.of()).getParameterSpecifiers();
        List<ParameterSpecifier> expected = List.of();
        assertEquals(expected, actual);
    }

    @Test
    void testRequiredArgsOnlyParsing() {
        List<ParameterSpecifier> actual = LambdaList.of(LispList.of(
            LispSymbol.of("a"),
            LispSymbol.of("b"),
            LispSymbol.of("c")
        )).getParameterSpecifiers();
        List<ParameterSpecifier> expected = List.of(
            RequiredParameterSpecifier.of("a"),
            RequiredParameterSpecifier.of("b"),
            RequiredParameterSpecifier.of("c")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testSingleOptionalArgParsing() {
        List<ParameterSpecifier> actual = LambdaList.of(LispList.of(
            LispSymbol.of("a"),
            LispSymbol.of("&optional"),
            LispSymbol.of("b")
        )).getParameterSpecifiers();
        List<ParameterSpecifier> expected = List.of(
            RequiredParameterSpecifier.of("a"),
            OptionalParameterSpecifier.of("b", Optional.empty(), Optional.empty())
        );
        assertEquals(expected, actual);
    }

    @Test
    void testDoubleOptionalArgsParsing() {
        List<ParameterSpecifier> actual = LambdaList.of(LispList.of(
            LispSymbol.of("a"),
            LispSymbol.of("b"),
            LispSymbol.of("&optional"),
            LispSymbol.of("c"),
            LispSymbol.of("d")
        )).getParameterSpecifiers();
        List<ParameterSpecifier> expected = List.of(
            RequiredParameterSpecifier.of("a"),
            RequiredParameterSpecifier.of("b"),
            OptionalParameterSpecifier.of("c", Optional.empty(), Optional.empty()),
            OptionalParameterSpecifier.of("d", Optional.empty(), Optional.empty())
        );
        assertEquals(expected, actual);
    }

    @Test
    void testSingleOptionalArgWithInitFormParsing() {
        List<ParameterSpecifier> actual = LambdaList.of(LispList.of(
            LispSymbol.of("&optional"),
            LispList.of(
                LispSymbol.of("a"),
                LispNumber.of(123)
            )
        )).getParameterSpecifiers();
        List<ParameterSpecifier> expected = List.of(
            OptionalParameterSpecifier.of("a", Optional.of(LispList.of(LispNumber.of(123))), Optional.empty())
        );
        assertEquals(expected, actual);
    }

    @Test
    void testSingleOptionalArgWithSuppliedVarParsing() {
        List<ParameterSpecifier> actual = LambdaList.of(LispList.of(
            LispSymbol.of("&optional"),
            LispList.of(
                LispSymbol.of("a"),
                LispNumber.of(123),
                LispSymbol.of("b")
            )
        )).getParameterSpecifiers();
        List<ParameterSpecifier> expected = List.of(
            OptionalParameterSpecifier.of("a", Optional.of(LispList.of(LispNumber.of(123))), Optional.of(LispSymbol.of("b")))
        );
        assertEquals(expected, actual);
    }

    @Test
    void testSingleRestParsing() {
        List<ParameterSpecifier> actual = LambdaList.of(LispList.of(
            LispSymbol.of("&rest"),
            LispSymbol.of("a")
        )).getParameterSpecifiers();
        List<ParameterSpecifier> expected = List.of(
            RestParameterSpecifier.of("a")
        );
        assertEquals(expected, actual);
    }

    @Test
    void testSpecifyTooFewVariableForRestArgParsing() {
        assertThrows(LispEvaluationException.class, () -> {
            LambdaList.of(LispList.of(
                LispSymbol.of("&rest")
            ));
        });
    }

    @Test
    void testSpecifyTooManyVariableForRestArgParsing() {
        assertThrows(LispEvaluationException.class, () -> {
            LambdaList.of(LispList.of(
                LispSymbol.of("&rest"),
                LispSymbol.of("a"),
                LispSymbol.of("b")
            ));
        });
    }

    @Test
    void testMissingVariableForRestArgParsing() {
        assertThrows(LispEvaluationException.class, () -> {
            LambdaList.of(LispList.of(
                LispSymbol.of("&rest"),
                LispSymbol.of("&key")
            ));
        });
    }

    @Test
    void testMisplacedKeywordAfterRestArgParsing() {
        assertThrows(LispEvaluationException.class, () -> {
            LambdaList.of(LispList.of(
                LispSymbol.of("&rest"),
                LispSymbol.of("a"),
                LispSymbol.of("&optional"),
                LispSymbol.of("b")
            ));
        });
    }
}
