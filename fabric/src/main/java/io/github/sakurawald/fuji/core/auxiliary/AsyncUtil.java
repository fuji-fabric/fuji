package io.github.sakurawald.fuji.core.auxiliary;

import java.util.concurrent.CompletableFuture;

public class AsyncUtil {

    public static void runAsyncAndSwallowExceptions(Runnable runnable) {
        CompletableFuture
            .runAsync(runnable)
            .exceptionally(exception -> {
                LogUtil.debug("Failed to run an async task: {}", exception);
                return null;
            });
    }

    public static void runAsyncAndHandleExceptions(Runnable runnable) {
        CompletableFuture
            .runAsync(runnable)
            .exceptionally(exception -> {
                LogUtil.error("Failed to run an async task.", exception);
                return null;
            });
    }

}
