package io.github.sakurawald.fuji.module.initializer.doctor.analyzer;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public abstract class ExceptionAnalyzer {

    @Getter(lazy = true)
    private final Pattern pattern = makePattern();

    protected abstract @NotNull Pattern makePattern();

    public abstract Optional<String> analyze(@NotNull List<Throwable> throwableChain, @NotNull String causeChain);

}
