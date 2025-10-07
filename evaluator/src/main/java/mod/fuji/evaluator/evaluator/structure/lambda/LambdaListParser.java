package mod.fuji.evaluator.evaluator.structure.lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import mod.fuji.evaluator.auxiliary.CollectionUtil;
import mod.fuji.evaluator.evaluator.auxliary.LispFunctions;
import mod.fuji.evaluator.evaluator.context.LispEnvironment;
import mod.fuji.evaluator.evaluator.exception.LispEvaluationException;
import mod.fuji.evaluator.evaluator.node.LispList;
import mod.fuji.evaluator.evaluator.node.LispObject;
import mod.fuji.evaluator.evaluator.node.LispSymbol;
import mod.fuji.evaluator.evaluator.structure.lambda.parameter.KeyParameterSpecifier;
import mod.fuji.evaluator.evaluator.structure.lambda.parameter.OptionalParameterSpecifier;
import mod.fuji.evaluator.evaluator.structure.lambda.parameter.ParameterSpecifier;
import mod.fuji.evaluator.evaluator.structure.lambda.parameter.RequiredParameterSpecifier;
import mod.fuji.evaluator.evaluator.structure.lambda.parameter.RestParameterSpecifier;
import mod.fuji.evaluator.reader.LispStreamProcessor;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
public class LambdaListParser extends LispStreamProcessor<LispObject, LispList, ParameterSpecifier> {

    @NotNull LispList lambdaList;
    @NotNull List<ParameterSpecifier> builder = new ArrayList<>();

    @NotNull List<LispSymbol> seenLambdaListKeyword = new ArrayList<>(List.of(
        LambdaListKeywords.REQUIRED_KEYWORD
    ));

    private LambdaListParser(@NotNull LispList lambdaList) {
        this.lambdaList = lambdaList;
    }

    public static @NotNull List<ParameterSpecifier> parse(@NotNull LispList lambdaList) {
        LambdaListParser lambdaListParser = new LambdaListParser(
            lambdaList
        );
        lambdaListParser.parseLambdaList();
        return lambdaListParser.builder;
    }

    private void parseLambdaList() {
        parseRequiredArguments();
        parseOptionalArguments();
        parseRestArguments();
        parseKeyArguments();
    }

    private void parseKeyArguments() {
        LispObject peek = peek();
        if (!peek.equals(LambdaListKeywords.KEY_KEYWORD)) {
            return;
        }
        forward();
        seenLambdaListKeyword.add(LambdaListKeywords.KEY_KEYWORD);

        while ((peek = peek()) != LispEnvironment.NIL) {
            if (peek instanceof LispSymbol lispSymbol) {
                if (LambdaListKeywords.isLambdaListKeyword(lispSymbol)) {
                    validateLambdaListKeywordTransition(lispSymbol);
                    break;
                }
                forward();

                ParameterSpecifier parameterSpecifier = KeyParameterSpecifier.of(lispSymbol.getName(), Optional.empty(), Optional.empty());
                emit(parameterSpecifier);
            } else if (peek instanceof LispList lispList) {
                forward();

                LispObject parameterNameSymbol = LispFunctions.nth(lispList, 0);
                if (!(parameterNameSymbol instanceof LispSymbol $parameterNameSymbol)) {
                    throw new LispEvaluationException("Key argument is not a symbol or cons: %s".formatted(parameterNameSymbol));
                }
                String parameterName = $parameterNameSymbol.getName();

                if (lispList.size() == 2) {
                    LispList initForm = LispList.of(LispFunctions.nth(lispList, 1));
                    ParameterSpecifier parameterSpecifier = KeyParameterSpecifier.of(parameterName, Optional.of(initForm), Optional.empty());
                    emit(parameterSpecifier);
                } else if (lispList.size() == 3) {
                    LispList initForm = LispList.of(LispFunctions.nth(lispList, 1));
                    LispSymbol suppliedVar = LispFunctions.checkType(LispFunctions.nth(lispList, 2), LispSymbol.class);
                    ParameterSpecifier parameterSpecifier = KeyParameterSpecifier.of(parameterName, Optional.of(initForm), Optional.of(suppliedVar));
                    emit(parameterSpecifier);
                } else {
                    throw new LispEvaluationException("Specify too many elements in key arg list.");
                }

            } else {
                throw new LispEvaluationException("Key argument is not a symbol or cons: %s".formatted(peek));
            }
        }

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

                RequiredParameterSpecifier parameterSpecifier = RequiredParameterSpecifier.of(lispSymbol.getName());
                emit(parameterSpecifier);
            } else {
                throw new LispEvaluationException("Required argument is not a symbol: %s".formatted(peek));
            }
        }
    }

    private void parseOptionalArguments() {
        /* Initialize optional args parser. */
        LispObject peek = peek();
        if (!peek.equals(LambdaListKeywords.OPTIONAL_KEYWORD)) {
            return;
        }
        forward();
        seenLambdaListKeyword.add(LambdaListKeywords.OPTIONAL_KEYWORD);

        /* Consume optional args. */
        while ((peek = peek()) != LispEnvironment.NIL) {
            if (peek instanceof LispSymbol lispSymbol) {
                if (LambdaListKeywords.isLambdaListKeyword(lispSymbol)) {
                    validateLambdaListKeywordTransition(lispSymbol);
                    break;
                }
                forward();

                ParameterSpecifier parameterSpecifier = OptionalParameterSpecifier.of(lispSymbol.getName(), Optional.empty(), Optional.empty());
                emit(parameterSpecifier);
            } else if (peek instanceof LispList lispList) {
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

    private void parseRestArguments() {
        LispObject peek = peek();
        if (!peek.equals(LambdaListKeywords.REST_KEYWORD)) {
            return;
        }
        forward();
        seenLambdaListKeyword.add(LambdaListKeywords.REST_KEYWORD);

        LispObject parameterName = peek();
        if (!(parameterName instanceof LispSymbol $parameterName)
            || $parameterName.equals(LispEnvironment.NIL)
            || LambdaListKeywords.isLambdaListKeyword($parameterName)) {
            throw new LispEvaluationException("Expect variable after &rest in: %s".formatted(lambdaList));
        }
        forward();

        RestParameterSpecifier restParameterSpecifier = RestParameterSpecifier.of($parameterName.getName());
        emit(restParameterSpecifier);

        // FIXME: ban the use of NIL as the local variable.
        peek = peek();
        if (peek != LispEnvironment.NIL) {
            if (peek instanceof LispSymbol lispSymbol) {
                if (LambdaListKeywords.isLambdaListKeyword(lispSymbol)) {
                    validateLambdaListKeywordTransition(lispSymbol);
                } else {
                    throw new LispEvaluationException("Expect lambda list keyword at %s in: %s".formatted(peek, lambdaList));
                }
            } else {
                throw new LispEvaluationException("Expect lambda list keyword at %s in: %s".formatted(peek, lambdaList));
            }
        }
    }

    private @NotNull LispSymbol getMostRecentlySeenLambdaListKeyword() {
        return CollectionUtil.getLast(seenLambdaListKeyword);
    }

    private void validateLambdaListKeywordTransition(@NotNull LispSymbol to) {
        @NotNull LispSymbol from = getMostRecentlySeenLambdaListKeyword();

        /* Validate repeated lambda list keyword. */
        if (this.seenLambdaListKeyword.contains(to)) {
            throw new LispEvaluationException("Repeated %s in lambda list: %s".formatted(to, lambdaList));
        }

        /* Validate next lambda list keywords. */
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

