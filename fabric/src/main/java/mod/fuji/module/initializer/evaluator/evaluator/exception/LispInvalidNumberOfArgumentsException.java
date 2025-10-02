package mod.fuji.module.initializer.evaluator.evaluator.exception;

public class LispInvalidNumberOfArgumentsException extends LispEvaluationException {

    public LispInvalidNumberOfArgumentsException(int actual) {
        super("Invalid number of arguments: " + actual);
    }
}
