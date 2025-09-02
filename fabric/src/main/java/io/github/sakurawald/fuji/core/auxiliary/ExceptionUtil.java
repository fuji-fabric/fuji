package io.github.sakurawald.fuji.core.auxiliary;

import com.google.errorprone.annotations.CheckReturnValue;
import io.github.sakurawald.fuji.core.document.annotation.ForDeveloper;
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
    @ForDeveloper("""
        Call this method, if you need to express the `TRY-CATCH-WRAP-RETHROW` pattern.

        Unfortunately, the Java language doesn't support to throw a checked exception, without the `throws` signature.
        It introduce the in-convenience especially inside the functional interfaces.

        One solution is to use manifold-exceptions, and treat the checked-exception as unchecked-exception.
        This solution requires the manifold-rt, to install the host at JVM runtime.
        It works well in fabric platform.
        However, it breaks the compatibility with `connector` mod's `class loader (TRANSFORMER)`.

        You will get a big ClassNotFoundException, due to the class is loaded via different loaders.
        The manifold-rt needs to generate the byte-code at runtime, to provide the power.

        Finally, use the vanilla Java's pattern, for better compatibility.
        """)
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
