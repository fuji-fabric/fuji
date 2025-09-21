package mod.fuji.core.command.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AbortCommandExecutionException extends RuntimeException {
    // NOTE: Actually, any exception will break the execution of a command. If you use this exception, then there is not any exception report, and will be treated as `exit normally`.
}
