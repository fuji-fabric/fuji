package io.github.sakurawald.fuji.core.diagnostic.analyzer;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface ExceptionAnalyzer {

    Optional<String> analyze(@NotNull Throwable throwable);

}
