package mod.fuji.evaluator.evaluator.exception;

public class LispInvalidNumberOfArgumentsException extends LispEvaluationException {

    public LispInvalidNumberOfArgumentsException(int actual) {
        super("Invalid number of arguments: " + actual);
    }
}
