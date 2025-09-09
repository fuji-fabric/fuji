package io.github.sakurawald.fuji.core.diagnostic.analyzer;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class FujiCrashExceptionAnalyzer implements ExceptionAnalyzer {


    @Override
    public Optional<String> analyze(@NotNull Throwable throwable) {
        return Optional.empty();
    }
}
