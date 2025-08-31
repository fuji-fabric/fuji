package io.github.sakurawald.fuji.core.auxiliary;

import io.github.sakurawald.fuji.core.document.annotation.TestCase;
import io.github.sakurawald.fuji.core.exception.FujiException;
import org.jetbrains.annotations.NotNull;

public class ExceptionUtil {

    @TestCase(action = "Test the exception handler functions.", targets = {
        "This mod failed at server startup, due to mixin injection errors.",
        "This mod failed at server startup, due to module initialization failed.",
        "This mod failed at `/fuji reload` command."
    })
    public static void reThrowException(@NotNull Exception exception) {
        ExceptionUtil.reThrowException("⬆⬆⬆⬆⬆ Re-throw exception by Fuji Mod. Refer to the details above. ⬆⬆⬆⬆⬆", exception);
    }

    @SuppressWarnings("SameParameterValue")
    private static void reThrowException(@NotNull String message, @NotNull Exception exception) {
        if (exception instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }

        throw new FujiException(message, exception);
    }
}
