package io.github.sakurawald.fuji.core.auxiliary;

import com.google.errorprone.annotations.CheckReturnValue;
import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.exception.FujiException;
import org.jetbrains.annotations.NotNull;

public class ExceptionUtil {

    @TestCase(action = "Test the exception handler functions.", targets = {
        "This mod failed at server startup, due to mixin injection errors.",
        "This mod failed at server startup, due to module initialization failed.",
        "This mod failed at the execution of `/fuji reload` command.",
        "This mod failed at the execution of `/json read a b` command.",
        "This mod failed at the execution of `/run as console run as player %player:name% bad` command."
    })
    @CheckReturnValue
    public static @NotNull RuntimeException makeReThrownException(@NotNull Exception exception) {
        return ExceptionUtil.makeReThrownException("⬆⬆⬆⬆⬆ Re-throw exception by Fuji Mod. Refer to the details above. ⬆⬆⬆⬆⬆", exception);
    }

    @CheckReturnValue
    @SuppressWarnings("SameParameterValue")
    private static @NotNull RuntimeException makeReThrownException(@NotNull String message, @NotNull Exception exception) {
        if (exception instanceof RuntimeException runtimeException) {
            return runtimeException;
        }

        return new FujiException(message, exception);
    }
}
