package mod.fuji.evaluator.evaluator.structure.lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mod.fuji.evaluator.auxiliary.CollectionUtil;
import mod.fuji.evaluator.evaluator.auxliary.LispFunctions;
import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispObject;
import mod.fuji.evaluator.evaluator.node.LispSymbol;
import mod.fuji.evaluator.evaluator.structure.lambda.parameter.OptionalParameterSpecifier;
import mod.fuji.evaluator.evaluator.structure.lambda.parameter.ParameterSpecifier;
import mod.fuji.evaluator.evaluator.structure.lambda.parameter.RequiredParameterSpecifier;
import mod.fuji.evaluator.reader.LispStreamProcessor;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class LambdaListParser extends LispStreamProcessor<LispObject, LispList, ParameterSpecifier> {

    @NotNull LispList lambdaList;
    @NotNull List<ParameterSpecifier> builder = new ArrayList<>();

    @NotNull List<LispSymbol> seenLambdaListKeyword = new ArrayList<>(List.of(
        LambdaListKeywords.REQUIRED_ARGUMENT_KEYWORD
    ));

    public LambdaListParser(@NotNull LispList lambdaList) {
        this.lambdaList = lambdaList;
    }

    public void parseLambdaList() {
        parseRequiredArguments();
        parseOptionalArguments();
    }

    /**
     * Eat all symbols until a lambda list keyword is seen.
     */
    private void parseRequiredArguments() {
        LispObject peek;
        while ((peek = peek()) != LispEnvironment.NIL) {
            if (peek instanceof LispSymbol lispSymbol) {
                if (LambdaListKeywords.isLambdaListKeyword(lispSymbol)) {
                    validateLambdaListKeywordTransition(lispSymbol);
                    return;
                }
                forward();

                ParameterSpecifier parameterSpecifier = RequiredParameterSpecifier.of(lispSymbol.getName());
                emit(parameterSpecifier);
            } else {
                throw new LispEvaluationException("Required argument is not a symbol: %s".formatted(peek));
            }
        }
    }

    private void parseOptionalArguments() {
        /* Initialize optional args parser. */
        LispObject peek = peek();
        if (!peek.equals(LambdaListKeywords.OPTIONAL_ARGUMENT_KEYWORD)) {
            return;
        }
        forward();
        seenLambdaListKeyword.add(LambdaListKeywords.OPTIONAL_ARGUMENT_KEYWORD);

        /* Consume optional args. */
        while ((peek = peek()) != LispEnvironment.NIL) {
            if (peek instanceof LispSymbol lispSymbol) {
                if (LambdaListKeywords.isLambdaListKeyword(lispSymbol)) {
                    validateLambdaListKeywordTransition(lispSymbol);
                    break;
                }
                forward();

                ParameterSpecifier parameterSpecifier = RequiredParameterSpecifier.of(lispSymbol.getName());
                emit(parameterSpecifier);
            } else if (peek instanceof @SuppressWarnings("unused") LispList lispList) {
                forward();

                LispObject parameterNameSymbol = LispFunctions.nth(lispList, 0);
                if (!(parameterNameSymbol instanceof LispSymbol $parameterNameSymbol)) {
                    throw new LispEvaluationException("Optional argument is not a symbol or cons: %s".formatted(parameterNameSymbol));
                }
                String parameterName = $parameterNameSymbol.getName();

                // FIXME: use destructing-bind special form.
                if (lispList.size() == 2) {
                    LispList initForm = LispList.of(LispFunctions.nth(lispList, 1));
                    ParameterSpecifier parameterSpecifier = OptionalParameterSpecifier.of(parameterName, Optional.of(initForm), Optional.empty());
                    emit(parameterSpecifier);
                } else if (lispList.size() == 3) {
                    LispList initForm = LispList.of(LispFunctions.nth(lispList, 1));
                    LispSymbol suppliedVar = LispFunctions.checkType(LispFunctions.nth(lispList, 2), LispSymbol.class);
                    ParameterSpecifier parameterSpecifier = OptionalParameterSpecifier.of(parameterName, Optional.of(initForm), Optional.of(suppliedVar));
                    emit(parameterSpecifier);
                } else {
                    throw new LispEvaluationException("Specify too many elements in optional arg list.");
                }

            } else {
                throw new LispEvaluationException("Optional argument is not a symbol or cons: %s".formatted(peek));
            }
        }
    }


    private @NotNull LispSymbol getMostRecentlySeenLambdaListKeyword() {
        return CollectionUtil.getLast(seenLambdaListKeyword);
    }

    private void validateLambdaListKeywordTransition(@NotNull LispSymbol to) {
        @NotNull LispSymbol from = getMostRecentlySeenLambdaListKeyword();
        List<LispSymbol> nextLambdaListKeywords = LambdaListKeywords.getNextLambdaListKeywords(from);
        if (!nextLambdaListKeywords.contains(to)) {
            throw new LispEvaluationException("Misplaced %s in lambda list: %s".formatted(to, lambdaList));
        }
    }

    @Override
    protected int streamLength() {
        return lambdaList.size();
    }

    @Override
    protected @NotNull LispObject peek() {
        if (this.end >= this.lambdaList.size()) {
            return LispEnvironment.NIL;
        }

        return lambdaList.get(this.end);
    }

    @Override
    protected @NotNull LispObject previous() {
        return lambdaList.get(this.end - 1);
    }

    @Override
    protected void emit(@NotNull ParameterSpecifier parameterSpecifier) {
        this.builder.add(parameterSpecifier);
    }

}

