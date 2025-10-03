package mod.fuji.core.auxiliary;

import com.google.errorprone.annotations.CheckReturnValue;
import java.util.ArrayList;
import java.util.List;
import mod.fuji.core.document.annotation.TestCase;
import mod.fuji.core.exception.FujiModException;
import org.jetbrains.annotations.NotNull;

public class ExceptionUtil {

    /**
     * Call this method, if you need to express the `TRY-CATCH-WRAP-RETHROW` pattern.
     * <p>
     * Unfortunately, the Java language doesn't support to throw a checked exception, without the `throws` signature.
     * It introduces the in-convenience especially inside the functional interfaces.
     * <p>
     * One solution is to use manifold-exceptions, and treat the checked-exception as unchecked-exception.
     * This solution requires the manifold-rt, to install the host at JVM runtime.
     * It works well in fabric platform.
     * However, it breaks the compatibility with `connector` mod's `class loader (TRANSFORMER)`.
     * <p>
     * You will get a big ClassNotFoundException, due to the class is loaded via different loaders.
     * The manifold-rt needs to generate the byte-code at runtime, to provide the power.
     * <p>
     * Finally, use the vanilla Java's pattern, for better compatibility.
     **/
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

        return new FujiModException(message, exception);
    }

    public static @NotNull List<Throwable> getThrowableChain(@NotNull Throwable throwable) {
        List<Throwable> chain = new ArrayList<>();
        Throwable current = throwable;
        while (current != null) {
            chain.add(current);
            current = current.getCause();
        }
        return chain;
    }
}
