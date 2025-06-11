package io.github.sakurawald.core.command.argument.exception;

public class ArgumentTypeNotSupportedException extends RuntimeException {
    public ArgumentTypeNotSupportedException() {
        super("This argument type is not supported in this MC version.");
    }
}
