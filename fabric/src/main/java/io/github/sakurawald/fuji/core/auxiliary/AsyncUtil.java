package io.github.sakurawald.fuji.core.auxiliary;

import java.util.concurrent.CompletableFuture;

public class AsyncUtil {

    public static void runAsyncAndSwallowExceptions(Runnable runnable) {
        var unused = CompletableFuture
            .runAsync(runnable);
    }

    public static void runAsyncAndHandleExceptions(Runnable runnable) {
        var unused = CompletableFuture
            .runAsync(runnable)
            .exceptionally(exception -> {
                LogUtil.error("Failed to run an async task.", exception);
                return null;
            });
    }

}
