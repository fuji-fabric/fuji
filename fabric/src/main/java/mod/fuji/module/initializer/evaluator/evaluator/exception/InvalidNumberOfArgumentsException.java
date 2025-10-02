package mod.fuji.module.initializer.evaluator.evaluator.exception;

public class InvalidNumberOfArgumentsException extends LispEvaluationException {

    public InvalidNumberOfArgumentsException(int actual) {
        super("Invalid number of arguments: " + actual);
    }
}
